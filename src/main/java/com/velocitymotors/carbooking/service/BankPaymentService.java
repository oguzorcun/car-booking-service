package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.async.BankTransferPaymentEvent;
import com.velocitymotors.carbooking.exception.BankTransferPaymentEventValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankPaymentService {

    private static final Pattern TRANSACTION_DETAILS = Pattern.compile("\\S+\\s+(?<bookingId>BKG\\d{7})");

    private final BookingService bookingService;

    public void confirmBooking(BankTransferPaymentEvent event) {
        String bookingId = extractBookingId(event);

        if (bookingService.confirmPendingBankTransferBooking(bookingId)) {
            log.info("Confirmed booking {} for bank transfer payment {}", bookingId, event.paymentId());
        } else {
            // duplicate event (already confirmed) or canceled booking (late payment) or not found
            log.info("Ignored bank transfer payment {} for booking {}", event.paymentId(), bookingId);
        }
    }

    private String extractBookingId(BankTransferPaymentEvent event) {
        if (event == null || event.transactionDetails() == null || event.transactionDetails().isBlank()) {
            throw new BankTransferPaymentEventValidationException(
                    "Bank transfer payment event without transaction details");
        }

        Matcher transactionDetails = TRANSACTION_DETAILS.matcher(event.transactionDetails().trim());
        if (!transactionDetails.matches()) {
            throw new BankTransferPaymentEventValidationException(
                    "Transaction details without a booking id: " + event.transactionDetails());
        }
        return transactionDetails.group("bookingId");
    }
}
