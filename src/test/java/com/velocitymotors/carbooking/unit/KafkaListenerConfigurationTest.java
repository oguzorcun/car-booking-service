package com.velocitymotors.carbooking.unit;

import com.velocitymotors.carbooking.async.KafkaListenerConfiguration;
import com.velocitymotors.carbooking.exception.BankTransferPaymentEventValidationException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.RetryListener;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaListenerConfigurationTest {

    private static final ConsumerRecord<String, String> RECORD =
            new ConsumerRecord<>("bank-transfer-payment-events", 0, 42L, "key", "payload");

    private final DefaultErrorHandler errorHandler = new KafkaListenerConfiguration().kafkaErrorHandler(); // custom error handler from application
    private final RecordingRetryListener retries = new RecordingRetryListener(); // custom retry listener to record retry attemps

    private final Consumer<?, ?> consumer = mock(Consumer.class);
    private final MessageListenerContainer container = mock(MessageListenerContainer.class);

    @BeforeEach
    void setUp() {
        errorHandler.setRetryListeners(retries);
    }

    @Test
    void skipsValidationFailureImmediatelyWithoutRetrying() {
        // the listener adapter wraps whatever the service threw
        Exception thrown = new ListenerExecutionFailedException("Listener failed",
                new BankTransferPaymentEventValidationException("Transaction details without a booking id"));

        // true = handled: the container commits the offset instead of redelivering the record
        assertThat(errorHandler.handleOne(thrown, RECORD, consumer, container)).isTrue();

        assertThat(retries.deliveryAttempts).containsExactly(1); // no retry, only the initial delivery
        assertThat(retries.recoveries).isEqualTo(1); // went straight to the recoverer, so it was logged and skipped
    }

    @Test
    void retriesTransientFailuresThreeTimesBeforeSkipping() {
        Exception thrown = new TransientDataAccessResourceException("database down");

        assertThat(errorHandler.handleOne(thrown, RECORD, consumer, container)).isFalse(); // initial delivery
        assertThat(errorHandler.handleOne(thrown, RECORD, consumer, container)).isFalse(); // retry 1
        assertThat(errorHandler.handleOne(thrown, RECORD, consumer, container)).isFalse(); // retry 2
        assertThat(errorHandler.handleOne(thrown, RECORD, consumer, container)).isTrue(); // retry 3, then skipped

        assertThat(retries.deliveryAttempts).containsExactly(1, 2, 3, 4);
        assertThat(retries.recoveries).isEqualTo(1);
    }

    private static final class RecordingRetryListener implements RetryListener {

        private final List<Integer> deliveryAttempts = new ArrayList<>();
        private int recoveries;

        @Override
        public void failedDelivery(ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {
            deliveryAttempts.add(deliveryAttempt);
        }

        @Override
        public void recovered(ConsumerRecord<?, ?> record, Exception ex) {
            recoveries++;
        }
    }
}
