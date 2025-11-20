package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.AppointmentDTO;
import com.example.appointment.dto.AppointmentResultDTO;
import com.example.appointment.service.AppointmentService;
import com.example.appointment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final UserService userService;

    @PostMapping
    @RequireRole({"STUDENT", "TEACHER"})
    public Result<AppointmentResultDTO> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        // 获取当前登录用户
        appointmentDTO.setPatientId(userService.getCurrentUser().getId());
        return Result.success(appointmentService.createAppointment(appointmentDTO));
    }

    @PutMapping("/{id}/status")
    @RequireRole({"ADMIN", "DOCTOR"})
    public Result<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        String status = requestBody.get("status");
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        return Result.success(appointmentService.updateAppointmentStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"STUDENT", "TEACHER"})
    public Result<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @RequireRole({"STUDENT", "TEACHER", "ADMIN", "DOCTOR"})
    public Result<AppointmentDTO> getAppointment(@PathVariable Long id) {
        return Result.success(appointmentService.getAppointment(id));
    }

    @GetMapping("/patient")
    @RequireRole({"STUDENT", "TEACHER"})
    public Result<Page<AppointmentDTO>> getPatientAppointments(
            @RequestParam(required = false) String date,
            Pageable pageable) {
        // 获取当前登录用户
        Long patientId = userService.getCurrentUser().getId();
        
        // 如果有指定日期，使用按日期筛选的方法
        if (date != null && !date.isEmpty()) {
            try {
                LocalDate localDate = LocalDate.parse(date);
                return Result.success(appointmentService.getPatientAppointmentsByDate(patientId, localDate, pageable));
            } catch (Exception e) {
                log.warn("Invalid date format: {}", date);
            }
        }
        
        // 默认返回所有预约
        return Result.success(appointmentService.getPatientAppointments(patientId, pageable));
    }

    @GetMapping("/doctor/{doctorId}")
    @RequireRole({"ADMIN", "DOCTOR"})
    public Result<Page<AppointmentDTO>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        
        LocalDate localDate = null;
        if (date != null && !date.isEmpty()) {
            try {
                localDate = LocalDate.parse(date);
            } catch (Exception e) {
                log.warn("Invalid date format: {}", date);
            }
        }
        
        return Result.success(appointmentService.getDoctorAppointments(
            doctorId, 
            localDate, 
            status, 
            pageable
        ));
    }

    @GetMapping("/department/{departmentId}")
    @RequireRole({"ADMIN"})
    public Result<Page<AppointmentDTO>> getDepartmentAppointments(
            @PathVariable Long departmentId,
            Pageable pageable) {
        return Result.success(appointmentService.getDepartmentAppointments(departmentId, pageable));
    }

    @GetMapping("/doctor/{doctorId}/availability")
    public Result<Boolean> checkDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam LocalDateTime appointmentTime) {
        return Result.success(appointmentService.isDoctorAvailable(doctorId, appointmentTime));
    }

    @GetMapping("/doctor/{doctorId}/schedule")
    public Result<List<AppointmentDTO>> getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return Result.success(appointmentService.getDoctorAppointmentsByTimeRange(doctorId, startTime, endTime));
    }

    @GetMapping("/today/count")
    @RequireRole({"ADMIN"})
    public Result<Long> getTodayAppointmentsCount() {
        return Result.success(appointmentService.getTodayAppointmentsCount());
    }

    @PostMapping("/{id}/cancel")
    @RequireRole({"STUDENT", "TEACHER"})
    public Result<Void> cancelAppointmentRequest(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return Result.success();
    }
}