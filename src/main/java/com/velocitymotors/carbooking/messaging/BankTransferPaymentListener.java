package com.velocitymotors.carbooking.messaging;

import com.velocitymotors.carbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferPaymentListener {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${bank-transfer-payment.topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentReceived(String payload) {

        BankTransferPaymentEvent event = objectMapper.readValue(payload, BankTransferPaymentEvent.class);
        String bookingId = extractBookingId(event.transactionDetails());

        if (bookingService.confirmBankTransferPayment(bookingId)) {
            log.info("Confirmed booking {} for bank transfer payment {}", bookingId, event.paymentId());
        } else {
            // duplicate event (already confirmed) or canceled booking (late payment) or not found
            log.info("Ignored bank transfer payment {} for booking {}", event.paymentId(), bookingId);
        }
    }

    private String extractBookingId(String transactionDetails) {
        return transactionDetails.split(" ")[1];
    }
}
