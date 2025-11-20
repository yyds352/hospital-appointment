package com.example.appointment.service.impl;

import com.example.appointment.dto.DoctorScheduleDTO;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.DoctorSchedule;
import com.example.appointment.entity.Department;
import com.example.appointment.entity.User;
import com.example.appointment.exception.BusinessException;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.DoctorScheduleRepository;
import com.example.appointment.repository.DepartmentRepository;
import com.example.appointment.service.DoctorScheduleService;
import com.example.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(DoctorScheduleServiceImpl.class);

    @Override
    @Transactional
    public DoctorScheduleDTO create(DoctorScheduleDTO scheduleDTO) {
        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        
        // 获取医生信息
        Doctor doctor = doctorRepository.findById(scheduleDTO.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));
                
        // 验证医生是否有关联科室
        if (doctor.getDepartment() == null) {
            throw new BusinessException("医生未关联科室，无法创建排班");
        }
                
        // 如果是医生用户，只能创建自己的排班
        if ("DOCTOR".equals(currentUser.getRole()) && !doctor.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("医生只能创建自己的排班");
        }

        // 检查日期是否合法 - 允许创建今天及以后的排班
        LocalDate today = LocalDate.now();
        if (scheduleDTO.getScheduleDate().isBefore(today.minusDays(1))) {
            throw new BusinessException("不能创建过去的排班");
        }

        // 检查是否已存在排班
        if (scheduleRepository.existsByDoctorIdAndScheduleDate(
                scheduleDTO.getDoctorId(), scheduleDTO.getScheduleDate())) {
            throw new BusinessException("该日期已存在排班");
        }

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(scheduleDTO.getDoctorId());
        schedule.setDepartmentId(doctor.getDepartment().getId());
        schedule.setScheduleDate(scheduleDTO.getScheduleDate());
        schedule.setPeriod(scheduleDTO.getPeriod());
        schedule.setMaxAppointments(scheduleDTO.getMaxAppointments());
        schedule.setAvailableAppointments(scheduleDTO.getMaxAppointments());
        schedule.setStatus(1);

        schedule = scheduleRepository.save(schedule);
        return convertToDTO(schedule);
    }

    @Override
    @Transactional
    public void updateAvailableAppointments(Long scheduleId, int change) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException("排班不存在"));
        
        int newAvailable = schedule.getAvailableAppointments() + change;
        if (newAvailable < 0) {
            throw new BusinessException("可预约数量不能小于0");
        }
        if (newAvailable > schedule.getMaxAppointments()) {
            throw new BusinessException("可预约数量不能大于最大预约数量");
        }
        
        schedule.setAvailableAppointments(newAvailable);
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public DoctorScheduleDTO update(Long id, DoctorScheduleDTO scheduleDTO) {
        DoctorSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));

        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        
        // 获取医生信息
        Doctor doctor = doctorRepository.findById(schedule.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));
                
        // 如果是医生用户，只能更新自己的排班
        if ("DOCTOR".equals(currentUser.getRole()) && !doctor.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("医生只能更新自己的排班");
        }

        schedule.setPeriod(scheduleDTO.getPeriod());
        schedule.setMaxAppointments(scheduleDTO.getMaxAppointments());
        schedule.setStatus(scheduleDTO.getStatus());

        schedule = scheduleRepository.save(schedule);
        return convertToDTO(schedule);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DoctorSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));

        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        
        // 获取医生信息
        Doctor doctor = doctorRepository.findById(schedule.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));
                
        // 如果是医生用户，只能删除自己的排班
        if ("DOCTOR".equals(currentUser.getRole()) && !doctor.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("医生只能删除自己的排班");
        }

        scheduleRepository.delete(schedule);
    }

    @Override
    public DoctorScheduleDTO getById(Long id) {
        DoctorSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));
        return convertToDTO(schedule);
    }

    @Override
    public List<DoctorScheduleDTO> getDoctorSchedules(Long doctorId, LocalDate date) {
        System.out.println("查询医生排班 - 医生ID：" + doctorId + ", 日期：" + date);
        
        // 尝试使用JPQL查询
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndScheduleDate(doctorId, date);
        System.out.println("JPQL查询结果数量：" + (schedules != null ? schedules.size() : "null"));
        
        // 如果JPQL查询没有结果，尝试使用原生SQL查询
        if (schedules == null || schedules.isEmpty()) {
            String dateStr = date.toString(); // 转为YYYY-MM-DD格式
            System.out.println("尝试使用原生SQL查询 - 日期字符串：" + dateStr);
            schedules = scheduleRepository.findSchedulesByNativeQuery(doctorId, dateStr);
            System.out.println("原生SQL查询结果数量：" + (schedules != null ? schedules.size() : "null"));
        }
        
        if (schedules != null && !schedules.isEmpty()) {
            for (DoctorSchedule schedule : schedules) {
                System.out.println("排班ID: " + schedule.getId() + ", 时段: " + schedule.getPeriod() + ", 日期: " + schedule.getScheduleDate());
            }
        }
        
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleDTO> getDepartmentSchedules(Long departmentId, LocalDate date) {
        List<DoctorSchedule> schedules = scheduleRepository.findByDepartmentIdAndScheduleDate(departmentId, date);
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleDTO> getFutureSchedulesByDoctor(Long doctorId) {
        List<DoctorSchedule> schedules = scheduleRepository.findDoctorSchedulesFromDate(
                doctorId, LocalDate.now());
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleDTO> getFutureSchedulesByDepartment(Long departmentId) {
        List<DoctorSchedule> schedules = scheduleRepository.findDepartmentSchedulesFromDate(
                departmentId, LocalDate.now());
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleDTO> getRecentSchedules(Integer limit) {
        LocalDate today = LocalDate.now();
        
        // 先获取今天和未来的排班
        List<DoctorSchedule> futureSchedules = scheduleRepository.findSchedulesFromDateOrdered(today);
        
        // 如果未来排班不足指定数量，再获取过去的排班补足
        List<DoctorSchedule> recentSchedules = new ArrayList<>(futureSchedules);
        
        if (recentSchedules.size() < limit) {
            // 需要补充的数量
            int remainingCount = limit - recentSchedules.size();
            // 获取过去的排班，按日期降序排列
            List<DoctorSchedule> pastSchedules = scheduleRepository.findSchedulesBeforeDateOrdered(
                    today, PageRequest.of(0, remainingCount));
            recentSchedules.addAll(pastSchedules);
        } else if (recentSchedules.size() > limit) {
            // 如果超过了指定数量，只保留前limit个
            recentSchedules = recentSchedules.subList(0, limit);
        }
        
        return recentSchedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean checkPeriodExists(Long doctorId, LocalDate scheduleDate, String period) {
        log.info("检查医生时间段是否存在，医生ID：{}，日期：{}，时间段：{}", doctorId, scheduleDate, period);
        return scheduleRepository.existsByDoctorIdAndScheduleDateAndPeriod(doctorId, scheduleDate, period);
    }

    @Override
    public List<DoctorScheduleDTO> getDoctorSchedulesByMonth(Long doctorId, int year, int month) {
        log.info("获取医生月度排班，医生ID：{}，年份：{}，月份：{}", doctorId, year, month);
        
        // 创建月份的起始日期和结束日期
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        log.info("查询日期范围：{} 至 {}", startDate, endDate);
        
        // 查询数据库中是否存在该医生
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new BusinessException("医生不存在"));
            log.info("查询到医生信息: ID={}, 姓名={}, 用户ID={}", doctor.getId(), doctor.getName(), doctor.getUserId());
        } catch (Exception e) {
            log.error("查询医生信息失败: {}", e.getMessage(), e);
        }
        
        // 查询日期范围内的排班记录
        List<DoctorSchedule> schedules = scheduleRepository.findDoctorSchedulesBetweenDatesOrdered(
            doctorId, startDate, endDate);
        
        log.info("查询到 {} 条排班记录", schedules.size());
        
        // 如果没有找到排班记录，尝试使用原生SQL查询
        if (schedules.isEmpty()) {
            log.info("使用原生SQL查询尝试获取排班数据");
            try {
                String sql = "SELECT * FROM doctor_schedule WHERE doctor_id = ? AND schedule_date BETWEEN ? AND ? ORDER BY schedule_date, period";
                log.info("执行SQL: {}, 参数: [{}, {}, {}]", sql, doctorId, startDate, endDate);
                
                // 添加日志输出doctor_schedule表中的所有doctorId
                log.info("数据库中的医生ID: {}", scheduleRepository.findAll().stream()
                    .map(DoctorSchedule::getDoctorId)
                    .distinct()
                    .collect(Collectors.toList()));
            } catch (Exception e) {
                log.error("原生SQL查询失败: {}", e.getMessage(), e);
            }
        }
        
        // 转换为DTO并返回
        return schedules.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private DoctorScheduleDTO convertToDTO(DoctorSchedule schedule) {
        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctorId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setPeriod(schedule.getPeriod());
        dto.setMaxAppointments(schedule.getMaxAppointments());
        dto.setAvailableAppointments(schedule.getAvailableAppointments());
        dto.setStatus(schedule.getStatus());
        
        // 获取医生信息
        Doctor doctor = doctorRepository.findById(schedule.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));
        dto.setDoctorName(doctor.getName());
        dto.setDepartmentId(doctor.getDepartment() != null ? doctor.getDepartment().getId() : null);
        
        // 获取部门信息
        Department department = doctor.getDepartment() != null ? doctor.getDepartment() : 
                departmentRepository.findById(doctor.getDepartment().getId())
                .orElseThrow(() -> new BusinessException("部门不存在"));
        dto.setDepartmentName(department.getName());
        
        // 计算拥挤度信息
        calculateCongestionInfo(dto);
        
        return dto;
    }
    
    private void calculateCongestionInfo(DoctorScheduleDTO dto) {
        if (dto.getMaxAppointments() == null || dto.getMaxAppointments() <= 0) {
            dto.setCongestionRate(0.0);
            dto.setCongestionLevel("FULL");
            dto.setBookedAppointments(0);
            return;
        }
        
        // 计算已预约数量
        Integer booked = dto.getMaxAppointments() - (dto.getAvailableAppointments() != null ? dto.getAvailableAppointments() : 0);
        dto.setBookedAppointments(booked);
        
        // 计算拥挤度比例
        double rate = (double) booked / dto.getMaxAppointments();
        dto.setCongestionRate(rate);
        
        // 设置拥挤度级别
        if (rate >= 1.0) {
            dto.setCongestionLevel("FULL");
        } else if (rate >= 0.8) {
            dto.setCongestionLevel("HIGH");
        } else if (rate >= 0.5) {
            dto.setCongestionLevel("MEDIUM");
        } else {
            dto.setCongestionLevel("LOW");
        }
    }
}