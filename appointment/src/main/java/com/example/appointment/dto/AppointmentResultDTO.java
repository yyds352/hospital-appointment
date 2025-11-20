package com.example.appointment.dto;

import java.time.LocalDateTime;

public class AppointmentResultDTO extends AppointmentDTO {
    private String successMessage;
    private String appointmentNumber;
    private String departmentName;
    private String doctorName;
    private String doctorTitle;
    private String appointmentDate;
    private String formattedAppointmentTime;
    private String location;
    private String notes;
    
    public AppointmentResultDTO() {
        super();
    }
    
    public AppointmentResultDTO(AppointmentDTO appointment) {
        super();
        this.setId(appointment.getId());
        this.setDepartmentId(appointment.getDepartmentId());
        this.setDepartmentName(appointment.getDepartmentName());
        this.setDoctorId(appointment.getDoctorId());
        this.setDoctorName(appointment.getDoctorName());
        this.setPatientId(appointment.getPatientId());
        this.setPatientName(appointment.getPatientName());
        this.setAppointmentTime(appointment.getAppointmentTime());
        this.setStatus(appointment.getStatus());
        this.setDescription(appointment.getDescription());
        this.setCreateTime(appointment.getCreateTime());
        this.setUpdateTime(appointment.getUpdateTime());
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
    
    public String getAppointmentNumber() {
        return appointmentNumber;
    }
    
    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getDoctorTitle() {
        return doctorTitle;
    }
    
    public void setDoctorTitle(String doctorTitle) {
        this.doctorTitle = doctorTitle;
    }
    
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public String getFormattedAppointmentTime() {
        return formattedAppointmentTime;
    }
    
    public void setFormattedAppointmentTime(String formattedAppointmentTime) {
        this.formattedAppointmentTime = formattedAppointmentTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}