package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.client.CreditCardClient;
import com.velocitymotors.carbooking.exception.BookingValidationException;
import com.velocitymotors.carbooking.exception.PaymentRejectedException;
import com.velocitymotors.carbooking.model.*;
import com.velocitymotors.carbooking.persistance.BookingRepository;
import com.velocitymotors.carbooking.persistance.InMemoryVehicleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private static final String VEHICLE_ID = "VEH-001";
    private static final String PAYMENT_REFERENCE = "PAY-REF-1";
    private static final LocalDate RENTAL_START = LocalDate.now().plusDays(10);
    private static final LocalDate RENTAL_END = RENTAL_START.plusDays(3);

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final InMemoryVehicleRepository vehicleRepository = mock(InMemoryVehicleRepository.class);
    private final CreditCardClient creditCardPaymentClient = mock(CreditCardClient.class);

    private final BookingService bookingService =
            new BookingService(creditCardPaymentClient, bookingRepository, vehicleRepository);

    @Test
    void confirmsDigitalWalletBookingImmediately() {
        given(vehicleRepository.existsById(VEHICLE_ID)).willReturn(true);
        given(bookingRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(command(PaymentMode.DIGITAL_WALLET));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getId()).startsWith("BKG").hasSize(10);
    }

    @Test
    void confirmsCreditCardBookingWhenPaymentIsApproved() {
        given(vehicleRepository.existsById(VEHICLE_ID)).willReturn(true);
        given(creditCardPaymentClient.isPaymentApproved(PAYMENT_REFERENCE)).willReturn(true);
        given(bookingRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(command(PaymentMode.CREDIT_CARD));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void rejectsCreditCardBookingWhenPaymentIsNotApproved() {
        given(vehicleRepository.existsById(VEHICLE_ID)).willReturn(true);
        given(creditCardPaymentClient.isPaymentApproved(PAYMENT_REFERENCE)).willReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(command(PaymentMode.CREDIT_CARD)))
                .isInstanceOf(PaymentRejectedException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createsBankTransferBookingAsPendingPayment() {
        given(vehicleRepository.existsById(VEHICLE_ID)).willReturn(true);
        given(bookingRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(command(PaymentMode.BANK_TRANSFER));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void rejectsBookingWhenEndDateIsNotAfterStartDate() {
        CreateBookingCommand command = new CreateBookingCommand(
                "John Doe", VEHICLE_ID, RENTAL_END, RENTAL_START,
                VehicleCategory.SUV, PaymentMode.DIGITAL_WALLET, PAYMENT_REFERENCE);

        assertThatThrownBy(() -> bookingService.createBooking(command))
                .isInstanceOf(BookingValidationException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsBookingWhenRentalPeriodIsLongerThanTwentyOneDays() {
        LocalDate start = LocalDate.now().plusDays(30);
        CreateBookingCommand command = new CreateBookingCommand(
                "John Doe", VEHICLE_ID, start, start.plusDays(22),
                VehicleCategory.SUV, PaymentMode.DIGITAL_WALLET, PAYMENT_REFERENCE);

        assertThatThrownBy(() -> bookingService.createBooking(command))
                .isInstanceOf(BookingValidationException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsBookingWhenVehicleIsUnknown() {
        given(vehicleRepository.existsById("VEH-999")).willReturn(false);
        CreateBookingCommand command = new CreateBookingCommand(
                "John Doe", "VEH-999", RENTAL_START, RENTAL_END,
                VehicleCategory.SUV, PaymentMode.DIGITAL_WALLET, PAYMENT_REFERENCE);

        assertThatThrownBy(() -> bookingService.createBooking(command))
                .isInstanceOf(BookingValidationException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmsPendingBankTransferBookingWhenPaymentIsReceived() {
        Booking booking = Booking.builder()
                .id("BKG0012345")
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
        given(bookingRepository.findById("BKG0012345")).willReturn(Optional.of(booking));

        bookingService.confirmBankTransferPayment("BKG0012345");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void ignoresPaymentForAlreadyConfirmedBooking() {
        Booking booking = Booking.builder()
                .id("BKG0012345")
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .status(BookingStatus.CONFIRMED)
                .build();
        given(bookingRepository.findById("BKG0012345")).willReturn(Optional.of(booking));

        bookingService.confirmBankTransferPayment("BKG0012345");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelsUnpaidBankTransferBookingsPastThePaymentDeadline() {
        Booking booking = Booking.builder()
                .id("BKG0012345")
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
        given(bookingRepository.findPendingBankTransferBookingsStartingBefore(any())).willReturn(List.of(booking));

        bookingService.cancelExpiredBankTransferBookings();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void returnsAllBookingsFromTheRepository() {
        Booking first = Booking.builder().id("BKG0000001").status(BookingStatus.CONFIRMED).build();
        Booking second = Booking.builder().id("BKG0000002").status(BookingStatus.PENDING_PAYMENT).build();
        given(bookingRepository.findAll()).willReturn(List.of(first, second));

        assertThat(bookingService.getAllBookings()).containsExactly(first, second);
    }

    private static CreateBookingCommand command(PaymentMode paymentMode) {
        return new CreateBookingCommand(
                "John Doe",
                VEHICLE_ID,
                RENTAL_START,
                RENTAL_END,
                VehicleCategory.SUV,
                paymentMode,
                PAYMENT_REFERENCE);
    }
}
