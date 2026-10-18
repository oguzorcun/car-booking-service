package com.velocitymotors.carbooking.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class Booking {
    private String id;
    private String customerName;
    private String vehicleId;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private VehicleCategory vehicleCategory;
    private PaymentMode paymentMode;
    private String paymentReference;
    private BookingStatus status;

    public void confirm() {
        status = BookingStatus.CONFIRMED;
    }

    public void awaitPayment() {
        status = BookingStatus.PENDING_PAYMENT;
    }
}
