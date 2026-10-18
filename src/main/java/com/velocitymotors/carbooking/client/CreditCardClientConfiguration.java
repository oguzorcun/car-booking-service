package com.velocitymotors.carbooking.client;

import com.velocitymotors.carbooking.generated.creditcard.ApiClient;
import com.velocitymotors.carbooking.generated.creditcard.api.PaymentStatusApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditCardClientConfiguration {

    @Bean
    public PaymentStatusApi paymentStatusApi(@Value("${credit-card-api.base-url}") String baseUrl) {
        return new PaymentStatusApi(new ApiClient().setBasePath(baseUrl));
    }
}
