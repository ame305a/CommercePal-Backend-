package com.commerce.pal.backend.module.ScheduledReportService;

import com.commerce.pal.backend.module.database.UserDatabaseService;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Logger;

@Service
public class ScheduledUserReportService {

    private final UserDatabaseService userDatabaseService;
    private final SlackService slackService;

    private static final Logger log = Logger.getLogger(ScheduledUserReportService.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ScheduledUserReportService(UserDatabaseService userDatabaseService, SlackService slackService) {
        this.userDatabaseService = userDatabaseService;
        this.slackService = slackService;
    }

    @Scheduled(cron = "0 0 6 * * ?") // Runs every day at 6 AM
    @Async
    public void scheduleReportGeneration() {
        LocalDate today = LocalDate.now();

        // Always generate daily report for yesterday
        LocalDate yesterday = today.minusDays(1);
        JSONObject dailyReport = generateDailyReport(yesterday);
        String dailyReportDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        slackService.sendUserReport("Daily", dailyReportDate, dailyReport);

        // If it's Monday, also get the report for the last week (Monday to Sunday)
        if (today.getDayOfWeek().getValue() == 1) { // 1 means Monday
            LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            LocalDate lastSunday = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);
            String weeklyReportDate = String.format("%s - %s", lastMonday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    lastSunday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            JSONObject weeklyReport = generateWeeklyReport(lastMonday, lastSunday);
            slackService.sendUserReport("Weekly", weeklyReportDate, weeklyReport);
        }

        // If it's the first day of the month, also get last month's report
        if (today.getDayOfMonth() == 1) {
            JSONObject monthlyReport = generateMonthlyReport(today);
            String monthName = today.minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            int lastMonthYear = today.minusMonths(1).getYear();
            String monthlyReportDate = String.format("%s, %d", monthName, lastMonthYear);
            slackService.sendUserReport("Monthly", monthlyReportDate, monthlyReport);
        }
    }

    private JSONObject generateDailyReport(LocalDate yesterday) {
        String startDate = yesterday.format(formatter);
        String endDate = yesterday.format(formatter);

        log.info("Generating daily report for " + startDate);
        return userDatabaseService.callUserReportService(startDate, endDate, "Daily Report");
    }

    private JSONObject generateWeeklyReport(LocalDate lastMonday, LocalDate lastSunday) {
        String startDate = lastMonday.format(formatter);
        String endDate = lastSunday.format(formatter);

        log.info("Generating weekly report from " + startDate + " to " + endDate);
        return userDatabaseService.callUserReportService(startDate, endDate, "Weekly Report");
    }

    private JSONObject generateMonthlyReport(LocalDate today) {
        // Get the first day and last day of last month
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

        String startDate = firstDayOfLastMonth.format(formatter);
        String endDate = lastDayOfLastMonth.format(formatter);

        log.info("Generating monthly report from " + startDate + " to " + endDate);
        return userDatabaseService.callUserReportService(startDate, endDate, "Monthly Report");
    }

}
