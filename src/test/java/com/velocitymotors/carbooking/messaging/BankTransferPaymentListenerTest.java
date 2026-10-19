package com.velocitymotors.carbooking.messaging;

import com.velocitymotors.carbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BankTransferPaymentListenerTest {

    private final BookingService bookingService = mock(BookingService.class);

    private BankTransferPaymentListener listener;

    @BeforeEach
    void setUp() {
        listener = new BankTransferPaymentListener(bookingService, new ObjectMapper());
    }

    @Test
    void confirmsBookingIdExtractedFromTransactionDetails() {
        String payload = """
                {
                  "paymentId": "PAY-1",
                  "senderAccountNumber": "NL91ABNA0417164300",
                  "paymentAmount": 1250.00,
                  "transactionDetails": "TXN987654321 BKG0012345"
                }
                """;

        listener.onPaymentReceived(payload);

        verify(bookingService).confirmBankTransferPayment("BKG0012345");
    }
}
