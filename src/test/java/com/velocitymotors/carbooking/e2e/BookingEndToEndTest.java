package com.velocitymotors.carbooking.e2e;

import com.velocitymotors.carbooking.generated.booking.model.BookingResponse;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import com.velocitymotors.carbooking.persistance.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * - posts a booking via API
 * - reads it from the pgsql DB
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class BookingEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void persistsBookingPostedThroughHttpEndpoint() {
        LocalDate rentalStartDate = LocalDate.now().plusDays(10);
        LocalDate rentalEndDate = rentalStartDate.plusDays(3);
        String request = """
                {
                  "customerName": "John Doe",
                  "vehicleId": "VEH-001",
                  "rentalStartDate": "%s",
                  "rentalEndDate": "%s",
                  "vehicleCategory": "SUV",
                  "paymentMode": "DIGITAL_WALLET",
                  "paymentReference": "PAY-REF-1"
                }
                """.formatted(rentalStartDate, rentalEndDate);

        ResponseEntity<BookingResponse> response = RestClient.create("http://localhost:" + port)
                .post()
                .uri("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(BookingResponse.class);

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
}
