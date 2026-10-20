package com.velocitymotors.carbooking.api;

import com.velocitymotors.carbooking.generated.booking.model.BookingDetails;
import com.velocitymotors.carbooking.generated.booking.model.BookingResponse;
import com.velocitymotors.carbooking.generated.booking.model.BookingStatus;
import com.velocitymotors.carbooking.generated.booking.model.CreateBookingRequest;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.CreateBookingCommand;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;

final class BookingApiMapper {

    static CreateBookingCommand toCommand(CreateBookingRequest request) {
        return new CreateBookingCommand(
                request.getCustomerName(),
                request.getVehicleId(),
                request.getRentalStartDate(),
                request.getRentalEndDate(),
                VehicleCategory.valueOf(request.getVehicleCategory().name()),
                PaymentMode.valueOf(request.getPaymentMode().name()),
                request.getPaymentReference());
    }

    static BookingResponse toResponse(Booking booking) {
        return new BookingResponse()
                .bookingId(booking.getId())
                .status(BookingStatus.valueOf(booking.getStatus().name()));
    }

    static BookingDetails toDetails(Booking booking) {
        return new BookingDetails()
                .bookingId(booking.getId())
                .customerName(booking.getCustomerName())
                .vehicleId(booking.getVehicleId())
                .rentalStartDate(booking.getRentalStartDate())
                .rentalEndDate(booking.getRentalEndDate())
                .vehicleCategory(com.velocitymotors.carbooking.generated.booking.model.VehicleCategory
                        .valueOf(booking.getVehicleCategory().name()))
                .paymentMode(com.velocitymotors.carbooking.generated.booking.model.PaymentMode
                        .valueOf(booking.getPaymentMode().name()))
                .paymentReference(booking.getPaymentReference())
                .status(BookingStatus.valueOf(booking.getStatus().name()));
    }
}
