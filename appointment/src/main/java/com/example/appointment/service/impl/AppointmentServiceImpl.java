package com.example.appointment.service.impl;

import com.example.appointment.dto.AppointmentDTO;
import com.example.appointment.dto.AppointmentResultDTO;
import com.example.appointment.dto.DoctorScheduleDTO;
import com.example.appointment.dto.TimeConflictAnalysisDTO;
import com.example.appointment.service.AppointmentConflictService;
import com.example.appointment.service.SmartReminderService;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Department;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.User;
import com.example.appointment.enums.AppointmentStatus;
import com.example.appointment.exception.ResourceNotFoundException;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.DepartmentRepository;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.service.AppointmentService;
import com.example.appointment.service.DoctorScheduleService;
import com.example.appointment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorScheduleService doctorScheduleService;
    private final UserService userService;
    private final AppointmentConflictService appointmentConflictService;
    private final SmartReminderService smartReminderService;

    @Override
    @Transactional
    public AppointmentResultDTO createAppointment(AppointmentDTO appointmentDTO) {
        log.info("Creating new appointment for doctor: {}, time: {}", 
                appointmentDTO.getDoctorId(), appointmentDTO.getAppointmentTime());

        // 验证预约时间
        validateAppointmentTime(appointmentDTO.getAppointmentTime());

        // 获取医生当天的排班信息
        LocalDate appointmentDate = appointmentDTO.getAppointmentTime().toLocalDate();
        List<DoctorScheduleDTO> schedules = doctorScheduleService.getDoctorSchedules(
            appointmentDTO.getDoctorId(), 
            appointmentDate
        );

        // 检查是否有排班
        DoctorScheduleDTO schedule = findMatchingSchedule(schedules, appointmentDTO.getAppointmentTime());
        if (schedule == null) {
            throw new IllegalStateException("所选时间段医生未排班");
        }

        // 检查是否还有可用预约数
        if (schedule.getAvailableAppointments() <= 0) {
            throw new IllegalStateException("所选时间段预约已满");
        }

        // 检查患者是否已经有同一天的预约
        if (hasExistingAppointment(appointmentDTO.getPatientId(), appointmentDate)) {
            throw new IllegalStateException("您在该日期已有预约");
        }

        Appointment appointment = new Appointment();
        
        // 设置科室信息
        Department department = departmentRepository.findById(appointmentDTO.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("科室不存在"));
        appointment.setDepartment(department);

        // 设置医生信息
        Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("医生不存在"));
        appointment.setDoctor(doctor);

        // 设置患者信息
        User patient = userRepository.findById(appointmentDTO.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("患者不存在"));
        appointment.setPatient(patient);

        appointment.setAppointmentTime(appointmentDTO.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.PENDING.name());
        appointment.setDescription(appointmentDTO.getDescription());
        
        // 生成预约编号
        String appointmentNumber = generateAppointmentNumber();
        appointment.setAppointmentNumber(appointmentNumber);

        // 保存预约并更新可用预约数
        appointment = appointmentRepository.save(appointment);
        doctorScheduleService.updateAvailableAppointments(schedule.getId(), -1);

        log.info("Created appointment with ID: {}", appointment.getId());
        
        // 执行冲突检测
        try {
            TimeConflictAnalysisDTO conflictAnalysis = appointmentConflictService.analyzeTimeConflicts(
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime()
            );
            
            if (!conflictAnalysis.getPatientConflicts().isEmpty() || !conflictAnalysis.getDoctorConflicts().isEmpty()) {
                appointment.setConflictLevel(conflictAnalysis.getRiskLevel());
                appointment.setConflictChecked(true);
                appointmentRepository.save(appointment);
                log.warn("Appointment conflicts detected for appointment ID: {}", appointment.getId());
            }
        } catch (Exception e) {
            log.error("Error during conflict analysis for appointment ID: {}", appointment.getId(), e);
        }
        
        // 设置提醒
        try {
            smartReminderService.sendAppointmentReminder(appointment.getId());
            log.info("Scheduled reminder for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error scheduling reminder for appointment ID: {}", appointment.getId(), e);
        }
        
        // 创建增强的返回结果
        AppointmentDTO baseDTO = convertToDTO(appointment);
        AppointmentResultDTO resultDTO = new AppointmentResultDTO(baseDTO);
        
        // 设置增强信息
        resultDTO.setSuccessMessage("预约成功！请按时就诊");
        resultDTO.setAppointmentNumber(String.format("YY%06d", appointment.getId()));
        resultDTO.setDepartmentName(department.getName());
        resultDTO.setDoctorName(doctor.getName());
        resultDTO.setDoctorTitle(doctor.getTitle());
        resultDTO.setAppointmentDate(appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        resultDTO.setFormattedAppointmentTime(appointment.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        resultDTO.setLocation(department.getName());
        resultDTO.setNotes("请提前30分钟到达医院，携带身份证和医保卡");
        
        return resultDTO;
    }

    /**
     * 生成预约编号
     */
    private String generateAppointmentNumber() {
        String datePrefix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new java.util.Random().nextInt(10000));
        return "A" + datePrefix + randomSuffix;
    }

    private void validateAppointmentTime(LocalDateTime appointmentTime) {
        // 检查是否是过去的时间
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("不能预约过去的时间");
        }

        // 检查是否是工作日
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("不能预约周末时间");
        }

        // 获取当前用户角色
        User currentUser = userService.getCurrentUser();
        String role = currentUser.getRole();

        // 如果是学生或老师，允许24小时预约
        if ("STUDENT".equals(role) || "TEACHER".equals(role)) {
            return;
        }

        // 其他用户只能在工作时间内预约
        LocalTime time = appointmentTime.toLocalTime();
        boolean isMorning = time.isAfter(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(12, 0));
        boolean isAfternoon = time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(17, 0));
        if (!isMorning && !isAfternoon) {
            throw new IllegalArgumentException("请在工作时间内预约（上午8:00-12:00，下午14:00-17:00）");
        }
    }

    private DoctorScheduleDTO findMatchingSchedule(List<DoctorScheduleDTO> schedules, LocalDateTime appointmentTime) {
        LocalTime time = appointmentTime.toLocalTime();
        String period = time.getHour() < 12 ? "MORNING" : "AFTERNOON";
        
        return schedules.stream()
            .filter(s -> s.getPeriod().equals(period) && s.getStatus() == 1)
            .findFirst()
            .orElse(null);
    }

    private boolean hasExistingAppointment(Long patientId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        
        return !appointmentRepository.findByPatientAndTimeRange(
            patientId, dayStart, dayEnd).isEmpty();
    }

    @Override
    @Transactional
    public AppointmentDTO updateAppointmentStatus(Long id, String status) {
        log.info("Updating appointment status: {} to {}", id, status);
        
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
            
        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(status);
            AppointmentStatus oldStatus = AppointmentStatus.valueOf(appointment.getStatus());
            
            // 如果是取消预约，需要增加可用预约数
            if (newStatus == AppointmentStatus.CANCELLED && oldStatus != AppointmentStatus.CANCELLED) {
                LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
                List<DoctorScheduleDTO> schedules = doctorScheduleService.getDoctorSchedules(
                    appointment.getDoctor().getId(), 
                    appointmentDate
                );
                
                DoctorScheduleDTO schedule = findMatchingSchedule(
                    schedules, 
                    appointment.getAppointmentTime()
                );
                
                if (schedule != null) {
                    doctorScheduleService.updateAvailableAppointments(schedule.getId(), 1);
                }
            }
            
            appointment.setStatus(newStatus.name());
            appointment = appointmentRepository.save(appointment);
            log.info("Updated appointment status successfully");
            return convertToDTO(appointment);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的预约状态");
        }
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id) {
        log.info("Cancelling appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
            
        appointment.setStatus(AppointmentStatus.CANCELLED.name());
        appointmentRepository.save(appointment);
        log.info("Cancelled appointment successfully");
    }

    @Override
    public AppointmentDTO getAppointment(Long id) {
        log.info("Fetching appointment: {}", id);
        
        return appointmentRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new ResourceNotFoundException("预约不存在"));
    }

    @Override
    public Page<AppointmentDTO> getPatientAppointments(Long patientId, Pageable pageable) {
        log.info("Fetching appointments for patient: {}", patientId);
        return appointmentRepository.findPatientAppointmentsOrdered(patientId, pageable)
            .map(this::convertToDTO);
    }
    
    @Override
    public Page<AppointmentDTO> getPatientAppointmentsByDate(Long patientId, LocalDate date, Pageable pageable) {
        log.info("Fetching appointments for patient: {} on date: {}", patientId, date);
        return appointmentRepository.findAppointmentsByPatientAndDateAndStatus(patientId, date, null, pageable)
            .map(this::convertToDTO);
    }

    @Override
    public Page<AppointmentDTO> getDoctorAppointments(Long doctorId, Pageable pageable) {
        log.info("Fetching appointments for doctor: {}", doctorId);
        return appointmentRepository.findDoctorAppointmentsOrdered(doctorId, pageable)
            .map(this::convertToDTO);
    }

    @Override
    public Page<AppointmentDTO> getDoctorAppointments(Long doctorId, LocalDate date, String status, Pageable pageable) {
        log.info("Fetching appointments for doctor: {} with date: {} and status: {}", doctorId, date, status);
        return appointmentRepository.findAppointmentsByDoctorAndDateAndStatus(doctorId, date, status, pageable)
            .map(this::convertToDTO);
    }

    @Override
    public Page<AppointmentDTO> getDepartmentAppointments(Long departmentId, Pageable pageable) {
        log.info("Fetching appointments for department: {}", departmentId);
        return appointmentRepository.findDepartmentAppointmentsOrdered(departmentId, pageable)
            .map(this::convertToDTO);
    }

    @Override
    public boolean isDoctorAvailable(Long doctorId, LocalDateTime appointmentTime) {
        // 获取当天的开始和结束时间
        LocalDateTime dayStart = appointmentTime.with(LocalTime.MIN);
        LocalDateTime dayEnd = appointmentTime.with(LocalTime.MAX);
        
        // 检查医生在该时间段是否已有预约
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorAndTimeRange(doctorId, dayStart, dayEnd);
            
        // 如果没有预约或者所有预约都是取消状态，则医生可用
        return existingAppointments.isEmpty() || 
               existingAppointments.stream()
                   .allMatch(a -> AppointmentStatus.CANCELLED.name().equals(a.getStatus()));
    }

    @Override
    public List<AppointmentDTO> getDoctorAppointmentsByTimeRange(
            Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching appointments for doctor: {} between {} and {}", 
                doctorId, startTime, endTime);
                
        return appointmentRepository.findByDoctorAndTimeRange(doctorId, startTime, endTime)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Long getTodayAppointmentsCount() {
        return appointmentRepository.countTodayAppointments();
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setDepartmentId(appointment.getDepartment().getId());
        dto.setDepartmentName(appointment.getDepartment().getName());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorName(appointment.getDoctor().getName());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientName(appointment.getPatient().getName());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setDescription(appointment.getDescription());
        dto.setCreateTime(appointment.getCreatedAt());
        dto.setUpdateTime(appointment.getUpdatedAt());
        return dto;
    }
}