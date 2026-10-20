package com.velocitymotors.carbooking.integration;

import com.velocitymotors.carbooking.model.Booking;
import com.velocitymotors.carbooking.model.BookingStatus;
import com.velocitymotors.carbooking.model.PaymentMode;
import com.velocitymotors.carbooking.model.VehicleCategory;
import com.velocitymotors.carbooking.persistence.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRepositoryTest extends IntegrationTest{

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void confirmsAPendingBankTransferBooking() {
        Booking booking = savePendingBankTransferBooking("BKG-CAS0001");

        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isEqualTo(1);
        assertThat(statusOf(booking.getId())).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void doesNotConfirmTheSameBookingTwice() {
        Booking booking = savePendingBankTransferBooking("BKG-CAS0002");

        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isEqualTo(1);
        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isEqualTo(0);
        assertThat(statusOf(booking.getId())).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void doesNotConfirmACancelledBooking() {
        Booking booking = savePendingBankTransferBooking("BKG-CAS0003");
        booking.cancel();
        bookingRepository.save(booking);

        assertThat(bookingRepository.confirmPendingBankTransferBooking(booking.getId())).isEqualTo(0);
        assertThat(statusOf(booking.getId())).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void doesNotConfirmAPendingBookingThatIsNotABankTransfer() {
        bookingRepository.save(pendingBooking("BKG-CAS0004")
                .paymentMode(PaymentMode.DIGITAL_WALLET)
                .build());

        assertThat(bookingRepository.confirmPendingBankTransferBooking("BKG-CAS0004")).isEqualTo(0);
    }

    @Test
    void doesNotConfirmAnUnknownBooking() {
        assertThat(bookingRepository.confirmPendingBankTransferBooking("BKG-CAS0005")).isEqualTo(0);
    }

    @Test
    void cancelsOnlyUnpaidBankTransferBookingsInsideTheDeadlineWindow() {
        savePendingBankTransferBooking("BKG-CAS0006", LocalDate.now().plusDays(1)); // inside, should go
        savePendingBankTransferBooking("BKG-CAS0007", LocalDate.now().plusDays(5)); // starts later, stays
        Booking alreadyPaid = savePendingBankTransferBooking("BKG-CAS0008", LocalDate.now().plusDays(1));
        assertThat(bookingRepository.confirmPendingBankTransferBooking(alreadyPaid.getId())).isEqualTo(1);

        int cancelled = bookingRepository
                .cancelPendingBankTransferBookingsStartingBefore(LocalDate.now().plusDays(2));

        assertThat(cancelled).isEqualTo(1);
        assertThat(statusOf("BKG-CAS0006")).isEqualTo(BookingStatus.CANCELLED);
        assertThat(statusOf("BKG-CAS0007")).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(statusOf("BKG-CAS0008")).isEqualTo(BookingStatus.CONFIRMED);
    }

    private Booking savePendingBankTransferBooking(String bookingId) {
        return bookingRepository.save(pendingBooking(bookingId).build());
    }

    private Booking savePendingBankTransferBooking(String bookingId, LocalDate rentalStartDate) {
        return bookingRepository.save(pendingBooking(bookingId)
                .rentalStartDate(rentalStartDate)
                .rentalEndDate(rentalStartDate.plusDays(3))
                .build());
    }

    private Booking.BookingBuilder pendingBooking(String bookingId) {
        return Booking.builder()
                .id(bookingId)
                .customerName("John Doe")
                .vehicleId("VEH-001")
                .rentalStartDate(LocalDate.now().plusDays(10))
                .rentalEndDate(LocalDate.now().plusDays(13))
                .vehicleCategory(VehicleCategory.SUV)
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .paymentReference("PAY-REF-" + bookingId)
                .status(BookingStatus.PENDING_PAYMENT);
    }

    private BookingStatus statusOf(String bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow().getStatus();
    }
}
