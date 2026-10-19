package com.velocitymotors.carbooking.unit;

import com.velocitymotors.carbooking.async.BankTransferPaymentEvent;
import com.velocitymotors.carbooking.exception.BankTransferPaymentEventValidationException;
import com.velocitymotors.carbooking.service.BankTransferPaymentService;
import com.velocitymotors.carbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BankTransferPaymentServiceTest {

    private final BookingService bookingService = mock(BookingService.class);

    private BankTransferPaymentService bankTransferPaymentService;

    @BeforeEach
    void setUp() {
        bankTransferPaymentService = new BankTransferPaymentService(bookingService);
    }

    @Test
    void confirmsBookingIdTakenFromTransactionDetails() {
        bankTransferPaymentService.confirmBooking(event("TXN123456789 BKG0012345"));

        verify(bookingService).confirmPendingBankTransferBooking("BKG0012345");
    }

    @Test
    void rejectsAnEventWithoutTransactionDetails() {
        assertThatThrownBy(() -> bankTransferPaymentService.confirmBooking(event(null)))
                .isInstanceOf(BankTransferPaymentEventValidationException.class);
        assertThatThrownBy(() -> bankTransferPaymentService.confirmBooking(event("   ")))
                .isInstanceOf(BankTransferPaymentEventValidationException.class);

        verifyNoInteractions(bookingService);
    }

    @Test
    void rejectsTransactionDetailsWithoutABookingId() {
        assertThatThrownBy(() -> bankTransferPaymentService.confirmBooking(event("TXN123456789")))
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
