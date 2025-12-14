package com.amit.mymarket.cart.service.cache;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisCartCacheInvalidator implements CartCacheInvalidator {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisCartCacheInvalidator(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> invalidateCart(String sessionId) {
        return this.redisTemplate.opsForValue()
                .delete("cart:view:" + sessionId)
                .then();
    }

}
