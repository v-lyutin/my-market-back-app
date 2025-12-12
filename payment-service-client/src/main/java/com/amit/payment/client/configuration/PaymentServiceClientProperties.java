package com.amit.payment.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment-service-client")
public record PaymentServiceClientProperties(
        String baseUrl) {
}
