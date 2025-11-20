package com.example.appointment.service;

import com.example.appointment.dto.OneStepAppointmentRequest;
import com.example.appointment.dto.OneStepAppointmentResponse;
import com.example.appointment.dto.SymptomAnalysisResponse;
import com.example.appointment.dto.TimeSlotAvailabilityResponse;
import com.example.appointment.entity.*;
import com.example.appointment.repository.*;
import com.example.appointment.entity.User;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.util.TimeSlotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OneStepAppointmentService {
    
    @Autowired
    private SymptomAnalysisService symptomAnalysisService;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;
    
    @Autowired
    private TimeSlotUtil timeSlotUtil;
    
    /**
     * 一键预约核心服务
     */
    @Transactional
    public OneStepAppointmentResponse createOneStepAppointment(OneStepAppointmentRequest request) {
        // 1. 症状分析获取推荐科室
        List<Map<String, Object>> recommendations = symptomAnalysisService.analyzeSymptomsAndRecommendDepartments(request.getSymptoms());
        
        if (recommendations.isEmpty()) {
            throw new RuntimeException("无法根据症状匹配合适的科室，请详细描述症状");
        }
        
        // 2. 选择最佳推荐科室（如果没有指定，使用AI推荐）
        Long departmentId = request.getDepartmentId();
        if (departmentId == null) {
            departmentId = Long.valueOf(recommendations.get(0).get("departmentId").toString());
        }
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("科室不存在"));
        
        // 3. 智能选择医生
        Doctor selectedDoctor = selectBestDoctor(departmentId, request.getAppointmentTime());
        
        // 4. 验证时间段可用性
        LocalDateTime appointmentTime = request.getAppointmentTime();
        String timeSlot = timeSlotUtil.getTimeSlot(appointmentTime);
        
        if (!isTimeSlotAvailable(selectedDoctor.getId(), appointmentTime, timeSlot)) {
            throw new RuntimeException("所选时间段已满，请选择其他时间");
        }
        
        // 5. 获取当前患者（模拟登录用户）
        User patient = getCurrentPatient();
        
        // 6. 创建预约
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(selectedDoctor);
        appointment.setDepartment(department);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setTimeSlot(timeSlot);
        appointment.setSymptoms(request.getSymptoms());
        appointment.setStatus("PENDING");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setCreatedBy("system");
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // 生成预约号
        String appointmentNumber = generateAppointmentNumber();
        appointment.setAppointmentNumber(appointmentNumber);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // 7. 构建响应
        return buildOneStepResponse(savedAppointment, department, selectedDoctor, recommendations.get(0));
    }
    
    /**
     * 智能选择最佳医生
     */
    private Doctor selectBestDoctor(Long departmentId, LocalDateTime appointmentTime) {
        List<Doctor> availableDoctors = doctorRepository.findByDepartmentId(departmentId);
        
        if (availableDoctors.isEmpty()) {
            throw new RuntimeException("该科室暂无可用医生");
        }
        
        // 筛选出指定时间段有排班的医生
        List<Doctor> scheduledDoctors = availableDoctors.stream()
                .filter(doctor -> hasSchedule(doctor.getId(), appointmentTime))
                .collect(Collectors.toList());
        
        if (scheduledDoctors.isEmpty()) {
            throw new RuntimeException("所选时间段该科室无医生排班");
        }
        
        // 按预约数量排序选择最佳医生（预约数量少的更空闲）
        return scheduledDoctors.stream()
                .sorted((d1, d2) -> {
                    return Integer.compare(
                        getAppointmentCount(d1.getId(), appointmentTime.toLocalDate()),
                        getAppointmentCount(d2.getId(), appointmentTime.toLocalDate())
                    );
                })
                .findFirst()
                .orElse(scheduledDoctors.get(0));
    }
    
    /**
     * 检查医生是否有排班
     */
    private boolean hasSchedule(Long doctorId, LocalDateTime appointmentTime) {
        LocalDate scheduleDate = appointmentTime.toLocalDate();
        String timeSlot = timeSlotUtil.getTimeSlot(appointmentTime);
        
        return doctorScheduleRepository.checkDoctorScheduleAvailability(
                doctorId, scheduleDate, timeSlot, 1);
    }
    
    /**
     * 获取医生指定日期的预约数量
     */
    private int getAppointmentCount(Long doctorId, LocalDate date) {
        long count = appointmentRepository.countDoctorAppointmentsInTimeRangeWithStatus(
                doctorId, 
                date.atStartOfDay(), 
                date.plusDays(1).atStartOfDay(),
                Arrays.asList("PENDING", "CONFIRMED"));
        return (int) count;
    }
    
    /**
     * 检查时间段是否可用
     */
    private boolean isTimeSlotAvailable(Long doctorId, LocalDateTime appointmentTime, String timeSlot) {
        // 获取医生在该时间段的预约数量
        LocalDate date = appointmentTime.toLocalDate();
        long count = appointmentRepository.countDoctorAppointmentsInTimeRangeWithStatus(
                doctorId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                Arrays.asList("PENDING", "CONFIRMED"));
        
        // 获取医生在该时间段的最大预约数（假设每个时间段最多4个预约）
        int maxAppointmentsPerSlot = 4;
        
        return (int)count < maxAppointmentsPerSlot;
    }
    
    /**
     * 生成预约号
     */
    private String generateAppointmentNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "A" + datePrefix + randomSuffix;
    }
    
    /**
     * 获取当前患者（模拟）
     */
    private User getCurrentPatient() {
        // 这里应该从SecurityContext获取当前登录用户
        // 暂时返回第一个患者用户（STUDENT或TEACHER角色）
        return userRepository.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole()) || "TEACHER".equals(u.getRole()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到患者信息"));
    }
    
    /**
     * 构建一键预约响应
     */
    private OneStepAppointmentResponse buildOneStepResponse(
            Appointment appointment, 
            Department department, 
            Doctor doctor,
            Map<String, Object> recommendation) {
        
        OneStepAppointmentResponse response = new OneStepAppointmentResponse();
        response.setAppointmentId(appointment.getId());
        response.setAppointmentNumber(appointment.getAppointmentNumber());
        response.setDepartmentName(department.getName());
        response.setDoctorName(doctor.getName());
        response.setDoctorTitle(doctor.getTitle());
        response.setAppointmentDate(appointment.getAppointmentTime().toLocalDate().toString());
        response.setAppointmentTime(appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.setFormattedAppointmentTime(formatDateTime(appointment.getAppointmentTime()));
        response.setLocation(department.getLocation());
        
        // AI推荐理由
        String aiReason = buildAIRecommendationReason(recommendation, department, doctor);
        response.setAiRecommendationReason(aiReason);
        
        return response;
    }
    
    /**
     * 构建AI推荐理由
     */
    private String buildAIRecommendationReason(Map<String, Object> recommendation, 
                                              Department department, Doctor doctor) {
        StringBuilder reason = new StringBuilder();
        reason.append("根据您的症状描述，AI智能分析推荐").append(department.getName()).append("，");
        reason.append("匹配度").append(recommendation.get("matchScore")).append("%。");
        reason.append("为您安排").append(doctor.getName()).append("医生（").append(doctor.getTitle()).append("），");
        reason.append("擅长").append(doctor.getSpecialty()).append("。");
        return reason.toString();
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"));
    }
    
    /**
     * 获取时间段可用性
     */
    public TimeSlotAvailabilityResponse getTimeSlotAvailability(Long departmentId, 
                                                                LocalDate date, 
                                                                String period) {
        
        TimeSlotAvailabilityResponse response = new TimeSlotAvailabilityResponse();
        response.setDepartmentId(departmentId);
        response.setDate(date);
        response.setPeriod(period);
        
        // 生成时间段
        List<TimeSlotAvailabilityResponse.TimeSlot> timeSlots = new ArrayList<>();
        
        if ("MORNING".equals(period)) {
            // 上午时间段
            for (int hour = 9; hour <= 11; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    String time = String.format("%02d:%02d", hour, minute);
                    int available = calculateAvailableSlots(departmentId, date, time);
                    timeSlots.add(new TimeSlotAvailabilityResponse.TimeSlot(time, available));
                }
            }
        } else if ("AFTERNOON".equals(period)) {
            // 下午时间段
            for (int hour = 14; hour <= 16; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    String time = String.format("%02d:%02d", hour, minute);
                    int available = calculateAvailableSlots(departmentId, date, time);
                    timeSlots.add(new TimeSlotAvailabilityResponse.TimeSlot(time, available));
                }
            }
        }
        
        response.setTimeSlots(timeSlots);
        return response;
    }
    
    /**
     * 获取科室数量
     */
    public long getDepartmentCount() {
        return departmentRepository.count();
    }
    
    /**
     * 获取医生数量
     */
    public long getDoctorCount() {
        return doctorRepository.count();
    }
    
    /**
     * AI模型是否可用
     */
    public boolean isAIModelAvailable() {
        try {
            // 测试AI模型是否可用
            List<Map<String, Object>> recommendations = symptomAnalysisService.analyzeSymptomsAndRecommendDepartments("测试症状");
            return recommendations != null && !recommendations.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 计算时间段可用数量
     */
    private int calculateAvailableSlots(Long departmentId, LocalDate date, String time) {
        // 获取科室下所有医生
        List<Doctor> doctors = doctorRepository.findByDepartmentId(departmentId);
        
        int totalAvailable = 0;
        for (Doctor doctor : doctors) {
            // 检查医生是否有排班
            if (hasSchedule(doctor.getId(), date.atTime(java.time.LocalTime.parse(time)))) {
                // 检查时间段是否可用（假设每个医生每个时间段最多4个预约）
                int appointmentCount = getAppointmentCount(doctor.getId(), date);
                int maxPerDoctor = 4;
                int availableForDoctor = Math.max(0, maxPerDoctor - appointmentCount);
                totalAvailable += availableForDoctor;
            }
        }
        
        return totalAvailable;
    }
}