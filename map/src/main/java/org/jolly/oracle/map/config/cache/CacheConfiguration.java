package org.jolly.oracle.map.config.cache;

import org.jolly.oracle.map.config.ApplicationProperties;
import org.jolly.oracle.map.service.scheduled.AssetTickerService;
import org.jolly.oracle.map.web.rest.MapController;
import org.redisson.Redisson;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {
    private GitProperties gitProperties;
    private BuildProperties buildProperties;

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(ApplicationProperties props) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();

        URI redisURI = URI.create(props.getRedis().getServer().getFirst());

        Config config = new Config();
        if (props.getRedis().isCluster()) {
            ClusterServersConfig clusterServersConfig = config.useClusterServers()
                    .setMasterConnectionPoolSize(props.getRedis().getConnectionPoolSize())
                    .setMasterConnectionMinimumIdleSize(props.getRedis().getConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(props.getRedis().getSubscriptionConnectionPoolSize())
                    .addNodeAddress(props.getRedis().getServer().toArray(new String[0]));

            if (redisURI.getUserInfo() != null) {
                clusterServersConfig.setPassword(redisURI.getUserInfo().substring(redisURI.getUserInfo().indexOf(":") + 1));
            }
        } else {
            SingleServerConfig singleServerConfig = config.useSingleServer()
                    .setConnectionPoolSize(props.getRedis().getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(props.getRedis().getConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(props.getRedis().getSubscriptionConnectionPoolSize())
                    .setAddress(props.getRedis().getServer().getFirst());

            if (redisURI.getUserInfo() != null) {
                singleServerConfig.setPassword(redisURI.getUserInfo().substring(redisURI.getUserInfo().indexOf(":") + 1));
            }
        }

        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
                CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, props.getRedis().getExpiration()))
        );

        return RedissonConfiguration.fromInstance(Redisson.create(config), jcacheConfig);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            createCache(cm, MapController.PROCESSED_VAR_REQUEST_CACHE, jcacheConfiguration);
            createCache(cm, AssetTickerService.SCHED_LOCK_CACHE, jcacheConfiguration);
        };
    }

    private static void createCache(CacheManager cm, String cacheName, javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }

    @Autowired(required = false)
    public void setGitProperties(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
    }
}
