package com.example.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DoctorScheduleDTO {
    private Long id;

    @NotNull(message = "请选择医生")
    private Long doctorId;
    
    private String doctorName;

    @NotNull(message = "请选择科室")
    private Long departmentId;
    
    private String departmentName;

    @NotNull(message = "请选择日期")
    private LocalDate scheduleDate;

    @NotNull(message = "请选择时段")
    private String period; // MORNING, AFTERNOON

    @NotNull(message = "请设置最大预约人数")
    private Integer maxAppointments;

    private Integer availableAppointments;

    private Integer status = 1; // 1-正常, 0-停诊
    
    public Integer getStatus() {
        return status;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public void setMaxAppointments(Integer maxAppointments) {
        this.maxAppointments = maxAppointments;
    }
    
    public void setAvailableAppointments(Integer availableAppointments) {
        this.availableAppointments = availableAppointments;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    // 新增拥挤度相关字段
    private Double congestionRate; // 拥挤度比例 (0.0-1.0)
    private String congestionLevel; // 拥挤度级别：LOW, MEDIUM, HIGH, FULL
    private Integer bookedAppointments; // 已预约数量
    
    public Integer getBookedAppointments() {
        return bookedAppointments;
    }
    
    public void setBookedAppointments(Integer bookedAppointments) {
        this.bookedAppointments = bookedAppointments;
    }
    
    public Double getCongestionRate() {
        return congestionRate;
    }
    
    public void setCongestionRate(Double congestionRate) {
        this.congestionRate = congestionRate;
    }
    
    public String getCongestionLevel() {
        return congestionLevel;
    }
    
    public void setCongestionLevel(String congestionLevel) {
        this.congestionLevel = congestionLevel;
    }
    
    public LocalDate getScheduleDate() {
        return scheduleDate;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public Integer getMaxAppointments() {
        return maxAppointments;
    }
    
    public Long getId() {
        return id;
    }
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public Integer getAvailableAppointments() {
        return availableAppointments;
    }
}