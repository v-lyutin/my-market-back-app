package com.amit.payment.client.service;

import com.amit.payment.client.api.PaymentApi;
import com.amit.payment.client.exception.ErrorMapper;
import com.amit.payment.client.model.BalanceResponse;
import com.amit.payment.client.model.PaymentRequest;
import com.amit.payment.client.model.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class PaymentServiceGateway {

    private final PaymentApi paymentApi;

    public PaymentServiceGateway(PaymentApi paymentApi) {
        this.paymentApi = paymentApi;
    }

    public Mono<Long> getBalanceKopecks(String accountId) {
        return this.paymentApi.getBalance(accountId)
                .map(BalanceResponse::getBalance)
                .onErrorMap(ErrorMapper::mapToDomainException);
    }

    public Mono<Boolean> tryPay(String accountId, long amount) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);

        return this.paymentApi.makePayment(accountId, paymentRequest)
                .map(PaymentResponse::getSuccess)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                        return Mono.just(false);
                    }
                    return Mono.error(ErrorMapper.mapToDomainException(exception));
                })
                .onErrorMap(ErrorMapper::mapToDomainException);
    }

}
