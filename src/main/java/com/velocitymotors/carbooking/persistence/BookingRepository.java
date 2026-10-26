package com.velocitymotors.carbooking.persistence;

import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, String> {

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.vehicleId = :vehicleId
              AND b.status IN :blockingStatuses
              AND b.rentalStartDate < :rentalEndDate
              AND b.rentalEndDate > :rentalStartDate
            """)
    long countOverlappingBookings(String vehicleId,
            LocalDate rentalStartDate,
            LocalDate rentalEndDate,
            Collection<BookingStatus> blockingStatuses);

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
