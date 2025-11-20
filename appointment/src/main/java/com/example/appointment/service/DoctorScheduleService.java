package com.example.appointment.service;

import com.example.appointment.dto.DoctorScheduleDTO;
import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {
    
    // 创建排班
    DoctorScheduleDTO create(DoctorScheduleDTO scheduleDTO);
    
    // 更新排班
    DoctorScheduleDTO update(Long id, DoctorScheduleDTO scheduleDTO);
    
    // 删除排班
    void delete(Long id);
    
    // 获取排班详情
    DoctorScheduleDTO getById(Long id);
    
    // 获取医生某天的排班
    List<DoctorScheduleDTO> getDoctorSchedules(Long doctorId, LocalDate date);
    
    // 获取科室某天的排班
    List<DoctorScheduleDTO> getDepartmentSchedules(Long departmentId, LocalDate date);
    
    // 获取医生未来的排班
    List<DoctorScheduleDTO> getFutureSchedulesByDoctor(Long doctorId);
    
    // 获取科室未来的排班
    List<DoctorScheduleDTO> getFutureSchedulesByDepartment(Long departmentId);
    
    // 更新可用预约数
    void updateAvailableAppointments(Long scheduleId, int change);
    
    /**
     * 获取最近的排班数据，优先返回当天和未来的排班
     * @param limit 返回记录的最大数量
     * @return 最近的排班数据列表
     */
    List<DoctorScheduleDTO> getRecentSchedules(Integer limit);
    
    /**
     * 获取医生指定月份的排班信息
     * @param doctorId 医生ID
     * @param year 年份
     * @param month 月份(1-12)
     * @return 排班信息列表
     */
    List<DoctorScheduleDTO> getDoctorSchedulesByMonth(Long doctorId, int year, int month);
    
    /**
     * 检查医生在指定日期的指定时段是否已存在排班
     * @param doctorId 医生ID
     * @param scheduleDate 排班日期
     * @param period 时间段（上午/下午/晚上）
     * @return 是否已存在排班
     */
    boolean checkPeriodExists(Long doctorId, LocalDate scheduleDate, String period);
}