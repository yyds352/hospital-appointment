package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.DoctorScheduleDTO;
import com.example.appointment.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class DoctorScheduleController {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorScheduleController.class);

    private final DoctorScheduleService scheduleService;

    @PostMapping
    @RequireRole({"ADMIN", "DOCTOR"})
    public Result<DoctorScheduleDTO> create(@RequestBody @Valid DoctorScheduleDTO scheduleDTO) {
        log.info("创建排班：{}", scheduleDTO);
        return Result.success(scheduleService.create(scheduleDTO));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "DOCTOR"})
    public Result<DoctorScheduleDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid DoctorScheduleDTO scheduleDTO) {
        log.info("更新排班，ID：{}，数据：{}", id, scheduleDTO);
        return Result.success(scheduleService.update(id, scheduleDTO));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "DOCTOR"})
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除排班，ID：{}", id);
        scheduleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DoctorScheduleDTO> getById(@PathVariable Long id) {
        return Result.success(scheduleService.getById(id));
    }

    @GetMapping("/doctor/{doctorId}/schedules")
    public Result<List<DoctorScheduleDTO>> getDoctorSchedules(@PathVariable Long doctorId,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("获取医生排班，医生ID：{}，日期：{}", doctorId, date);
        
        try {
            List<DoctorScheduleDTO> schedules;
            if (date != null) {
                schedules = scheduleService.getDoctorSchedules(doctorId, date);
                log.info("根据日期查询到 {} 条排班记录", schedules.size());
            } else {
                // 如果没有提供日期，返回当天的排班
                LocalDate today = LocalDate.now();
                log.info("日期参数为空，默认使用当天日期：{}", today);
                schedules = scheduleService.getDoctorSchedules(doctorId, today);
                log.info("根据当天日期查询到 {} 条排班记录", schedules.size());
            }
            
            return Result.success(schedules);
        } catch (Exception e) {
            log.error("获取医生排班发生错误: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/department/{departmentId}")
    public Result<List<DoctorScheduleDTO>> getDepartmentSchedules(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(scheduleService.getDepartmentSchedules(departmentId, date));
    }

    @GetMapping("/doctor/{doctorId}/future")
    public Result<List<DoctorScheduleDTO>> getFutureSchedulesByDoctor(@PathVariable Long doctorId) {
        return Result.success(scheduleService.getFutureSchedulesByDoctor(doctorId));
    }

    @GetMapping("/department/{departmentId}/future")
    public Result<List<DoctorScheduleDTO>> getFutureSchedulesByDepartment(@PathVariable Long departmentId) {
        return Result.success(scheduleService.getFutureSchedulesByDepartment(departmentId));
    }
    
    /**
     * 检查医生时间段可用性
     * @param doctorId 医生ID
     * @param date 日期
     * @param period 时间段（上午/下午/晚上）
     * @return 可用性信息
     */
    @GetMapping("/doctor/{doctorId}/time-slots/availability")
    public Result<Boolean> checkTimeSlotAvailability(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String period) {
        log.info("检查医生时间段可用性，医生ID：{}，日期：{}，时间段：{}", doctorId, date, period);
        
        // 检查该时间段是否已存在排班
        boolean isAvailable = !scheduleService.checkPeriodExists(doctorId, date, period);
        
        return Result.success(isAvailable);
    }

    /**
     * 获取最近的排班数据
     * @param limit 返回记录的最大数量
     * @return 最近的排班数据列表
     */
    @GetMapping("/recent")
    public Result<List<DoctorScheduleDTO>> getRecentSchedules(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.info("获取最近的排班数据，限制数量: {}", limit);
        return Result.success(scheduleService.getRecentSchedules(limit));
    }
}