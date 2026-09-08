package com.tiffin.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReminderScheduler {

    private final PaymentService paymentService;

    /**
     * Runs daily at 9:00 AM on days 25 through 31 of every month to process payment reminders.
     * Cron expression: "0 0 9 25-31 * ?"
     */
    @Scheduled(cron = "0 0 9 25-31 * ?")
    public void scheduleMonthlyPaymentReminders() {
        log.info("Running automatic monthly payment reminder cron job (25th - 31st of month)");
        try {
            var result = paymentService.sendBulkPaymentReminders("CRON_SCHEDULER");
            log.info("Automatic payment reminder completed successfully: {}", result);
        } catch (Exception e) {
            log.error("Error executing monthly payment reminder cron job", e);
        }
    }
}
