package com.velocitymotors.carbooking.persistance;

import com.velocitymotors.carbooking.TestcontainersConfiguration;
import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void confirmsAPendingBankTransferBooking() {
        Booking booking = savePendingBankTransferBooking("BKG-CAS0001");

        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isTrue();

        assertThat(statusOf(booking.getId())).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void doesNotConfirmTheSameBookingTwice() {
        Booking booking = savePendingBankTransferBooking("BKG-CAS0002");
        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isTrue();

        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isFalse();

        assertThat(statusOf(booking.getId())).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void doesNotConfirmACancelledBooking() {
        Booking booking = savePendingBankTransferBooking("BKG-CAS0003");
        booking.cancel();
        bookingRepository.save(booking);

        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isFalse();

        assertThat(statusOf(booking.getId())).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void doesNotConfirmAPendingBookingThatIsNotABankTransfer() {
        bookingRepository.save(Booking.builder()
                .id("BKG-CAS0004")
                .customerName("John Doe")
                .vehicleId("VEH-001")
                .rentalStartDate(LocalDate.now().plusDays(10))
                .rentalEndDate(LocalDate.now().plusDays(13))
                .vehicleCategory(VehicleCategory.SUV)
                .paymentMode(PaymentMode.DIGITAL_WALLET)
                .paymentReference("PAY-REF-CAS0004")
                .status(BookingStatus.PENDING_PAYMENT)
                .build());

        assertThat(bookingRepository.confirmPendingBankTransferBooking("BKG-CAS0004")).isFalse();
    }

    @Test
    void doesNotConfirmAnUnknownBooking() {
        assertThat(bookingRepository.confirmPendingBankTransferBooking("BKG-CAS0005")).isFalse();
    }

    @Test
    void cancelsOnlyUnpaidBankTransferBookingsInsideTheDeadlineWindow() {
        savePendingBankTransferBooking("BKG-CAS0006", LocalDate.now().plusDays(1)); // inside, should go
        savePendingBankTransferBooking("BKG-CAS0007", LocalDate.now().plusDays(5)); // starts later, stays
        Booking alreadyPaid = savePendingBankTransferBooking("BKG-CAS0008", LocalDate.now().plusDays(1));
        assertThat(bookingRepository.confirmPendingBankTransferBooking(alreadyPaid.getId())).isTrue();

        int cancelled = bookingRepository
                .cancelPendingBankTransferBookingsStartingBefore(LocalDate.now().plusDays(2));

        assertThat(cancelled).isEqualTo(1);
        assertThat(statusOf("BKG-CAS0006")).isEqualTo(BookingStatus.CANCELLED);
        assertThat(statusOf("BKG-CAS0007")).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(statusOf("BKG-CAS0008")).isEqualTo(BookingStatus.CONFIRMED);
    }

    private Booking savePendingBankTransferBooking(String bookingId) {
        return savePendingBankTransferBooking(bookingId, LocalDate.now().plusDays(10));
    }

    private Booking savePendingBankTransferBooking(String bookingId, LocalDate rentalStartDate) {
        return bookingRepository.save(Booking.builder()
                .id(bookingId)
                .customerName("John Doe")
                .vehicleId("VEH-001")
                .rentalStartDate(rentalStartDate)
                .rentalEndDate(rentalStartDate.plusDays(3))
                .vehicleCategory(VehicleCategory.SUV)
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .paymentReference("PAY-REF-" + bookingId)
                .status(BookingStatus.PENDING_PAYMENT)
                .build());
    }

    private BookingStatus statusOf(String bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow().getStatus();
    }
}
