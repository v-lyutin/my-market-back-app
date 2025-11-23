package com.amit.mymarket.order.service.exception;

public final class EmptyCartException extends RuntimeException {

    public EmptyCartException(String message) {
        super(message);
    }

}
