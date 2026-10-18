package com.velocitymotors.carbooking.persistance;

import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, String> {
    List<BookingEntity> findByStatusAndPaymentModeAndRentalStartDateLessThan(
            BookingStatus status, PaymentMode paymentMode, LocalDate rentalStartDate);
}
