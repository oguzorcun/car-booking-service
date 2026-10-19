package com.velocitymotors.carbooking.messaging;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankTransferPaymentEvent(
        String paymentId,
        String senderAccountNumber,
        BigDecimal paymentAmount,
        String transactionDetails) {
}
