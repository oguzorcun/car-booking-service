package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.CreateBookingCommand;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BookingService {

    public Booking createBooking(CreateBookingCommand command) {

        return Booking.builder()
                .id(generateBookingId())
                .customerName(command.customerName())
                .vehicleId(command.vehicleId())
                .rentalStartDate(command.rentalStartDate())
                .rentalEndDate(command.rentalEndDate())
                .vehicleCategory(command.vehicleCategory())
                .paymentMode(command.paymentMode())
                .paymentReference(command.paymentReference())
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
    }

    private String generateBookingId() {
        return "BKG" + new Random().nextInt(9_000_000) + 1_000_000;
    }
}
