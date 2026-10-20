package com.velocitymotors.carbooking.integration;

import com.velocitymotors.carbooking.generated.booking.model.BookingDetails;
import com.velocitymotors.carbooking.generated.booking.model.BookingResponse;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import com.velocitymotors.carbooking.persistence.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * - posts a booking via API
 * - reads it from the pgsql DB
 */
class BookingApiTest extends AbstractIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void persistsBookingPostedThroughHttpEndpoint() {
        LocalDate rentalStartDate = LocalDate.now().plusDays(10);
        LocalDate rentalEndDate = rentalStartDate.plusDays(3);
        String request = bookingRequest("John Doe", "VEH-001", rentalStartDate, rentalEndDate,
                VehicleCategory.SUV, PaymentMode.DIGITAL_WALLET, "PAY-REF-1");

        ResponseEntity<BookingResponse> response = postBooking(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).hasToString(BookingStatus.CONFIRMED.name());

        Booking saved = bookingRepository.findById(response.getBody().getBookingId()).orElseThrow();
        assertThat(saved.getCustomerName()).isEqualTo("John Doe");
        assertThat(saved.getVehicleId()).isEqualTo("VEH-001");
        assertThat(saved.getRentalStartDate()).isEqualTo(rentalStartDate);
        assertThat(saved.getRentalEndDate()).isEqualTo(rentalEndDate);
        assertThat(saved.getVehicleCategory()).isEqualTo(VehicleCategory.SUV);
        assertThat(saved.getPaymentMode()).isEqualTo(PaymentMode.DIGITAL_WALLET);
        assertThat(saved.getPaymentReference()).isEqualTo("PAY-REF-1");
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void listsBookingsCreatedThroughHttp() {
        LocalDate rentalStartDate = LocalDate.now().plusDays(10);
        String request = bookingRequest("Jane Roe", "VEH-003", rentalStartDate, rentalStartDate.plusDays(2),
                VehicleCategory.LUXURY, PaymentMode.DIGITAL_WALLET, "PAY-REF-2");

        String bookingId = postBooking(request).getBody().getBookingId();

        BookingDetails[] bookings = getAllBookings();

        BookingDetails listed = Arrays.stream(bookings)
                .filter(booking -> booking.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow();
        // mapping of other fields covered by BookingControllerTest
        assertThat(listed.getRentalStartDate()).isEqualTo(rentalStartDate);
    }
}
