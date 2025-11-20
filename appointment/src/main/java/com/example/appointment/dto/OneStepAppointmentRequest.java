package com.example.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class OneStepAppointmentRequest {
    private Long departmentId; // 可选，如果未指定则使用AI推荐
    private String symptoms; // 症状描述
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime; // 预约时间
    
    private AIRecommendation aiRecommendation; // AI推荐信息
    
    public static class AIRecommendation {
        private Long departmentId;
        private Integer matchScore;
        private String[] matchedKeywords;
        
        public Long getDepartmentId() {
            return departmentId;
        }
        
        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }
        
        public Integer getMatchScore() {
            return matchScore;
        }
        
        public void setMatchScore(Integer matchScore) {
            this.matchScore = matchScore;
        }
        
        public String[] getMatchedKeywords() {
            return matchedKeywords;
        }
        
        public void setMatchedKeywords(String[] matchedKeywords) {
            this.matchedKeywords = matchedKeywords;
        }
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public AIRecommendation getAiRecommendation() {
        return aiRecommendation;
    }
    
    public void setAiRecommendation(AIRecommendation aiRecommendation) {
        this.aiRecommendation = aiRecommendation;
    }
}