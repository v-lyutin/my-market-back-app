package com.amit.mymarket.user.repository;

import com.amit.mymarket.user.model.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {

    @Query(value = """
                SELECT roles.name
                FROM shop.users_roles
                JOIN shop.roles ON roles.id = users_roles.role_id
                WHERE users_roles.user_id = :userId
            """)
    Flux<String> findRoleNamesByUserId(Long userId);

    Mono<Role> findByName(String name);

}
