package org.jolly.oracle.map.config;

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
    private final Polygon polygon = new Polygon();
    private final Redis redis = new Redis();

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

    @Setter
    @NoArgsConstructor
    @Getter
    public static class Polygon {
        private String apiKey;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Redis {
        private List<String> server = List.of("redis://localhost:6379");
        private int expiration = 300;
        private boolean cluster = false;
        private int connectionPoolSize = 64;
        private int connectionMinimumIdleSize = 24;
        private int subscriptionConnectionPoolSize = 50;
        private int subscriptionConnectionMinimumIdleSize = 1;
    }
}
