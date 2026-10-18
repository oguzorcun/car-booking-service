package com.velocitymotors.carbooking.persistance;

import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    private String id;

    private String customerName;

    private String vehicleId;

    private LocalDate rentalStartDate;

    private LocalDate rentalEndDate;

    @Enumerated(EnumType.STRING)
    private VehicleCategory vehicleCategory;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
