package com.example.appointment.service;

import com.example.appointment.dto.AppointmentDTO;
import com.example.appointment.dto.AppointmentResultDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    
    // 创建预约
    AppointmentResultDTO createAppointment(AppointmentDTO appointmentDTO);

    // 更新预约状态
    AppointmentDTO updateAppointmentStatus(Long id, String status);

    // 取消预约
    void cancelAppointment(Long id);

    // 获取预约详情
    AppointmentDTO getAppointment(Long id);

    // 获取患者的预约列表
    Page<AppointmentDTO> getPatientAppointments(Long patientId, Pageable pageable);
    
    Page<AppointmentDTO> getPatientAppointmentsByDate(Long patientId, LocalDate date, Pageable pageable);

    // 获取医生的预约列表
    Page<AppointmentDTO> getDoctorAppointments(Long doctorId, Pageable pageable);

    // 获取医生的预约列表(带日期和状态筛选)
    Page<AppointmentDTO> getDoctorAppointments(Long doctorId, java.time.LocalDate date, String status, Pageable pageable);

    // 获取科室的预约列表
    Page<AppointmentDTO> getDepartmentAppointments(Long departmentId, Pageable pageable);

    // 检查医生在指定时间段是否有空闲
    boolean isDoctorAvailable(Long doctorId, LocalDateTime appointmentTime);

    // 获取医生在指定时间段的预约列表
    List<AppointmentDTO> getDoctorAppointmentsByTimeRange(Long doctorId, LocalDateTime startTime, LocalDateTime endTime);

    // 获取当天预约数量
    Long getTodayAppointmentsCount();
}