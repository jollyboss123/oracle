package org.jolly.oracle.map.service.yahoofinance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RedirectableRequest {
    private final URL request;
    private final int protocolRedirectLimit;
    private final int connectTimeout;
    private final int readTimeout;

    private RedirectableRequest(URL request, int protocolRedirectLimit) {
        this(request, protocolRedirectLimit, 10_000, 10_000);
    }

    public static RedirectableRequest of(URL request, int protocolRedirectLimit, int connectTimeout, int readTimeout) {
        return new RedirectableRequest(request, protocolRedirectLimit, connectTimeout, readTimeout);
    }

    public static RedirectableRequest of(URL request, int protocolRedirectLimit) {
        return new RedirectableRequest(request, protocolRedirectLimit);
    }

    public URLConnection openConnection() throws IOException, URISyntaxException {
        return openConnection(Collections.emptyMap());
    }

    public URLConnection openConnection(Map<String, String> requestProperties) throws IOException, URISyntaxException {
        int redirectCount = 0;
        boolean hasResponse = false;
        HttpURLConnection connection = null;
        URL currentRequest = this.request;

        while (!hasResponse && (redirectCount <= this.protocolRedirectLimit)) {
            connection = (HttpURLConnection) currentRequest.openConnection();
            connection.setConnectTimeout(this.connectTimeout);
            connection.setReadTimeout(this.readTimeout);

            for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                connection.addRequestProperty(requestProperty.getKey(), requestProperty.getValue());
            }

            // only handle protocol redirects manually
            connection.setInstanceFollowRedirects(true);

            //TODO: handle FileNotFoundException i.e. ticker does not exist
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                redirectCount++;
                String location = connection.getHeaderField("Location");
                URI baseUri = currentRequest.toURI();
                URI resolvedUri = baseUri.resolve(location);
                currentRequest = resolvedUri.toURL();
            } else {
                hasResponse = true;
            }
        }

        if (redirectCount > this.protocolRedirectLimit) {
            throw new IOException("Protocol redirect count exceeded for url: " + this.request.toExternalForm());
        } else {
            return connection;
        }
    }
}
