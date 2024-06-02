package org.jolly.oracle.map.config.cache;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PrefixedKeyGenerator implements KeyGenerator {
    @Getter
    private final String prefix;
    private String shortCommitId = null;
    private Instant time = null;
    private String version = null;

    public PrefixedKeyGenerator(GitProperties gitProperties, BuildProperties buildProperties) {
        this.prefix = this.generatePrefix(gitProperties, buildProperties);
    }

    private String generatePrefix(GitProperties gitProperties, BuildProperties buildProperties) {
        if (Objects.nonNull(gitProperties)) {
            this.shortCommitId = gitProperties.getShortCommitId();
        }

        if (Objects.nonNull(buildProperties)) {
            this.time = buildProperties.getTime();
            this.version = buildProperties.getVersion();
        }

        Object p = ObjectUtils.firstNonNull(this.shortCommitId, this.time, this.version, RandomStringUtils.randomAlphanumeric(12));
        return p instanceof Instant instant ? DateTimeFormatter.ISO_INSTANT.format(instant) : p.toString();
    }

    @Override
    public Object generate(@NonNull Object target, Method method, @NonNull Object... params) {
        return new PrefixedSimpleKey(this.prefix, method.getName(), params);
    }
}
