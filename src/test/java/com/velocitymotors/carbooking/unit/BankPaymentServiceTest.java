package com.velocitymotors.carbooking.unit;

import com.velocitymotors.carbooking.async.BankTransferPaymentEvent;
import com.velocitymotors.carbooking.exception.BankTransferPaymentEventValidationException;
import com.velocitymotors.carbooking.service.BankPaymentService;
import com.velocitymotors.carbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BankPaymentServiceTest {

    private final BookingService bookingService = mock(BookingService.class);

    private BankPaymentService bankPaymentService;

    @BeforeEach
    void setUp() {
        bankPaymentService = new BankPaymentService(bookingService);
    }

    @Test
    void confirmsBookingIdTakenFromTransactionDetails() {
        bankPaymentService.confirmBooking(event("TXN123456789 BKG0012345"));

        verify(bookingService).confirmPendingBankTransferBooking("BKG0012345");
    }

    @Test
    void rejectsAnEventWithoutTransactionDetails() {
        assertThatThrownBy(() -> bankPaymentService.confirmBooking(event(null)))
                .isInstanceOf(BankTransferPaymentEventValidationException.class);
        assertThatThrownBy(() -> bankPaymentService.confirmBooking(event("   ")))
                .isInstanceOf(BankTransferPaymentEventValidationException.class);

        verifyNoInteractions(bookingService);
    }

    @Test
    void rejectsTransactionDetailsWithoutABookingId() {
        assertThatThrownBy(() -> bankPaymentService.confirmBooking(event("TXN123456789")))
                .isInstanceOf(BankTransferPaymentEventValidationException.class);

        verifyNoInteractions(bookingService);
    }

    private static BankTransferPaymentEvent event(String transactionDetails) {
        return BankTransferPaymentEvent.builder()
                .paymentId("BANK-PAY-ID-1")
                .senderAccountNumber("IBAN-NUMBER-1")
                .paymentAmount(new BigDecimal("500.00"))
                .transactionDetails(transactionDetails)
                .build();
    }
}
