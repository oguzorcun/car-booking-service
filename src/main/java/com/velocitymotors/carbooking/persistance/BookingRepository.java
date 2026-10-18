package com.velocitymotors.carbooking.persistance;

import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepository {

    private final BookingJpaRepository repository;

    public Booking save(Booking booking) {
        return toDomain(repository.save(toEntity(booking)));
    }

    public Optional<Booking> findById(String id) {
        return repository.findById(id).map(BookingRepository::toDomain);
    }

    public List<Booking> findPendingBankTransferBookingsStartingBefore(LocalDate date) {
        return repository
                .findByStatusAndPaymentModeAndRentalStartDateLessThan(
                        BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER, date)
                .stream()
                .map(BookingRepository::toDomain)
                .toList();
    }

    private static BookingEntity toEntity(Booking booking) {
        return BookingEntity.builder()
                .id(booking.getId())
                .customerName(booking.getCustomerName())
                .vehicleId(booking.getVehicleId())
                .rentalStartDate(booking.getRentalStartDate())
                .rentalEndDate(booking.getRentalEndDate())
                .vehicleCategory(booking.getVehicleCategory())
                .paymentMode(booking.getPaymentMode())
                .paymentReference(booking.getPaymentReference())
                .status(booking.getStatus())
                .build();
    }

    private static Booking toDomain(BookingEntity entity) {
        return Booking.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .vehicleId(entity.getVehicleId())
                .rentalStartDate(entity.getRentalStartDate())
                .rentalEndDate(entity.getRentalEndDate())
                .vehicleCategory(entity.getVehicleCategory())
                .paymentMode(entity.getPaymentMode())
                .paymentReference(entity.getPaymentReference())
                .status(entity.getStatus())
                .build();
    }
}
