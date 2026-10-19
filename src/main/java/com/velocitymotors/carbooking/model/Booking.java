package com.velocitymotors.carbooking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Booking {
    @Id
    private String id;

    private String customerName;

    private String vehicleId;

    private LocalDate rentalStartDate;

    private LocalDate rentalEndDate;

    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    private VehicleCategory vehicleCategory;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public void confirm() {
        status = BookingStatus.CONFIRMED;
    }

    public void awaitPayment() {
        status = BookingStatus.PENDING_PAYMENT;
    }

    public void cancel() {
        status = BookingStatus.CANCELLED;
    }
}
