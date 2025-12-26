package com.amit.mymarket.user.service.impl;

import com.amit.mymarket.user.model.MarketUser;
import com.amit.mymarket.user.model.UserRole;
import com.amit.mymarket.user.repository.MarketUserRepository;
import com.amit.mymarket.user.repository.RoleRepository;
import com.amit.mymarket.user.repository.UserRoleRepository;
import com.amit.mymarket.user.service.RegistrationService;
import com.amit.mymarket.user.service.exception.UsernameAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
public class DefaultRegistrationService implements RegistrationService {

    private static final String ROLE_USER = "ROLE_USER";

    private final MarketUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;

    private final TransactionalOperator transactionalOperator;

    @Autowired
    public DefaultRegistrationService(MarketUserRepository userRepository,
                                      RoleRepository roleRepository,
                                      UserRoleRepository userRoleRepository,
                                      PasswordEncoder passwordEncoder,
                                      TransactionalOperator transactionalOperator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Void> registerUser(String username, String rawPassword) {
        String normalizedUsername = this.normalizeUsername(username);
        return this.userRepository.findByUsername(normalizedUsername)
                .flatMap(marketUser -> Mono.<Void>error(new UsernameAlreadyExistsException(normalizedUsername)))
                .switchIfEmpty(
                        this.roleRepository.findByName(ROLE_USER)
                                .switchIfEmpty(Mono.error(new IllegalStateException("Role not found: " + ROLE_USER)))
                                .flatMap(role -> {
                                    MarketUser user = new MarketUser();
                                    user.setUsername(normalizedUsername);
                                    user.setPasswordHash(this.passwordEncoder.encode(rawPassword));
                                    user.setEnabled(true);
                                    return this.userRepository.save(user)
                                            .flatMap(saved ->
                                                    this.userRoleRepository.save(new UserRole(saved.getId(), role.getId())).then()
                                            );
                                })
                )
                .as(this.transactionalOperator::transactional)
                .onErrorMap(DuplicateKeyException.class, exception -> new UsernameAlreadyExistsException(normalizedUsername));
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }

}
