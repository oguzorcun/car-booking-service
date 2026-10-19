package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.client.CreditCardClient;
import com.velocitymotors.carbooking.exception.BookingValidationException;
import com.velocitymotors.carbooking.exception.PaymentRejectedException;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.CreateBookingCommand;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.persistance.BookingRepository;
import com.velocitymotors.carbooking.persistance.InMemoryVehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int BANK_TRANSFER_PAYMENT_DEADLINE_DAYS = 2;
    private static final int MAX_RENTAL_DAYS = 21;
    private final CreditCardClient creditCardClient;
    private final BookingRepository bookingRepository;
    private final InMemoryVehicleRepository vehicleRepository;

    public Booking createBooking(CreateBookingCommand command) {

        validate(command);

        Booking booking = Booking.builder()
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
                if (validateCreditCardPayment(booking.getPaymentReference()))
                    booking.confirm();
            }
            case BANK_TRANSFER -> booking.awaitPayment();
        }
        return bookingRepository.save(booking);
    }

    // TODO: Handle booking not found
    // TODO: Handle duplicate payment event
    @Transactional
    public void confirmBankTransferPayment(String bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT
                    && booking.getPaymentMode() == PaymentMode.BANK_TRANSFER) {
                booking.confirm();
                bookingRepository.save(booking);
            }
        });
    }

    @Transactional
    public void cancelExpiredBankTransferBookings() {
        LocalDate earliestPossibleRentalStartDate = LocalDate.now().plusDays(BANK_TRANSFER_PAYMENT_DEADLINE_DAYS);
        bookingRepository.findPendingBankTransferBookingsStartingBefore(earliestPossibleRentalStartDate).forEach(booking -> {
            booking.cancel();
            bookingRepository.save(booking);
        });
    }

    // TODO: Need more validations?
    private void validate(CreateBookingCommand command) {
        if (!command.rentalEndDate().isAfter(command.rentalStartDate())) {
            throw new BookingValidationException("Rental end date must be after rental start date");
        }
        if (ChronoUnit.DAYS.between(command.rentalStartDate(), command.rentalEndDate()) > MAX_RENTAL_DAYS) {
            throw new BookingValidationException("A car cannot be booked for more than " + MAX_RENTAL_DAYS + " days");
        }
        if (!vehicleRepository.existsById(command.vehicleId())) {
            throw new BookingValidationException("Invalid vehicle id: " + command.vehicleId());
        }
    }

    private boolean validateCreditCardPayment(String paymentReference) {
        if (creditCardClient.isPaymentApproved(paymentReference)) {
            return true;
        } else {
            throw new PaymentRejectedException(
                    "Credit card payment was rejected for reference " + paymentReference);
        }
    }

    private String generateBookingId() {
        return "BKG" + ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);
    }
}
