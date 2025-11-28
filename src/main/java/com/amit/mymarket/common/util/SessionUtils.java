package com.amit.mymarket.common.util;

import com.amit.mymarket.common.exception.ServiceException;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public final class SessionUtils {

    public static Mono<String> ensureSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Mono.error(new ServiceException("Session id is empty"));
        }
        return Mono.just(sessionId);
    }

    private SessionUtils() {
        throw new UnsupportedOperationException();
    }

}
