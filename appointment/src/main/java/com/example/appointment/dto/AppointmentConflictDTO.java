package com.example.appointment.dto;

import java.time.LocalDateTime;

/**
 * 预约冲突DTO
 */
public class AppointmentConflictDTO {
    private String conflictType;        // 冲突类型：PATIENT_TIME_CONFLICT, DOCTOR_TIME_CONFLICT
    private String conflictDescription; // 冲突描述
    private Long conflictingAppointmentId; // 冲突的预约ID
    private LocalDateTime conflictingTime; // 冲突的预约时间
    private long timeDifference;        // 时间差（分钟）
    private String severity;            // 严重程度：LOW, MEDIUM, HIGH
    
    public AppointmentConflictDTO() {
    }
    
    public String getConflictType() {
        return conflictType;
    }
    
    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }
    
    public String getConflictDescription() {
        return conflictDescription;
    }
    
    public void setConflictDescription(String conflictDescription) {
        this.conflictDescription = conflictDescription;
    }
    
    public Long getConflictingAppointmentId() {
        return conflictingAppointmentId;
    }
    
    public void setConflictingAppointmentId(Long conflictingAppointmentId) {
        this.conflictingAppointmentId = conflictingAppointmentId;
    }
    
    public LocalDateTime getConflictingTime() {
        return conflictingTime;
    }
    
    public void setConflictingTime(LocalDateTime conflictingTime) {
        this.conflictingTime = conflictingTime;
    }
    
    public long getTimeDifference() {
        return timeDifference;
    }
    
    public void setTimeDifference(long timeDifference) {
        this.timeDifference = timeDifference;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}