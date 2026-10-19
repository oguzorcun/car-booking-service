package com.velocitymotors.carbooking.e2e;

import com.velocitymotors.carbooking.async.BankTransferPaymentEvent;
import com.velocitymotors.carbooking.generated.booking.model.BookingResponse;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import com.velocitymotors.carbooking.persistence.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * - posts a bank transfer booking via API
 * - publishes a payment event to Kafka
 * - waits for the listener to confirm the booking
 */
class BookingViaBankTransferEndToEndTest extends EndToEndTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${bank-transfer-payment.topic}")
    private String topic;

    @Test
    void confirmsBookingWhenBankTransferPaymentIsReceived() throws Exception {
        LocalDate rentalStartDate = LocalDate.now().plusDays(10);
        String request = bookingRequest("Jane Roe", "VEH-002", rentalStartDate, rentalStartDate.plusDays(3),
                VehicleCategory.COMPACT, PaymentMode.BANK_TRANSFER, "BT-REF-1");

        ResponseEntity<BookingResponse> response = postBooking(request);

        // bank transfer booking is first created as pending payment
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).hasToString(BookingStatus.PENDING_PAYMENT.name());
        String bookingId = response.getBody().getBookingId();

        // simulate a bank transfer payment event
        BankTransferPaymentEvent event = BankTransferPaymentEvent.builder()
                .paymentId("BANK-PAY-ID-1")
                .senderAccountNumber("IBAN-NUMBER-1")
                .paymentAmount(new BigDecimal("500.00"))
                .transactionDetails("TXN123456789 " + bookingId)
                .build();
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(event)).join();

        // wait until the simulated event is processed by the listener
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Booking booking = bookingRepository.findById(bookingId).orElseThrow();
                    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
                });
    }
}
