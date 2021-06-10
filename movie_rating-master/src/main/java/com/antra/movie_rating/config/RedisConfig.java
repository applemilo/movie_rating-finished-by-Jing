package com.antra.movie_rating.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.io.Serializable;
import java.time.Duration;


    @Configuration
    @EnableCaching
    public class RedisConfig implements Serializable {
        @Autowired
        private RedisTemplate<String, Object> template;
        @Bean
        public CacheManager cacheManager( ) {

            // configuration
            RedisCacheConfiguration defaultCacheConfiguration =
                    RedisCacheConfiguration
                            .defaultCacheConfig()
                            // set key is string
                            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(template.getStringSerializer()))
                            // Object convert json into object
                            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(template.getValueSerializer()))
                            // no cache is null
                            .disableCachingNullValues()
                            // cache data for 1 hour
                            .entryTtl(Duration.ofHours(1));

            // create a redis cache manager
            RedisCacheManager redisCacheManager =
                    RedisCacheManager.RedisCacheManagerBuilder
                            // Redis connection
                            .fromConnectionFactory(template.getConnectionFactory())
                            // cache setting
                            .cacheDefaults(defaultCacheConfiguration)
                            // put/evict
                            .transactionAware()
                            .build();

            return redisCacheManager;
        }


    }


