package com.example.appointment.scheduler;

import com.example.appointment.service.SmartReminderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    private final SmartReminderService smartReminderService;

    /**
     * 每30分钟检查并发送即将到来的预约提醒
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void sendUpcomingAppointmentReminders() {
        log.info("Starting scheduled upcoming appointment reminders check at: {}", LocalDateTime.now());
        
        try {
            Map<String, Object> result = smartReminderService.sendUpcomingAppointmentReminders();
            log.info("Scheduled reminder check completed. Sent reminders: {}, Failed: {}", 
                    result.get("successfulCount"), result.get("failedCount"));
        } catch (Exception e) {
            log.error("Error during scheduled upcoming appointment reminders", e);
        }
    }

    /**
     * 每天上午8点发送当天的预约提醒
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyAppointmentReminders() {
        log.info("Starting daily appointment reminders at: {}", LocalDateTime.now());
        
        try {
            Map<String, Object> result = smartReminderService.sendDailyAppointmentReminders();
            log.info("Daily reminder check completed. Sent reminders: {}, Failed: {}", 
                    result.get("successfulCount"), result.get("failedCount"));
        } catch (Exception e) {
            log.error("Error during daily appointment reminders", e);
        }
    }

    /**
     * 每周一早上9点发送本周的预约统计
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyAppointmentStatistics() {
        log.info("Starting weekly appointment statistics at: {}", LocalDateTime.now());
        
        try {
            Map<String, Object> result = smartReminderService.sendWeeklyAppointmentStatistics();
            log.info("Weekly statistics completed. Sent to {} patients", result.get("patientCount"));
        } catch (Exception e) {
            log.error("Error during weekly appointment statistics", e);
        }
    }

    /**
     * 每天凌晨2点清理过期的提醒记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredReminders() {
        log.info("Starting cleanup of expired reminders at: {}", LocalDateTime.now());
        
        try {
            int cleanedCount = smartReminderService.cleanupExpiredReminders();
            log.info("Cleanup completed. Removed {} expired reminder records", cleanedCount);
        } catch (Exception e) {
            log.error("Error during cleanup of expired reminders", e);
        }
    }
}