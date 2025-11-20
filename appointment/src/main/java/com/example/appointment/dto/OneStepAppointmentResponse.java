package com.example.appointment.dto;

public class OneStepAppointmentResponse {
    private Long appointmentId;
    private String appointmentNumber;
    private String departmentName;
    private String doctorName;
    private String doctorTitle;
    private String appointmentDate;
    private String appointmentTime;
    private String formattedAppointmentTime;
    private String location;
    private String aiRecommendationReason;
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
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
    
    public String getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
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
    
    public String getAiRecommendationReason() {
        return aiRecommendationReason;
    }
    
    public void setAiRecommendationReason(String aiRecommendationReason) {
        this.aiRecommendationReason = aiRecommendationReason;
    }
}