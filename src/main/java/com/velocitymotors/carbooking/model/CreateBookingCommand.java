package com.velocitymotors.carbooking.model;

import java.time.LocalDate;

public record CreateBookingCommand(
        String customerName,
        String vehicleId,
        LocalDate rentalStartDate,
        LocalDate rentalEndDate,
        VehicleCategory vehicleCategory,
        PaymentMode paymentMode,
        String paymentReference) {
}
