package com.amit.mymarket.user.service.impl;

import com.amit.mymarket.user.repository.MarketUserRepository;
import com.amit.mymarket.user.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DatabaseReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final MarketUserRepository marketUserRepository;

    private final RoleRepository roleRepository;

    @Autowired
    public DatabaseReactiveUserDetailsService(MarketUserRepository marketUserRepository, RoleRepository roleRepository) {
        this.marketUserRepository = marketUserRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return this.marketUserRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .flatMap(user ->
                        this.roleRepository.findRoleNamesByUserId(user.getId())
                                .map(SimpleGrantedAuthority::new)
                                .collectList()
                                .map(authorities -> User.withUsername(user.getUsername())
                                        .password(user.getPasswordHash())
                                        .authorities(authorities)
                                        .disabled(Boolean.FALSE.equals(user.getEnabled()))
                                        .build()
                                )
                );
    }

}
