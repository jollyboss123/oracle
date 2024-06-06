package org.jolly.oracle.map.web.rest.errors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ProblemDetailWithCause {
    @Getter
    @Setter
    private ProblemDetailWithCause cause;
    private final ProblemDetail problemDetail;

    ProblemDetailWithCause(int rawStatus) {
        problemDetail = ProblemDetail.forStatus(rawStatus);
    }

    ProblemDetailWithCause(int rawStatus, ProblemDetailWithCause cause) {
        problemDetail = ProblemDetail.forStatus(rawStatus);
        this.cause = cause;
    }

    public ProblemDetailWithCause(ProblemDetail problemDetail) {
        this.problemDetail = problemDetail;
    }

    public ProblemDetail asProblemDetail() {
        return problemDetail;
    }

    public static ProblemDetailWithCause from(ProblemDetail problemDetail) {
        return new ProblemDetailWithCause(problemDetail);
    }

    public URI getType() {
        return problemDetail.getType();
    }

    public void setType(URI type) {
        problemDetail.setType(type);
    }

    public String getTitle() {
        return problemDetail.getTitle();
    }

    public void setTitle(String title) {
        problemDetail.setTitle(title);
    }

    public int getStatus() {
        return problemDetail.getStatus();
    }

    public void setStatus(HttpStatus status) {
        problemDetail.setStatus(status);
    }

    public void setStatus(int status) {
        problemDetail.setStatus(status);
    }

    public String getDetail() {
        return problemDetail.getDetail();
    }

    public void setDetail(String detail) {
        problemDetail.setDetail(detail);
    }

    public URI getInstance() {
        return problemDetail.getInstance();
    }

    public void setInstance(URI instance) {
        problemDetail.setInstance(instance);
    }

    public Map<String, Object> getProperties() {
        return problemDetail.getProperties();
    }

    public void setProperty(String key, Object value) {
        problemDetail.setProperty(key, value);
    }

    public static class ProblemDetailWithCauseBuilder {
        private static final URI BLANK_TYPE = URI.create("about:blank");
        // From Springs Problem Detail
        private URI type = BLANK_TYPE;
        private String title;
        private int status;
        private String detail;
        private URI instance;
        private Map<String, Object> properties = new HashMap<>();
        private ProblemDetailWithCause cause;

        public static ProblemDetailWithCauseBuilder instance() {
            return new ProblemDetailWithCauseBuilder();
        }

        public ProblemDetailWithCauseBuilder withType(URI type) {
            this.type = type;
            return this;
        }

        public ProblemDetailWithCauseBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public ProblemDetailWithCauseBuilder withStatus(int status) {
            this.status = status;
            return this;
        }

        public ProblemDetailWithCauseBuilder withDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public ProblemDetailWithCauseBuilder withInstance(URI instance) {
            this.instance = instance;
            return this;
        }

        public ProblemDetailWithCauseBuilder withCause(ProblemDetailWithCause cause) {
            this.cause = cause;
            return this;
        }

        public ProblemDetailWithCauseBuilder withProperties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public ProblemDetailWithCauseBuilder withProperty(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public ProblemDetailWithCause build() {
            ProblemDetailWithCause problemDetailWithCause = new ProblemDetailWithCause(this.status);
            problemDetailWithCause.setType(this.type);
            problemDetailWithCause.setTitle(this.title);
            problemDetailWithCause.setDetail(this.detail);
            problemDetailWithCause.setInstance(this.instance);
            this.properties.forEach(problemDetailWithCause::setProperty);
            problemDetailWithCause.setCause(this.cause);
            return problemDetailWithCause;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProblemDetailWithCause pdwc)) {
            return false;
        }
        return pdwc.cause.equals(this.cause) && pdwc.problemDetail.equals(this.problemDetail);
    }

    @Override
    public int hashCode() {
        return 31 * cause.hashCode() + problemDetail.hashCode();
    }
}
