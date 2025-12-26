package com.amit.mymarket.user.repository;

import com.amit.mymarket.user.model.MarketUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MarketUserRepository extends ReactiveCrudRepository<MarketUser, Long> {

    Mono<MarketUser> findByUsername(String username);

}
