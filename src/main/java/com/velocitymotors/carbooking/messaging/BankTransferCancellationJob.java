package com.velocitymotors.carbooking.messaging;

import com.velocitymotors.carbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankTransferCancellationJob {

    private final BookingService bookingService;

    @Scheduled(cron = "${bank-transfer-payment.cancellation-schedule}")
    public void cancelExpiredBankTransferBookings() {
        bookingService.cancelExpiredBankTransferBookings();
    }
}
