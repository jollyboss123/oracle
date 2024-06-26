package org.jolly.oracle.reduce.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@ConfigurationProperties(
        prefix = "app",
        ignoreUnknownFields = false
)
public class ApplicationProperties {
    private final Logging logging = new Logging();
    private final Pyroscope pyroscope = new Pyroscope();
    private final OpenTelemetry openTelemetry = new OpenTelemetry();

    @NoArgsConstructor
    @Getter
    public static class Logging {
        @Setter
        private boolean useJsonFormat = false;
        private final Logstash logstash = new Logstash();

        @NoArgsConstructor
        @Getter
        @Setter
        public static class Logstash {
            private boolean enabled = false;
            private String host = "localhost";
            private int port = 5000;
            private int ringBufferSize = 512;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OpenTelemetry {
        private boolean enabled = false;
        private String server;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Pyroscope {
        private boolean enabled = false;
        private String server;
    }
}
