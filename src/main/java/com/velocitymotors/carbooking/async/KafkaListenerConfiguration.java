package com.velocitymotors.carbooking.async;

import com.velocitymotors.carbooking.exception.BankTransferPaymentEventValidationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaListenerConfiguration {

    /**
     * Transient failures (e.g. db down) are retried 3 times, 1s apart
     * Malformed events are skipped
     * Undeserializable payloads are also skipped (ErrorHandlingDeserializer in app config)
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1_000L, 3L));
        handler.addNotRetryableExceptions(BankTransferPaymentEventValidationException.class);
        return handler;
    }
}
