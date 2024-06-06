package org.jolly.oracle.map.web.rest.errors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class HeaderUtil {

    public static HttpHeaders createFailureAlert(String applicationName, boolean enableTranslation, String entityName, String errorKey, String defaultMessage) {
        log.error("entity processing failed, {}", defaultMessage);
        String message;
        if (enableTranslation) {
            message = "error." + errorKey;
        } else {
            message = defaultMessage;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-%s-error".formatted(applicationName), message);
        headers.add("X-%s-params".formatted(applicationName), entityName);
        return headers;
    }
}
