package com.amit.mymarket.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializationContext<String, Object> context = RedisSerializationContext.<String, Object>newSerializationContext(keySerializer)
                .key(keySerializer)
                .value(RedisSerializer.json())
                .hashKey(keySerializer)
                .hashValue(RedisSerializer.json())
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

}
