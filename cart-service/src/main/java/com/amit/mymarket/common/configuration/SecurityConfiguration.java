package com.amit.mymarket.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(HttpMethod.GET, "/", "/items/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/items/**").authenticated()
                        .pathMatchers("/management/**").hasRole("MANAGER")
                        .pathMatchers("/cart/**", "/orders/**").authenticated()
                        .anyExchange().denyAll()
                )
                .requestCache(cache -> cache.requestCache(new WebSessionServerRequestCache()))
                .formLogin(form -> form.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/items")))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(new SecurityContextServerLogoutHandler())
                        .logoutHandler(new WebSessionServerLogoutHandler())
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
