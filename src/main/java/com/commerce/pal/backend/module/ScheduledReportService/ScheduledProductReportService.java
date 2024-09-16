package com.commerce.pal.backend.module.ScheduledReportService;

import com.commerce.pal.backend.module.database.ProductDatabaseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Service
public class ScheduledProductReportService {

    private final ProductDatabaseService productDatabaseService;

    private static final Logger log = Logger.getLogger(ScheduledProductReportService.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ScheduledProductReportService(ProductDatabaseService productDatabaseService) {
        this.productDatabaseService = productDatabaseService;
    }

    @Scheduled(cron = "0 0 6 * * ?") // Runs every day at 6 AM
    public void scheduleReportGeneration() {
        LocalDate today = LocalDate.now();

        // Always generate daily report
        generateDailyReport(today);

        // If it's the first day of the month, also get last month's report
        if (today.getDayOfMonth() == 1) {
            generateMonthlyReport(today);
        }
        // If it's Monday, also get the report for the last week (Monday to Sunday)
        if (today.getDayOfWeek().getValue() == 1) { // 1 means Monday
            generateWeeklyReport(today);
        }
    }

    private void generateDailyReport(LocalDate today) {
        // Get yesterday's date
        LocalDate yesterday = today.minusDays(1);
        String startDate = yesterday.format(formatter);
        String endDate = yesterday.format(formatter);

        log.info("Generating daily report for " + startDate);
        productDatabaseService.callProductReportService(startDate, endDate, "Daily Report");
    }

    private void generateWeeklyReport(LocalDate today) {
        // Get last Monday and last Sunday
        LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate lastSunday = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);

        String startDate = lastMonday.format(formatter);
        String endDate = lastSunday.format(formatter);

        log.info("Generating weekly report from " + startDate + " to " + endDate);
        productDatabaseService.callProductReportService(startDate, endDate, "Weekly Report");
    }

    private void generateMonthlyReport(LocalDate today) {
        // Get the first day and last day of last month
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

        String startDate = firstDayOfLastMonth.format(formatter);
        String endDate = lastDayOfLastMonth.format(formatter);

        log.info("Generating monthly report from " + startDate + " to " + endDate);
        productDatabaseService.callProductReportService(startDate, endDate, "Monthly Report");
    }

}
