package com.velocitymotors.carbooking.async;

import com.velocitymotors.carbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BankTransferPaymentListenerTest {

    private final BookingService bookingService = mock(BookingService.class);

    private BankTransferPaymentListener listener;

    @BeforeEach
    void setUp() {
        listener = new BankTransferPaymentListener(bookingService);
    }

    @Test
    void confirmsBookingIdExtractedFromTransactionDetails() {
        BankTransferPaymentEvent event = BankTransferPaymentEvent.builder()
                .paymentId("BANK-PAY-ID-1")
                .senderAccountNumber("IBAN-NUMBER-1")
                .paymentAmount(new BigDecimal("500.00"))
                .transactionDetails("TXN123456789 BKG0012345")
                .build();

        listener.onPaymentReceived(event);

        verify(bookingService).confirmBankTransferPayment("BKG0012345");
    }
}
