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
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(HttpMethod.POST, "/items/**").authenticated()
                        .pathMatchers("/management/**").hasRole("MANAGER")
                        .pathMatchers("/cart/**", "/orders/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/logout").authenticated()
                        .pathMatchers(HttpMethod.GET, "/login", "/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/register").permitAll()
                        .pathMatchers(HttpMethod.GET, "/", "/items/**").permitAll()
                        .anyExchange().denyAll()
                )
                .requestCache(cache -> cache.requestCache(new WebSessionServerRequestCache()))
                .formLogin(form -> form.loginPage("/login"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(this.redirectServerLogoutSuccessHandler())
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

    RedirectServerLogoutSuccessHandler redirectServerLogoutSuccessHandler() {
        RedirectServerLogoutSuccessHandler redirectServerLogoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        redirectServerLogoutSuccessHandler.setLogoutSuccessUrl(URI.create("/login?logout"));
        return redirectServerLogoutSuccessHandler;
    }

}
