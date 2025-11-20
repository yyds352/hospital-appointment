package com.example.appointment.controller;

import com.example.appointment.dto.TimeConflictAnalysisDTO;
import com.example.appointment.service.AppointmentConflictService;
import com.example.appointment.service.SmartReminderService;
import com.example.appointment.common.Result;
import com.example.appointment.dto.AppointmentConflictDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conflicts")
@RequiredArgsConstructor
public class AppointmentConflictController {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentConflictController.class);

    private final AppointmentConflictService appointmentConflictService;
    private final SmartReminderService smartReminderService;

    /**
     * 分析预约时间冲突
     */
    @GetMapping("/analyze/{patientId}")
    public Result<TimeConflictAnalysisDTO> analyzeTimeConflicts(
            @PathVariable Long patientId,
            @RequestParam Long doctorId,
            @RequestParam LocalDateTime appointmentTime) {
        
        log.info("Analyzing time conflicts for patient: {} at time: {}", patientId, appointmentTime);
        
        try {
            TimeConflictAnalysisDTO analysis = appointmentConflictService.analyzeTimeConflicts(patientId, doctorId, appointmentTime);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("Error analyzing time conflicts", e);
            return Result.error("分析时间冲突失败: " + e.getMessage());
        }
    }

    /**
     * 获取患者的所有冲突分析
     */
    @GetMapping("/patient/{patientId}")
    public Result<List<TimeConflictAnalysisDTO>> getPatientConflictAnalysis(@PathVariable Long patientId) {
        log.info("Getting conflict analysis for patient: {}", patientId);
        
        try {
            List<TimeConflictAnalysisDTO> analysisList = appointmentConflictService.getPatientConflictAnalysis(patientId);
            return Result.success(analysisList);
        } catch (Exception e) {
            log.error("Error getting patient conflict analysis", e);
            return Result.error("获取患者冲突分析失败: " + e.getMessage());
        }
    }

    /**
     * 检查指定预约的冲突状态
     */
    @GetMapping("/appointment/{appointmentId}")
    public Result<TimeConflictAnalysisDTO> checkAppointmentConflicts(@PathVariable Long appointmentId) {
        log.info("Checking conflicts for appointment: {}", appointmentId);
        
        try {
            TimeConflictAnalysisDTO analysis = appointmentConflictService.checkAppointmentConflicts(appointmentId);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("Error checking appointment conflicts", e);
            return Result.error("检查预约冲突失败: " + e.getMessage());
        }
    }

    /**
     * 获取智能建议
     */
    @GetMapping("/suggestions/{patientId}")
    public Result<Map<String, Object>> getSmartSuggestions(
            @PathVariable Long patientId,
            @RequestParam LocalDateTime preferredTime) {
        
        log.info("Getting smart suggestions for patient: {} at preferred time: {}", patientId, preferredTime);
        
        try {
            Map<String, Object> suggestions = appointmentConflictService.getSmartSuggestions(patientId, preferredTime);
            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("Error getting smart suggestions", e);
            return Result.error("获取智能建议失败: " + e.getMessage());
        }
    }

    /**
     * 手动发送预约提醒
     */
    @PostMapping("/reminders/{appointmentId}")
    public Result<String> sendAppointmentReminder(@PathVariable Long appointmentId) {
        log.info("Manually sending reminder for appointment: {}", appointmentId);
        
        try {
            smartReminderService.sendAppointmentReminder(appointmentId);
            return Result.success("提醒发送成功");
        } catch (Exception e) {
            log.error("Error sending appointment reminder", e);
            return Result.error("发送提醒失败: " + e.getMessage());
        }
    }

    /**
     * 批量发送即将到来的预约提醒
     */
    @PostMapping("/reminders/batch")
    public Result<Map<String, Object>> sendUpcomingReminders() {
        log.info("Sending batch upcoming reminders");
        
        try {
            Map<String, Object> result = smartReminderService.sendUpcomingAppointmentReminders();
            return Result.success(result);
        } catch (Exception e) {
            log.error("Error sending batch reminders", e);
            return Result.error("批量发送提醒失败: " + e.getMessage());
        }
    }

    /**
     * 获取预约统计信息
     */
    @GetMapping("/statistics/{patientId}")
    public Result<Map<String, Object>> getAppointmentStatistics(@PathVariable Long patientId) {
        log.info("Getting appointment statistics for patient: {}", patientId);
        
        try {
            Map<String, Object> statistics = smartReminderService.getAppointmentStatistics(patientId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("Error getting appointment statistics", e);
            return Result.error("获取预约统计失败: " + e.getMessage());
        }
    }
}