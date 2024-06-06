package org.jolly.oracle.map.web.rest.errors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.*;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows <a href="https://tools.ietf.org/html/rfc7807">RFC7807</a> - Problem Details for HTTP APIs.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionTranslator extends ResponseEntityExceptionHandler {
    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final boolean CASUAL_CHAIN_ENABLED = false;

    @Value("${spring.application.name}")
    private final String applicationName;
    private final Environment env;

    @ExceptionHandler
    public ResponseEntity<Object> handleAnyException(Throwable ex, NativeWebRequest request) {
        ProblemDetailWithCause pdCause = wrapAndCustomizeProblem(ex, request);
        return handleExceptionInternal((Exception) ex, pdCause, buildHeaders(ex).orElse(null), HttpStatusCode.valueOf(pdCause.getStatus()), request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        return super.handleExceptionInternal(ex,
                Optional.ofNullable(body)
                        .orElseGet(() -> wrapAndCustomizeProblem(ex, (NativeWebRequest) request)),
                headers,
                statusCode,
                request);
    }

    protected ProblemDetailWithCause wrapAndCustomizeProblem(Throwable ex, NativeWebRequest request) {
        return customizeProblem(getProblemDetailWithCause(ex), ex, request);
    }

    protected ProblemDetailWithCause customizeProblem(ProblemDetailWithCause problem, Throwable err, NativeWebRequest request) {
        if (problem.getStatus() <= 0) {
            problem.setStatus(toStatus(err));
        }

        if (problem.getType() == null || problem.getType().equals(URI.create("about:blank"))) {
            problem.setType(getMappedType(err));
        }

        // higher precedence to Custom/ResponseStatus types
        String title = extractTitle(err, problem.getStatus());
        String problemTitle = problem.getTitle();
        if (problemTitle == null || !problemTitle.equals(title)) {
            problem.setTitle(title);
        }

        if (problem.getDetail() == null) {
            // higher precedence to cause
            problem.setDetail(getCustomizedErrorDetails(err));
        }

        Map<String, Object> problemProperties = problem.getProperties();
        if (problemProperties == null || !problemProperties.containsKey(MESSAGE_KEY)) {
            problem.setProperty(MESSAGE_KEY, getMappedMessageKey(err)
                    .orElse("error.http." + problem.getStatus()));
        }

        if (problemProperties == null || !problemProperties.containsKey(PATH_KEY)) {
            problem.setProperty(PATH_KEY, getPathValue(request));
        }

        if (
            (err instanceof MethodArgumentNotValidException fieldException) &&
            (problemProperties == null || !problemProperties.containsKey(FIELD_ERRORS_KEY))
        ) {
            problem.setProperty(FIELD_ERRORS_KEY, getFieldErrors(fieldException));
        }

        problem.setCause(buildCause(err.getCause(), request).orElse(null));

        return problem;
    }

    private static HttpStatus toStatus(final Throwable throwable) {
        // let the ErrorResponse take responsibility
        if (throwable instanceof ErrorResponse err) {
            return HttpStatus.valueOf(err.getBody().getStatus());
        }

        return getMappedStatus(throwable)
                .orElseGet(() -> resolveResponseStatus(throwable)
                        .map(ResponseStatus::value)
                        .orElse(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Maps to the specific http status instead of resorting to Spring's defaults.
     *
     * @param err the Exception received.
     * @return the http status we intended.
     */
    private static Optional<HttpStatus> getMappedStatus(Throwable err) {
        if (err instanceof ConcurrencyFailureException) {
            return Optional.of(HttpStatus.CONFLICT);
        }

        return Optional.empty();
    }

    private static Optional<ResponseStatus> resolveResponseStatus(final Throwable type) {
        final ResponseStatus candidate = AnnotatedElementUtils.findMergedAnnotation(type.getClass(), ResponseStatus.class);
        if (candidate == null && type.getCause() != null) {
            return resolveResponseStatus(type.getCause());
        } else {
            return Optional.ofNullable(candidate);
        }
    }

    private static URI getMappedType(Throwable err) {
        if (err instanceof MethodArgumentNotValidException) {
            return ErrorConstants.CONSTRAINT_VIOLATION_TYPE;
        }

        return ErrorConstants.DEFAULT_TYPE;
    }

    private static String extractTitle(Throwable err, int statusCode) {
        return getCustomizedTitle(err)
                .orElseGet(() -> extractTitleForResponseStatus(err, statusCode));
    }

    private static Optional<String> getCustomizedTitle(Throwable err) {
        if (err instanceof MethodArgumentNotValidException) {
            return Optional.of("Method argument not valid");
        }

        return Optional.empty();
    }

    private static String extractTitleForResponseStatus(Throwable err, int statusCode) {
        Optional<ResponseStatus> specialStatus = extractResponseStatus(err);

        return specialStatus.map(ResponseStatus::reason)
                .orElse(HttpStatus.valueOf(statusCode).getReasonPhrase());
    }

    private static Optional<ResponseStatus> extractResponseStatus(final Throwable throwable) {
        return resolveResponseStatus(throwable);
    }

    private String getCustomizedErrorDetails(Throwable err) {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains("prod")) {
            if (err instanceof HttpMessageConversionException) {
                return "Unable to convert http message";
            }
            if (err instanceof DataAccessException) {
                return "Failure during data access";
            }
            if (containsPackageName(err.getMessage())) {
                return "Unexpected runtime exception";
            }
        }

        return Optional.ofNullable(err.getCause())
                .map(Throwable::getMessage)
                .orElseGet(err::getMessage);
    }

    private static boolean containsPackageName(String message) {
        // this list is for sure not complete
        return StringUtils.containsAny(message, "org.", "java.", "net.", "jakarta.", "javax.", "com.", "io.", "de.", "org.jolly.oracle");
    }

    private static Optional<String> getMappedMessageKey(Throwable err) {
        if (err instanceof MethodArgumentNotValidException) {
            return Optional.of(ErrorConstants.ERR_VALIDATION);
        }
        if (err instanceof ConcurrencyFailureException || err.getCause() instanceof ConcurrencyFailureException) {
            return Optional.of(ErrorConstants.ERR_CONCURRENCY_FAILURE);
        }
        return Optional.empty();
    }

    private static URI getPathValue(NativeWebRequest request) {
        if (request == null) {
            return URI.create("about:blank");
        }

        return URI.create(extractURI(request));
    }

    private static String extractURI(NativeWebRequest request) {
        HttpServletRequest nativeRequest = request.getNativeRequest(HttpServletRequest.class);

        return Optional.ofNullable(nativeRequest)
                .map(HttpServletRequest::getRequestURI)
                .orElse("");
    }

    private static List<FieldErrorVM> getFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors().stream()
                .map(f -> new FieldErrorVM(
                        f.getObjectName().replaceFirst("DTO$", ""),
                        f.getField(),
                        StringUtils.isNotBlank(f.getDefaultMessage()) ? f.getDefaultMessage() : f.getCode()
                ))
                .toList();
    }

    private Optional<ProblemDetailWithCause> buildCause(final Throwable throwable, NativeWebRequest request) {
        if (throwable != null && CASUAL_CHAIN_ENABLED) {
            return Optional.of(customizeProblem(getProblemDetailWithCause(throwable), throwable, request));
        }

        return Optional.empty();
    }

    private static ProblemDetailWithCause getProblemDetailWithCause(Throwable ex) {
        if (ex instanceof ErrorResponseException exp) {
            ProblemDetail problemDetail = exp.getBody();
            return ProblemDetailWithCause.from(problemDetail);
        }

        return ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(toStatus(ex).value())
                .build();
    }

    private Optional<HttpHeaders> buildHeaders(Throwable err) {
        if (err instanceof BadRequestAlertException badRequestAlertException) {
            return Optional.of(HeaderUtil.createFailureAlert(
                    applicationName,
                    true,
                    badRequestAlertException.getEntityName(),
                    badRequestAlertException.getErrorKey(),
                    badRequestAlertException.getMessage()
            ));
        }

        return Optional.empty();
    }
}
