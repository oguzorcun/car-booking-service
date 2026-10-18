package com.velocitymotors.carbooking.api;

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
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setStatus(BookingStatus.valueOf(booking.getStatus().name()));
        return response;
    }
}
