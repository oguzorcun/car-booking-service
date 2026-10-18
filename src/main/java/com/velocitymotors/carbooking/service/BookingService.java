package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.client.CreditCardClient;
import com.velocitymotors.carbooking.exception.PaymentRejectedException;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.CreateBookingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final CreditCardClient creditCardClient;

    public Booking createBooking(CreateBookingCommand command) {

        Booking booking =  Booking.builder()
                .id(generateBookingId())
                .customerName(command.customerName())
                .vehicleId(command.vehicleId())
                .rentalStartDate(command.rentalStartDate())
                .rentalEndDate(command.rentalEndDate())
                .vehicleCategory(command.vehicleCategory())
                .paymentMode(command.paymentMode())
                .paymentReference(command.paymentReference())
                .build();

        switch (booking.getPaymentMode()) {
            case DIGITAL_WALLET -> booking.confirm();
            case CREDIT_CARD -> {
                if (creditCardClient.isPaymentApproved(booking.getPaymentReference())) {
                    booking.confirm();
                } else {
                    throw new PaymentRejectedException(
                            "Credit card payment was rejected for reference " + booking.getPaymentReference());
                }
            }
            case BANK_TRANSFER -> booking.awaitPayment();
        }
        return booking;
    }

    private String generateBookingId() {
        return "BKG" + new Random().nextInt(9_000_000) + 1_000_000;
    }
}
