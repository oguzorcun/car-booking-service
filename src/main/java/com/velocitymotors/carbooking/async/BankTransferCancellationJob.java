package com.velocitymotors.carbooking.async;

import com.velocitymotors.carbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferCancellationJob {

    private final BookingService bookingService;

    @Scheduled(cron = "${bank-transfer-payment.cancellation-schedule}")
    public void cancelExpiredBankTransferBookings() {
        int cancelled = bookingService.cancelExpiredBankTransferBookings();
        if (cancelled > 0) {
            log.info("Cancelled {} bank transfer bookings pending payment", cancelled);
        }
    }
}
