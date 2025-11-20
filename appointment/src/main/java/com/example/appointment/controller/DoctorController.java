package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.DoctorDTO;
import com.example.appointment.dto.DoctorScheduleDTO;
import com.example.appointment.dto.UserDTO;
import com.example.appointment.dto.UserRegisterDTO;
import com.example.appointment.service.DoctorScheduleService;
import com.example.appointment.service.DoctorService;
import com.example.appointment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;
    private final UserService userService;
    private final DoctorScheduleService doctorScheduleService;

    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<DoctorDTO> create(@RequestBody @Valid DoctorDTO doctorDTO) {
        log.info("开始创建医生，请求数据: {}", doctorDTO);
        
        try {
            // 手动验证创建时的必填字段
            if (doctorDTO.getUsername() == null || doctorDTO.getUsername().trim().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            if (doctorDTO.getPassword() == null || doctorDTO.getPassword().trim().isEmpty()) {
                throw new RuntimeException("密码不能为空");
            }
            
            // 创建用户账号
            UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
            userRegisterDTO.setUsername(doctorDTO.getUsername());
            userRegisterDTO.setPassword(doctorDTO.getPassword());
            userRegisterDTO.setConfirmPassword(doctorDTO.getConfirmPassword() != null ? 
                                            doctorDTO.getConfirmPassword() : doctorDTO.getPassword());
            userRegisterDTO.setRole("DOCTOR");
            userRegisterDTO.setName(doctorDTO.getName());
            userRegisterDTO.setPhone(doctorDTO.getPhone());
            userRegisterDTO.setEmail(doctorDTO.getEmail());
            
            log.info("准备注册医生用户: {}", userRegisterDTO);
            UserDTO userDTO = userService.register(userRegisterDTO);
            log.info("医生用户注册成功，用户ID: {}", userDTO.getId());

            // 设置医生的userId
            doctorDTO.setUserId(userDTO.getId());
            
            // 确保状态字段有值
            if (doctorDTO.getStatus() == null) {
                doctorDTO.setStatus(1);
            }
            
            log.info("准备创建医生记录: {}", doctorDTO);
            // 创建医生信息
            DoctorDTO created = doctorService.create(doctorDTO);
            log.info("医生创建成功: {}", created);
            
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建医生失败", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<DoctorDTO> update(@PathVariable Long id, @RequestBody DoctorDTO doctorDTO) {
        log.info("更新医生，ID：{}，数据：{}", id, doctorDTO);
        return Result.success(doctorService.update(id, doctorDTO));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除医生，ID：{}", id);
        doctorService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DoctorDTO> getById(@PathVariable Long id) {
        return Result.success(doctorService.getById(id));
    }

    @GetMapping
    public Result<Page<DoctorDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer status,
            Pageable pageable) {
        return Result.success(doctorService.search(name, departmentId, status, pageable));
    }

    @GetMapping("/department/{departmentId}")
    public Result<List<DoctorDTO>> getByDepartment(@PathVariable Long departmentId) {
        return Result.success(doctorService.getByDepartment(departmentId));
    }

    @GetMapping("/user/{userId}")
    @RequireRole({"DOCTOR"})
    public Result<DoctorDTO> getDoctorByUserId(@PathVariable Long userId) {
        return Result.success(doctorService.getDoctorByUserId(userId));
    }


    
    /**
     * 获取医生排班信息
     * @param doctorId 医生ID
     * @param date 日期
     * @param type 查询类型，可选值: day, month
     * @return 排班信息列表
     */
    @GetMapping("/{doctorId}/schedules")
    public Result<List<DoctorScheduleDTO>> getDoctorSchedules(
            @PathVariable Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false, defaultValue = "day") String type) {
        log.info("获取医生排班，医生ID：{}，日期参数：{}，查询类型：{}", doctorId, date, type);
        
        try {
            LocalDate localDate;
            
            if (date == null || date.trim().isEmpty()) {
                // 如果没有提供日期，使用当天
                localDate = LocalDate.now();
                log.info("日期参数为空，使用当天日期：{}", localDate);
            } else if (date.contains("T")) {
                // ISO格式日期时间字符串，如: 2025-03-22T09:50:44.327Z
                localDate = LocalDate.parse(date.substring(0, 10));
                log.info("解析ISO日期时间格式：{} -> {}", date, localDate);
            } else {
                // 仅日期字符串，如: 2025-03-22
                localDate = LocalDate.parse(date);
                log.info("解析日期格式：{}", localDate);
            }
            
            List<DoctorScheduleDTO> schedules;
            if ("month".equalsIgnoreCase(type)) {
                // 查询整月排班
                schedules = doctorScheduleService.getDoctorSchedulesByMonth(doctorId, localDate.getYear(), localDate.getMonthValue());
                log.info("查询到的月度排班记录数: {}", schedules.size());
            } else {
                // 查询单日排班
                schedules = doctorScheduleService.getDoctorSchedules(doctorId, localDate);
                log.info("查询到的单日排班记录数: {}", schedules.size());
            }
            
            return Result.success(schedules);
        } catch (Exception e) {
            log.error("处理日期参数错误: {}", e.getMessage(), e);
            return Result.error("处理排班数据发生错误: " + e.getMessage());
        }
    }
}