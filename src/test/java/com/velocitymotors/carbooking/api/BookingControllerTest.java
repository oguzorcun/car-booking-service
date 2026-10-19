package com.velocitymotors.carbooking.api;

import com.velocitymotors.carbooking.exception.BookingValidationException;
import com.velocitymotors.carbooking.exception.PaymentRejectedException;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String REQUEST = """
            {
              "customerName": "John Doe",
              "vehicleId": "VEH-qqqqqq001",
              "rentalStartDate": "2026-11-01",
              "rentalEndDate": "2026-11-05",
              "vehicleCategory": "SUV",
              "paymentMode": "DIGITAL_WALLET",
              "paymentReference": "PAY-REF-1"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void returnsTheCreatedBooking() throws Exception {
        var bookingId = "BKG0012345";
        given(bookingService.createBooking(any())).willReturn(
                Booking.builder().id(bookingId).status(BookingStatus.CONFIRMED).build());

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.name()));
    }

    @Test
    void returnsBadRequestWhenBookingIsInvalid() throws Exception {
        var message = "Unknown vehicle id: VEH-999";
        given(bookingService.createBooking(any()))
                .willThrow(new BookingValidationException(message));

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(message));
    }

    @Test
    void returnsUnprocessableEntityWhenPaymentIsRejected() throws Exception {
        var message = "Credit card payment was not approved";
        given(bookingService.createBooking(any()))
                .willThrow(new PaymentRejectedException(message));

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").value(message));
    }
}
