package com.amit.mymarket.user.service;

import reactor.core.publisher.Mono;

public interface RegistrationService {

    Mono<Void> registerUser(String username, String rawPassword);

}
