package com.velocitymotors.carbooking.async;

import com.velocitymotors.carbooking.service.BankPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankTransferPaymentListener {

    private final BankPaymentService bankPaymentService;

    @KafkaListener(
            topics = "${bank-transfer-payment.topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentReceived(BankTransferPaymentEvent event) {
        bankPaymentService.confirmBooking(event);
    }
}
