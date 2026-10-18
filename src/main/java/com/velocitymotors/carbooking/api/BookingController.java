package com.velocitymotors.carbooking.api;

import com.velocitymotors.carbooking.generated.booking.api.BookingApi;
import com.velocitymotors.carbooking.generated.booking.model.BookingResponse;
import com.velocitymotors.carbooking.generated.booking.model.CreateBookingRequest;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingController implements BookingApi {

    private final BookingService bookingService;

    @Override
    public BookingResponse createBooking(CreateBookingRequest createBookingRequest) {
        Booking booking = bookingService.createBooking(BookingApiMapper.toCommand(createBookingRequest));
        return BookingApiMapper.toResponse(booking);
    }
}
