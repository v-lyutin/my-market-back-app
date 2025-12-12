package com.amit.payment.client.exception;

import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public final class ErrorMapper {

    public static Throwable mapToDomainException(Throwable exception) {
        if (exception instanceof WebClientRequestException) {
            return new PaymentServiceUnavailableException(exception);
        }
        if (exception instanceof WebClientResponseException wre && wre.getStatusCode().is5xxServerError()) {
            return new PaymentServiceUnavailableException(exception);
        }
        return exception;
    }

    private ErrorMapper() {
        throw new UnsupportedOperationException();
    }

}
