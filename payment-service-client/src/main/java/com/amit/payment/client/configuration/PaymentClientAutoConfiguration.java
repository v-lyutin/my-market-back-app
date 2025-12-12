package com.amit.payment.client.configuration;

import com.amit.payment.client.api.PaymentApi;
import com.amit.payment.client.invoker.ApiClient;
import com.amit.payment.client.service.PaymentServiceGateway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@EnableConfigurationProperties(value = PaymentServiceClientProperties.class)
public class PaymentClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiClient paymentApiClient(WebClient.Builder builder, PaymentServiceClientProperties properties) {
        ApiClient apiClient = new ApiClient(builder.build());
        apiClient.setBasePath(properties.baseUrl());
        return apiClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentApi paymentApi(ApiClient paymentApiClient) {
        return new PaymentApi(paymentApiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentServiceGateway paymentServiceGateway(PaymentApi paymentApi) {
        return new PaymentServiceGateway(paymentApi);
    }

}
