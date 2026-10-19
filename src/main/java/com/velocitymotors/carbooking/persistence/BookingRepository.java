package com.velocitymotors.carbooking.persistence;

import com.velocitymotors.carbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, String> {

    @Transactional
    @Modifying
    @Query("""
            UPDATE Booking b
            SET b.status = CONFIRMED
            WHERE b.id = :bookingId
              AND b.status = PENDING_PAYMENT
              AND b.paymentMode = BANK_TRANSFER
            """)
    int confirmPendingBankTransferBooking(String bookingId);

    @Transactional
    @Modifying
    @Query("""
            UPDATE Booking b
            SET b.status = CANCELLED
            WHERE b.status = PENDING_PAYMENT
              AND b.paymentMode = BANK_TRANSFER
              AND b.rentalStartDate < :earliestBookingRentalStartDate
            """)
    int cancelPendingBankTransferBookingsStartingBefore(LocalDate earliestBookingRentalStartDate);
}
