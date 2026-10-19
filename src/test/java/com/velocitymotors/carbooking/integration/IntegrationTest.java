package com.velocitymotors.carbooking.integration;

import com.velocitymotors.carbooking.TestcontainersConfiguration;
import com.velocitymotors.carbooking.generated.booking.model.BookingDetails;
import com.velocitymotors.carbooking.generated.booking.model.BookingResponse;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

/**
 * Shared plumbing for integration tests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
abstract class IntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUpClient() {
        client = RestClient.create("http://localhost:" + port);
    }

    protected static String bookingRequest(String customerName, String vehicleId, LocalDate rentalStartDate,
            LocalDate rentalEndDate, VehicleCategory vehicleCategory, PaymentMode paymentMode,
            String paymentReference) {
        return """
                {
                  "customerName": "%s",
                  "vehicleId": "%s",
                  "rentalStartDate": "%s",
                  "rentalEndDate": "%s",
                  "vehicleCategory": "%s",
                  "paymentMode": "%s",
                  "paymentReference": "%s"
                }
                """.formatted(customerName, vehicleId, rentalStartDate, rentalEndDate, vehicleCategory, paymentMode,
                paymentReference);
    }

    protected ResponseEntity<BookingResponse> postBooking(String request) {
        return client.post()
                .uri("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(BookingResponse.class);
    }

    protected BookingDetails[] getAllBookings() {
        return client.get()
                .uri("/bookings")
                .retrieve()
                .body(BookingDetails[].class);
    }
}
