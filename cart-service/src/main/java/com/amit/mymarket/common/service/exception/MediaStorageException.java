package com.amit.mymarket.common.service.exception;

public final class MediaStorageException extends RuntimeException {

    public MediaStorageException(String message) {
        super(message);
    }

    public MediaStorageException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
