package com.velocitymotors.carbooking.messaging;

import java.math.BigDecimal;

public record BankTransferPaymentEvent(
        String paymentId,
        String senderAccountNumber,
        BigDecimal paymentAmount,
        String transactionDetails) {
}
