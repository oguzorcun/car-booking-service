package com.velocitymotors.carbooking.unit;

import com.velocitymotors.carbooking.api.BookingController;
import com.velocitymotors.carbooking.exception.BookingValidationException;
import com.velocitymotors.carbooking.exception.PaymentRejectedException;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import com.velocitymotors.carbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String REQUEST = """
            {
              "customerName": "John Doe",
              "vehicleId": "VEH-001",
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
    void returnsAllBookings() throws Exception {
        Booking booking = Booking.builder()
                .id("BKG0012345")
                .customerName("John Doe")
                .vehicleId("VEH-001")
                .rentalStartDate(LocalDate.of(2026, 11, 1))
                .rentalEndDate(LocalDate.of(2026, 11, 5))
                .vehicleCategory(VehicleCategory.SUV)
                .paymentMode(PaymentMode.DIGITAL_WALLET)
                .paymentReference("PAY-REF-1")
                .status(BookingStatus.CONFIRMED)
                .build();
        given(bookingService.getAllBookings()).willReturn(List.of(booking));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookingId").value("BKG0012345"))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$[0].vehicleId").value("VEH-001"))
                .andExpect(jsonPath("$[0].rentalStartDate").value("2026-11-01"))
                .andExpect(jsonPath("$[0].rentalEndDate").value("2026-11-05"))
                .andExpect(jsonPath("$[0].vehicleCategory").value("SUV"))
                .andExpect(jsonPath("$[0].paymentMode").value("DIGITAL_WALLET"))
                .andExpect(jsonPath("$[0].paymentReference").value("PAY-REF-1"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
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
