package com.velocitymotors.carbooking.unit;

import com.velocitymotors.carbooking.async.BankTransferPaymentEvent;
import com.velocitymotors.carbooking.async.BankTransferPaymentListener;
import com.velocitymotors.carbooking.service.BankPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BankTransferPaymentListenerTest {

    private final BankPaymentService bankPaymentService = mock(BankPaymentService.class);

    private BankTransferPaymentListener listener;

    @BeforeEach
    void setUp() {
        listener = new BankTransferPaymentListener(bankPaymentService);
    }

    @Test
    void handsReceivedEventsToTheBankPaymentService() {
        BankTransferPaymentEvent event = BankTransferPaymentEvent.builder()
                .paymentId("BANK-PAY-ID-1")
                .senderAccountNumber("IBAN-NUMBER-1")
                .paymentAmount(new BigDecimal("500.00"))
                .transactionDetails("TXN123456789 BKG0012345")
                .build();

        listener.onPaymentReceived(event);

        verify(bankPaymentService).confirmBooking(event);
    }
}
