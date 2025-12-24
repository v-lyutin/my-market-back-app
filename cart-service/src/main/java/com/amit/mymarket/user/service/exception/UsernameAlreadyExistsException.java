package com.amit.mymarket.user.service.exception;

public final class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }

}
