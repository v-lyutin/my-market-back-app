package com.amit.mymarket.common.util;

import com.amit.mymarket.common.exception.ServiceException;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public final class UserIdUtils {

    public static Mono<String> ensureUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            return Mono.error(new ServiceException("userId is empty"));
        }
        return Mono.just(userId);
    }

    private UserIdUtils() {
        throw new UnsupportedOperationException();
    }

}
