package com.example.appointment.service;

import com.example.appointment.dto.AppointmentConflictDTO;
import com.example.appointment.dto.TimeConflictAnalysisDTO;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.User;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预约冲突检测服务
 * 提供智能的预约时间冲突检测和提醒功能
 */
@Service
public class AppointmentConflictService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 检测预约时间冲突
     * @param patientId 患者ID
     * @param doctorId 医生ID
     * @param appointmentTime 预约时间
     * @return 冲突分析结果
     */
    public TimeConflictAnalysisDTO analyzeTimeConflicts(Long patientId, Long doctorId, LocalDateTime appointmentTime) {
        TimeConflictAnalysisDTO analysis = new TimeConflictAnalysisDTO();
        
        // 1. 检查患者时间冲突
        List<AppointmentConflictDTO> patientConflicts = checkPatientConflicts(patientId, appointmentTime);
        analysis.setPatientConflicts(patientConflicts);
        
        // 2. 检查医生时间冲突
        List<AppointmentConflictDTO> doctorConflicts = checkDoctorConflicts(doctorId, appointmentTime);
        analysis.setDoctorConflicts(doctorConflicts);
        
        // 3. 检查时间段可用性
        boolean isTimeSlotAvailable = checkTimeSlotAvailability(doctorId, appointmentTime);
        analysis.setTimeSlotAvailable(isTimeSlotAvailable);
        
        // 4. 提供智能建议
        List<String> suggestions = generateSmartSuggestions(patientId, doctorId, appointmentTime, analysis);
        analysis.setSuggestions(suggestions);
        
        // 5. 计算冲突风险等级
        String riskLevel = calculateConflictRiskLevel(analysis);
        analysis.setRiskLevel(riskLevel);
        
        return analysis;
    }
    
    /**
     * 检查患者时间冲突
     */
    private List<AppointmentConflictDTO> checkPatientConflicts(Long patientId, LocalDateTime appointmentTime) {
        List<AppointmentConflictDTO> conflicts = new ArrayList<>();
        
        // 获取患者当天的所有预约
        LocalDate appointmentDate = appointmentTime.toLocalDate();
        List<Appointment> patientAppointments = appointmentRepository
                .findPatientAppointmentsByDateAndStatus(
                        patientId, appointmentDate, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : patientAppointments) {
            // 检查时间间隔（至少间隔30分钟）
            long minutesDiff = Math.abs(ChronoUnit.MINUTES.between(appointmentTime, appointment.getAppointmentTime()));
            if (minutesDiff < 30) {
                AppointmentConflictDTO conflict = new AppointmentConflictDTO();
                conflict.setConflictType("PATIENT_TIME_CONFLICT");
                conflict.setConflictDescription("与您的其他预约时间冲突");
                conflict.setConflictingAppointmentId(appointment.getId());
                conflict.setConflictingTime(appointment.getAppointmentTime());
                conflict.setTimeDifference(minutesDiff);
                conflict.setSeverity("HIGH");
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检查医生时间冲突
     */
    private List<AppointmentConflictDTO> checkDoctorConflicts(Long doctorId, LocalDateTime appointmentTime) {
        List<AppointmentConflictDTO> conflicts = new ArrayList<>();
        
        // 获取医生当天的所有预约
        LocalDate appointmentDate = appointmentTime.toLocalDate();
        List<Appointment> doctorAppointments = appointmentRepository
                .findDoctorAppointmentsByDateAndStatus(
                        doctorId, appointmentDate, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : doctorAppointments) {
            // 检查时间间隔（每个预约至少需要15分钟间隔）
            long minutesDiff = Math.abs(ChronoUnit.MINUTES.between(appointmentTime, appointment.getAppointmentTime()));
            if (minutesDiff < 15) {
                AppointmentConflictDTO conflict = new AppointmentConflictDTO();
                conflict.setConflictType("DOCTOR_TIME_CONFLICT");
                conflict.setConflictDescription("医生在该时间段已有其他预约");
                conflict.setConflictingAppointmentId(appointment.getId());
                conflict.setConflictingTime(appointment.getAppointmentTime());
                conflict.setTimeDifference(minutesDiff);
                conflict.setSeverity("HIGH");
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检查时间段可用性
     */
    private boolean checkTimeSlotAvailability(Long doctorId, LocalDateTime appointmentTime) {
        LocalTime time = appointmentTime.toLocalTime();
        
        // 检查是否在工作时间内
        boolean isWorkingHours = (time.isAfter(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(12, 0))) ||
                                (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(17, 0)));
        
        if (!isWorkingHours) {
            return false;
        }
        
        // 检查医生是否有排班
        return hasDoctorSchedule(doctorId, appointmentTime);
    }
    
    /**
     * 生成智能建议
     */
    private List<String> generateSmartSuggestions(Long patientId, Long doctorId, LocalDateTime appointmentTime, 
                                                  TimeConflictAnalysisDTO analysis) {
        List<String> suggestions = new ArrayList<>();
        
        // 1. 基于冲突类型提供建议
        if (!analysis.getPatientConflicts().isEmpty()) {
            suggestions.add("建议您选择其他时间段，避免与个人其他预约冲突");
        }
        
        if (!analysis.getDoctorConflicts().isEmpty()) {
            suggestions.add("该医生在此时间段已有预约，建议选择其他时间或医生");
        }
        
        if (!analysis.isTimeSlotAvailable()) {
            suggestions.add("所选时间不在工作时间内，请选择工作时间（8:00-12:00，14:00-17:00）");
        }
        
        // 2. 推荐最佳预约时间
        List<LocalDateTime> recommendedTimes = findRecommendedTimes(patientId, doctorId, appointmentTime);
        if (!recommendedTimes.isEmpty()) {
            suggestions.add("推荐预约时间：" + formatRecommendedTimes(recommendedTimes));
        }
        
        // 3. 提供替代医生建议
        List<Doctor> alternativeDoctors = findAlternativeDoctors(doctorId, appointmentTime);
        if (!alternativeDoctors.isEmpty()) {
            suggestions.add("同科室其他可用医生：" + formatDoctorNames(alternativeDoctors));
        }
        
        return suggestions;
    }
    
    /**
     * 查找推荐的预约时间
     */
    private List<LocalDateTime> findRecommendedTimes(Long patientId, Long doctorId, LocalDateTime targetTime) {
        List<LocalDateTime> recommendations = new ArrayList<>();
        
        // 检查目标时间前后30分钟内的可用时间段
        for (int offset = -30; offset <= 30; offset += 15) {
            if (offset == 0) continue; // 跳过目标时间本身
            
            LocalDateTime candidateTime = targetTime.plusMinutes(offset);
            
            // 检查该时间是否可用
            boolean isPatientAvailable = checkPatientConflicts(patientId, candidateTime).isEmpty();
            boolean isDoctorAvailable = checkDoctorConflicts(doctorId, candidateTime).isEmpty();
            boolean isTimeSlotAvailable = checkTimeSlotAvailability(doctorId, candidateTime);
            
            if (isPatientAvailable && isDoctorAvailable && isTimeSlotAvailable) {
                recommendations.add(candidateTime);
                if (recommendations.size() >= 3) { // 最多推荐3个时间
                    break;
                }
            }
        }
        
        return recommendations;
    }
    
    /**
     * 查找替代医生
     */
    private List<Doctor> findAlternativeDoctors(Long currentDoctorId, LocalDateTime appointmentTime) {
        // 获取当前医生的科室
        Doctor currentDoctor = doctorRepository.findById(currentDoctorId).orElse(null);
        if (currentDoctor == null) {
            return Collections.emptyList();
        }
        
        // 查找同科室的其他医生
        List<Doctor> departmentDoctors = doctorRepository.findByDepartmentIdAndIdNot(
                currentDoctor.getDepartment().getId(), currentDoctorId);
        
        // 筛选出在目标时间可用的医生
        return departmentDoctors.stream()
                .filter(doctor -> {
                    boolean hasConflict = !checkDoctorConflicts(doctor.getId(), appointmentTime).isEmpty();
                    boolean hasSchedule = hasDoctorSchedule(doctor.getId(), appointmentTime);
                    return !hasConflict && hasSchedule;
                })
                .limit(3) // 最多推荐3个医生
                .collect(Collectors.toList());
    }
    
    /**
     * 计算冲突风险等级
     */
    private String calculateConflictRiskLevel(TimeConflictAnalysisDTO analysis) {
        int conflictScore = 0;
        
        // 患者冲突评分
        conflictScore += analysis.getPatientConflicts().size() * 3;
        
        // 医生冲突评分
        conflictScore += analysis.getDoctorConflicts().size() * 2;
        
        // 时间段不可用评分
        if (!analysis.isTimeSlotAvailable()) {
            conflictScore += 5;
        }
        
        // 根据评分确定风险等级
        if (conflictScore >= 8) {
            return "HIGH";
        } else if (conflictScore >= 4) {
            return "MEDIUM";
        } else if (conflictScore >= 1) {
            return "LOW";
        } else {
            return "NONE";
        }
    }
    
    /**
     * 检查医生是否有排班
     */
    private boolean hasDoctorSchedule(Long doctorId, LocalDateTime appointmentTime) {
        // 这里需要实现具体的排班检查逻辑
        // 暂时返回true，后续可以集成DoctorScheduleService
        return true;
    }
    
    /**
     * 格式化推荐时间
     */
    private String formatRecommendedTimes(List<LocalDateTime> times) {
        return times.stream()
                .map(time -> time.format(java.time.format.DateTimeFormatter.ofPattern("MM月dd日 HH:mm")))
                .collect(Collectors.joining("、"));
    }
    
    /**
     * 格式化医生姓名
     */
    private String formatDoctorNames(List<Doctor> doctors) {
        return doctors.stream()
                .map(Doctor::getName)
                .collect(Collectors.joining("、"));
    }

    /**
     * 获取智能建议
     */
    public Map<String, Object> getSmartSuggestions(Long patientId, LocalDateTime preferredTime) {
        Map<String, Object> result = new HashMap<>();
        List<String> suggestions = new ArrayList<>();
        
        // 获取患者即将到来的预约
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<Appointment> upcomingAppointments = appointmentRepository
                .findPatientAppointmentsInTimeRangeWithStatus(
                        patientId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        
        if (!upcomingAppointments.isEmpty()) {
            suggestions.add("您在24小时内已有预约，建议合理安排时间");
        }
        
        result.put("suggestions", suggestions);
        result.put("hasConflict", !upcomingAppointments.isEmpty());
        return result;
    }

    /**
     * 获取患者的所有冲突分析
     */
    public List<TimeConflictAnalysisDTO> getPatientConflictAnalysis(Long patientId) {
        List<TimeConflictAnalysisDTO> analyses = new ArrayList<>();
        
        // 获取患者所有待确认或已确认的预约
        List<Appointment> appointments = appointmentRepository
                .findByPatientIdAndStatusIn(patientId, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : appointments) {
            TimeConflictAnalysisDTO analysis = analyzeTimeConflicts(
                    patientId, appointment.getDoctor().getId(), appointment.getAppointmentTime());
            analyses.add(analysis);
        }
        
        return analyses;
    }

    /**
     * 检查指定预约的冲突状态
     */
    public TimeConflictAnalysisDTO checkAppointmentConflicts(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return new TimeConflictAnalysisDTO();
        }
        
        return analyzeTimeConflicts(
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime());
    }
    
    /**
     * 获取智能提醒消息
     */
    public List<String> getSmartReminders(Long patientId) {
        List<String> reminders = new ArrayList<>();
        
        // 获取患者即将到来的预约
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<Appointment> upcomingAppointments = appointmentRepository
                .findPatientAppointmentsInTimeRangeWithStatus(
                        patientId, now, tomorrow, Arrays.asList("PENDING", "CONFIRMED"));
        
        for (Appointment appointment : upcomingAppointments) {
            long hoursUntilAppointment = ChronoUnit.HOURS.between(now, appointment.getAppointmentTime());
            
            if (hoursUntilAppointment <= 2 && hoursUntilAppointment > 0) {
                reminders.add(String.format("您预约的%s医生将在%d小时后就诊，请提前准备",
                        appointment.getDoctor().getName(), hoursUntilAppointment));
            }
        }
        
        return reminders;
    }
}