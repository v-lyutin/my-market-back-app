package com.amit.payment.api;

import com.amit.payment.model.BalanceResponse;
import com.amit.payment.model.DepositRequest;
import com.amit.payment.model.PaymentRequest;
import com.amit.payment.model.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class PaymentApiController implements PaymentApi {

    private final AtomicLong balance = new AtomicLong(100_000L);

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String accountId, ServerWebExchange exchange) {
        long currentBalance = balance.get();

        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setAccountId(accountId);
        balanceResponse.setBalance(currentBalance);

        return Mono.just(ResponseEntity.ok(balanceResponse));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> makePayment(String accountId,
                                                             Mono<PaymentRequest> paymentRequest,
                                                             ServerWebExchange exchange) {
        return paymentRequest.flatMap(request -> {
            long amount = request.getAmount();

            if (amount <= 0) {
                return Mono.just(ResponseEntity.badRequest().build());
            }

            while (true) {
                long currentBalance = balance.get();

                if (currentBalance < amount) {
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                }

                long updatedBalance = currentBalance - amount;
                if (balance.compareAndSet(currentBalance, updatedBalance)) {
                    PaymentResponse paymentResponse = new PaymentResponse();
                    paymentResponse.setAccountId(accountId);
                    paymentResponse.setAmount(amount);
                    paymentResponse.setSuccess(true);

                    return Mono.just(ResponseEntity.ok(paymentResponse));
                }
            }
        });
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> deposit(String accountId,
                                                         Mono<DepositRequest> depositRequest,
                                                         ServerWebExchange exchange) {
        return depositRequest.flatMap(request -> {
            long added = 100_000L;
            long updated = balance.addAndGet(added);

            BalanceResponse response = new BalanceResponse();
            response.setAccountId(accountId);
            response.setBalance(updated);

            return Mono.just(ResponseEntity.ok(response));
        });
    }

}
