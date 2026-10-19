package com.velocitymotors.carbooking.async;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankTransferPaymentEvent(
        String paymentId,
        String senderAccountNumber,
        BigDecimal paymentAmount,
        String transactionDetails) {
}
