package com.amit.payment.client.exception;

public class PaymentServiceUnavailableException extends RuntimeException {

    public PaymentServiceUnavailableException(Throwable cause) {
        super("Payment service is unavailable", cause);
    }

}
