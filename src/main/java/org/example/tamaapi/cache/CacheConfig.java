package org.example.tamaapi.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public SimpleCacheManager localCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = Arrays.stream(MyCacheType.values())
                .map(cache -> new CaffeineCache(
                        cache.name(),
                        Caffeine.newBuilder()
                                .expireAfterWrite(cache.getExpireAfterWrite(), TimeUnit.SECONDS)
                                .maximumSize(cache.getMaximumSize())
                                .recordStats()
                                .build()
                ))
                .collect(Collectors.toList());

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory){
        Map<String, RedisCacheConfiguration> cacheConfigs =
                Arrays.stream(MyCacheType.values())
                        .collect(Collectors.toMap(
                                MyCacheType::name,
                                cache -> RedisCacheConfiguration.defaultCacheConfig()
                                        .entryTtl(Duration.ofSeconds(cache.getExpireAfterWrite()))
                                        .disableCachingNullValues()
                                        .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair
                                                        .fromSerializer(new GenericJackson2JsonRedisSerializer())
                                        )
                        ));

        return RedisCacheManager.builder(factory)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }


}
