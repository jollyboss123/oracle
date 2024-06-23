package org.jolly.oracle.map.config;

import io.pyroscope.http.Format;
import io.pyroscope.javaagent.EventType;
import io.pyroscope.javaagent.PyroscopeAgent;
import io.pyroscope.javaagent.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app", name = "pyroscope.enabled", havingValue = "true")
@Slf4j
public class PyroscopeConfiguration {

    public PyroscopeConfiguration(@Value("${app.pyroscope.server}") String pyroscopeServer,
                                  @Value("${spring.application.name}") String applicationName) {
        log.info("Setting up pyroscope");
        PyroscopeAgent.start(
                new Config.Builder()
                        .setApplicationName(applicationName)
                        .setLabels(Map.of("project", applicationName, "type", "api"))
                        .setProfilingEvent(EventType.ITIMER)
                        .setProfilingAlloc("512k")
                        .setAllocLive(true)
                        .setFormat(Format.JFR)
                        .setServerAddress(pyroscopeServer)
                        .build()
        );
    }
}
