package com.velocitymotors.carbooking.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, String> {

    @Transactional
    @Modifying
    @Query("""
            UPDATE BookingEntity b
            SET b.status = CONFIRMED
            WHERE b.id = :bookingId
              AND b.status = PENDING_PAYMENT
              AND b.paymentMode = BANK_TRANSFER
            """)
    int confirmPendingBankTransferBooking(String bookingId);

    @Transactional
    @Modifying
    @Query("""
            UPDATE BookingEntity b
            SET b.status = CANCELLED
            WHERE b.status = PENDING_PAYMENT
              AND b.paymentMode = BANK_TRANSFER
              AND b.rentalStartDate < :earliestBookingRentalStartDate
            """)
    int cancelPendingBankTransferBookingsStartingBefore(LocalDate earliestBookingRentalStartDate);
}
