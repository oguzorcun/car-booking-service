package com.velocitymotors.carbooking.async;

import com.velocitymotors.carbooking.service.BankTransferPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankTransferPaymentListener {

    private final BankTransferPaymentService bankTransferPaymentService;

    @KafkaListener(topics = "${bank-transfer-payment.topic}")
    public void onPaymentReceived(BankTransferPaymentEvent event) {
        bankTransferPaymentService.confirmBooking(event);
    }
}
