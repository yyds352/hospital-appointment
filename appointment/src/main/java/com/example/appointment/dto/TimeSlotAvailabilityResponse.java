package com.example.appointment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TimeSlotAvailabilityResponse {
    private List<TimeSlotInfo> availableSlots;
    private int totalAvailableSlots;
    private String doctorName;
    private String departmentName;
    private Long departmentId;
    private LocalDate date;
    private String period;
    private List<TimeSlot> timeSlots;
    
    public static class TimeSlot {
        private String time;
        private int available;
        
        public TimeSlot() {}
        
        public TimeSlot(String time, int available) {
            this.time = time;
            this.available = available;
        }
        
        public String getTime() {
            return time;
        }
        
        public void setTime(String time) {
            this.time = time;
        }
        
        public int getAvailable() {
            return available;
        }
        
        public void setAvailable(int available) {
            this.available = available;
        }
    }
    
    public static class TimeSlotInfo {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean available;
        private String reason;
        
        public TimeSlotInfo() {}
        
        public TimeSlotInfo(LocalDateTime startTime, LocalDateTime endTime, boolean available, String reason) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.available = available;
            this.reason = reason;
        }
        
        public LocalDateTime getStartTime() {
            return startTime;
        }
        
        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
        
        public LocalDateTime getEndTime() {
            return endTime;
        }
        
        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
        
        public boolean isAvailable() {
            return available;
        }
        
        public void setAvailable(boolean available) {
            this.available = available;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
    
    public TimeSlotAvailabilityResponse() {}
    
    public TimeSlotAvailabilityResponse(List<TimeSlotInfo> availableSlots, int totalAvailableSlots, 
                                        String doctorName, String departmentName) {
        this.availableSlots = availableSlots;
        this.totalAvailableSlots = totalAvailableSlots;
        this.doctorName = doctorName;
        this.departmentName = departmentName;
    }
    
    public List<TimeSlotInfo> getAvailableSlots() {
        return availableSlots;
    }
    
    public void setAvailableSlots(List<TimeSlotInfo> availableSlots) {
        this.availableSlots = availableSlots;
    }
    
    public int getTotalAvailableSlots() {
        return totalAvailableSlots;
    }
    
    public void setTotalAvailableSlots(int totalAvailableSlots) {
        this.totalAvailableSlots = totalAvailableSlots;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }
    
    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }
}