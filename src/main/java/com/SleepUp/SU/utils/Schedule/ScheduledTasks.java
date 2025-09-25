package com.SleepUp.SU.utils.Schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Scheduled(cron = "0 0 2 * * *")
    public void dailyMaintenanceTask() {
        log.info("Daily maintenance task started at: {}",
                LocalDateTime.now().format(DATE_TIME_FORMATTER));

        log.info("Daily maintenance task completed at: {}",
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void hourlySystemCheck() {
        log.debug("Hourly system check executed at: {}",
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    @Scheduled(cron = "0 0 3 * * SUN")
    public void weeklyReportTask() {
        log.info("Weekly report task started at: {}",
                LocalDateTime.now().format(DATE_TIME_FORMATTER));

        log.info("Weekly report task completed at: {}",
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }
}