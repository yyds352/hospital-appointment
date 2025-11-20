package com.example.appointment.service;

import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.User;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 智能提醒服务
 * 提供预约相关的智能提醒功能
 */
@Service
public class SmartReminderService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取用户的智能提醒
     * @param userId 用户ID
     * @return 提醒消息列表
     */
    public List<String> getSmartReminders(Long userId) {
        List<String> reminders = new ArrayList<>();
        
        // 1. 即将到来的预约提醒
        reminders.addAll(getUpcomingAppointmentReminders(userId));
        
        // 2. 预约冲突提醒
        reminders.addAll(getAppointmentConflictReminders(userId));
        
        // 3. 取消预约提醒
        reminders.addAll(getCancellationReminders(userId));
        
        // 4. 就诊准备提醒
        reminders.addAll(getPreparationReminders(userId));
        
        return reminders;
    }
    
    /**
     * 获取即将到来的预约提醒
     */
    private List<String> getUpcomingAppointmentReminders(Long userId) {
        List<String> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 获取未来24小时内的预约
        LocalDateTime tomorrow = now.plusDays(1);
        List<Appointment> upcomingAppointments = appointmentRepository
                .findPatientAppointmentsInTimeRangeWithStatus(
                        userId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : upcomingAppointments) {
            long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointment.getAppointmentTime());
            long minutesUntilAppointment = ChronoUnit.MINUTES.between(now, appointment.getAppointmentTime());
            
            // 2小时前提醒
            if (hoursUntilAppointment == 2) {
                reminders.add(String.format("⏰ 您预约的%s医生将在2小时后就诊（%s），请提前准备",
                        appointment.getDoctor().getName(),
                        appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("MM月dd日 HH:mm"))));
            }
            
            // 30分钟前提醒
            if (minutesUntilAppointment == 30) {
                reminders.add(String.format("⚡ 您预约的%s医生将在30分钟后就诊，请立即前往医院",
                        appointment.getDoctor().getName()));
            }
            
            // 当天早上提醒（9点前）
            if (now.getHour() < 9 && appointment.getAppointmentTime().toLocalDate().equals(now.toLocalDate())) {
                reminders.add(String.format("📅 今天您有预约：%s医生（%s），请记得按时就诊",
                        appointment.getDoctor().getName(),
                        appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))));
            }
        }
        
        return reminders;
    }
    
    /**
     * 获取预约冲突提醒
     */
    private List<String> getAppointmentConflictReminders(Long userId) {
        List<String> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        // 获取用户未来24小时内的所有预约
        List<Appointment> userAppointments = appointmentRepository
                .findPatientAppointmentsInTimeRangeWithStatus(
                        userId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        
        // 检查时间冲突
        for (int i = 0; i < userAppointments.size(); i++) {
            for (int j = i + 1; j < userAppointments.size(); j++) {
                Appointment appointment1 = userAppointments.get(i);
                Appointment appointment2 = userAppointments.get(j);
                
                long minutesDiff = Math.abs(java.time.temporal.ChronoUnit.MINUTES.between(
                        appointment1.getAppointmentTime(), appointment2.getAppointmentTime()));
                
                if (minutesDiff < 30) {
                    reminders.add(String.format("⚠️ 您的两个预约时间冲突：%s（%s）和%s（%s），建议调整其中一个",
                            appointment1.getDoctor().getName(),
                            appointment1.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                            appointment2.getDoctor().getName(),
                            appointment2.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))));
                }
            }
        }
        
        return reminders;
    }
    
    /**
     * 获取取消预约提醒
     */
    private List<String> getCancellationReminders(Long userId) {
        List<String> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 获取已取消但就诊时间未到的预约
        List<Appointment> cancelledAppointments = appointmentRepository
                .findPatientFutureAppointmentsByStatus(userId, now, "CANCELLED");
        
        for (Appointment appointment : cancelledAppointments) {
            long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointment.getAppointmentTime());
            
            if (hoursUntilAppointment > 0 && hoursUntilAppointment <= 24) {
                reminders.add(String.format("🔄 您取消了%s的预约，如需重新预约请尽快操作",
                        appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("MM月dd日 HH:mm"))));
            }
        }
        
        return reminders;
    }
    
    /**
     * 获取就诊准备提醒
     */
    private List<String> getPreparationReminders(Long userId) {
        List<String> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        // 获取即将就诊的预约
        List<Appointment> upcomingAppointments = appointmentRepository
                .findPatientAppointmentsInTimeRangeWithStatus(
                        userId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : upcomingAppointments) {
            long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointment.getAppointmentTime());
            
            // 就诊前1天提醒准备事项
            if (hoursUntilAppointment <= 24 && hoursUntilAppointment > 20) {
                reminders.add(String.format("📋 明天您将就诊%s医生，请准备好相关病历和检查报告",
                        appointment.getDoctor().getName()));
            }
            
            // 就诊当天早上提醒
            if (now.getHour() < 9 && appointment.getAppointmentTime().toLocalDate().equals(now.toLocalDate())) {
                reminders.add(String.format("🏥 今天就诊提醒：请携带身份证、医保卡，提前30分钟到达医院（%s医生）",
                        appointment.getDoctor().getName()));
            }
        }
        
        return reminders;
    }
    
    /**
     * 发送即将到来的预约提醒
     */
    public Map<String, Object> sendUpcomingAppointmentReminders() {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);
        
        // 获取未来24小时内需要提醒的预约
        List<Appointment> appointmentsToRemind = appointmentRepository
                .findAppointmentsInTimeRangeWithStatusAndNotReminded(
                        now, next24Hours, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : appointmentsToRemind) {
            try {
                sendReminderNotification(appointment);
                appointment.setReminded(true);
                appointmentRepository.save(appointment);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                e.printStackTrace();
            }
        }
        
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("totalCount", appointmentsToRemind.size());
        return result;
    }
    
    /**
     * 定时任务：发送预约提醒通知
     * 每30分钟执行一次
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next30Minutes = now.plusMinutes(30);
        
        // 获取未来30分钟内需要提醒的预约
        List<Appointment> appointmentsToRemind = appointmentRepository
                .findAppointmentsInTimeRangeWithStatusAndNotReminded(
                        now, next30Minutes, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : appointmentsToRemind) {
            try {
                sendReminderNotification(appointment);
                appointment.setReminded(true); // 标记已提醒
                appointmentRepository.save(appointment);
            } catch (Exception e) {
                // 记录日志但不影响其他预约的提醒
                System.err.println("发送预约提醒失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 发送提醒通知（可以扩展为短信、邮件、推送等）
     */
    private void sendReminderNotification(Appointment appointment) {
        User patient = appointment.getPatient();
        String message = String.format("您预约的%s医生将在30分钟后就诊，请提前准备",
                appointment.getDoctor().getName());
        
        // 这里可以集成短信服务、邮件服务或推送服务
        System.out.println(String.format("发送提醒通知给患者%s: %s", patient.getName(), message));
        
        // 记录提醒日志
        // TODO: 可以添加提醒日志记录
    }
    
    /**
     * 获取预约统计信息
     */
    public Map<String, Object> getAppointmentStatistics(Long patientId) {
        Map<String, Object> statistics = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime weekLater = now.plusWeeks(1);
        
        // 今日预约数量
        long todayAppointments = appointmentRepository.countPatientAppointmentsInTimeRangeWithStatus(
                patientId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        statistics.put("todayAppointments", todayAppointments);
        
        // 本周预约数量
        long weekAppointments = appointmentRepository.countPatientAppointmentsInTimeRangeWithStatus(
                patientId, now, weekLater, Arrays.asList("PENDING", "CONFIRMED"));
        statistics.put("weekAppointments", weekAppointments);
        
        // 需要提醒的预约数量
        List<Appointment> reminders = appointmentRepository
                .findPatientAppointmentsInTimeRangeWithStatus(
                        patientId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        long needReminderCount = reminders.stream()
                .filter(a -> ChronoUnit.HOURS.between(now, a.getAppointmentTime()) <= 2)
                .count();
        statistics.put("needReminderCount", needReminderCount);
        
        return statistics;
    }

    /**
     * 发送预约提醒（单个预约）
     */
    public void sendAppointmentReminder(Long appointmentId) {
        // TODO: 实现单个预约提醒逻辑
    }

    /**
     * 发送每日预约提醒
     */
    public Map<String, Object> sendDailyAppointmentReminders() {
        // TODO: 实现每日预约提醒逻辑
        return new HashMap<>();
    }

    /**
     * 发送每周预约统计
     */
    public Map<String, Object> sendWeeklyAppointmentStatistics() {
        // TODO: 实现每周预约统计逻辑
        return new HashMap<>();
    }

    /**
     * 清理过期提醒记录
     */
    public int cleanupExpiredReminders() {
        // TODO: 实现清理过期提醒记录逻辑
        return 0;
    }
}