package com.example.appointment.controller;

import com.example.appointment.dto.OneStepAppointmentRequest;
import com.example.appointment.dto.OneStepAppointmentResponse;
import com.example.appointment.dto.TimeSlotAvailabilityResponse;
import com.example.appointment.service.OneStepAppointmentService;
import com.example.appointment.entity.User;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/one-step")
public class OneStepAppointmentController {
    
    @Autowired
    private OneStepAppointmentService oneStepAppointmentService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 一键预约接口
     */
    @PostMapping("/appointment")
    public ResponseEntity<ApiResponse<OneStepAppointmentResponse>> createOneStepAppointment(
            @RequestBody OneStepAppointmentRequest request) {
        
        try {
            // 验证请求参数
            if (request.getSymptoms() == null || request.getSymptoms().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("症状描述不能为空"));
            }
            
            if (request.getAppointmentTime() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("预约时间不能为空"));
            }
            
            // 创建一键预约
            OneStepAppointmentResponse response = oneStepAppointmentService.createOneStepAppointment(request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "一键预约成功！AI已为您智能匹配最适合的科室和医生"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取时间段可用性
     */
    @GetMapping("/time-slots/availability")
    public ResponseEntity<ApiResponse<TimeSlotAvailabilityResponse>> getTimeSlotAvailability(
            @RequestParam Long departmentId,
            @RequestParam String date,
            @RequestParam String period) {
        
        try {
            LocalDate appointmentDate = LocalDate.parse(date);
            TimeSlotAvailabilityResponse response = oneStepAppointmentService.getTimeSlotAvailability(
                    departmentId, appointmentDate, period);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取患者信息（用于一键预约页面）
     */
    @GetMapping("/patient-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientInfo() {
        try {
            // 模拟获取当前登录患者信息
            User user = userRepository.findAll().stream()
                    .filter(u -> "STUDENT".equals(u.getRole()) || "TEACHER".equals(u.getRole()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到患者信息"));
            
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("name", user.getName());
            patientInfo.put("phone", user.getPhone());
            
            return ResponseEntity.ok(ApiResponse.success(patientInfo));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 验证一键预约条件
     */
    @GetMapping("/validation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateOneStepAppointment() {
        try {
            Map<String, Object> validationResult = new HashMap<>();
            
            // 检查是否有足够的科室
            long departmentCount = oneStepAppointmentService.getDepartmentCount();
            validationResult.put("hasSufficientDepartments", departmentCount > 0);
            validationResult.put("departmentCount", departmentCount);
            
            // 检查是否有足够的医生
            long doctorCount = oneStepAppointmentService.getDoctorCount();
            validationResult.put("hasSufficientDoctors", doctorCount > 0);
            validationResult.put("doctorCount", doctorCount);
            
            // 检查AI模型是否可用
            boolean aiModelAvailable = oneStepAppointmentService.isAIModelAvailable();
            validationResult.put("aiModelAvailable", aiModelAvailable);
            
            // 总体可用性
            boolean overallAvailable = departmentCount > 0 && doctorCount > 0 && aiModelAvailable;
            validationResult.put("available", overallAvailable);
            
            if (!overallAvailable) {
                validationResult.put("message", "一键预约功能暂时不可用，请联系管理员");
            } else {
                validationResult.put("message", "一键预约功能可用");
            }
            
            return ResponseEntity.ok(ApiResponse.success(validationResult));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}