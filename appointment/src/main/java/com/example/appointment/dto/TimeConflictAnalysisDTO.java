package com.example.appointment.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间冲突分析DTO
 */
public class TimeConflictAnalysisDTO {
    private List<AppointmentConflictDTO> patientConflicts; // 患者时间冲突
    private List<AppointmentConflictDTO> doctorConflicts;   // 医生时间冲突
    private boolean timeSlotAvailable;                     // 时间段是否可用
    private String riskLevel;                              // 冲突风险等级：NONE, LOW, MEDIUM, HIGH
    private List<String> suggestions;                      // 智能建议
    
    public TimeConflictAnalysisDTO() {
    }
    
    public List<AppointmentConflictDTO> getPatientConflicts() {
        return patientConflicts;
    }
    
    public void setPatientConflicts(List<AppointmentConflictDTO> patientConflicts) {
        this.patientConflicts = patientConflicts;
    }
    
    public List<AppointmentConflictDTO> getDoctorConflicts() {
        return doctorConflicts;
    }
    
    public void setDoctorConflicts(List<AppointmentConflictDTO> doctorConflicts) {
        this.doctorConflicts = doctorConflicts;
    }
    
    public boolean isTimeSlotAvailable() {
        return timeSlotAvailable;
    }
    
    public void setTimeSlotAvailable(boolean timeSlotAvailable) {
        this.timeSlotAvailable = timeSlotAvailable;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}