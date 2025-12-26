package com.amit.mymarket.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentOAuth2WebClientConfiguration {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(ReactiveClientRegistrationRepository clients,
                                                                                       ReactiveOAuth2AuthorizedClientService clientService) {
        ReactiveOAuth2AuthorizedClientProvider provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, clientService);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    public WebClient paymentWebClient(WebClient.Builder builder, ReactiveOAuth2AuthorizedClientManager manager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth.setDefaultClientRegistrationId("payment-service");
        return builder
                .filter(oauth)
                .build();
    }

}
