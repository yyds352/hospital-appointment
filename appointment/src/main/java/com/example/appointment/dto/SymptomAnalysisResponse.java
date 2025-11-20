package com.example.appointment.dto;

import java.util.List;

public class SymptomAnalysisResponse {
    private Long departmentId;
    private String departmentName;
    private double matchScore;
    private List<String> matchedKeywords;
    private String analysisReason;
    
    public SymptomAnalysisResponse() {}
    
    public SymptomAnalysisResponse(Long departmentId, String departmentName, double matchScore, 
                                   List<String> matchedKeywords, String analysisReason) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.matchScore = matchScore;
        this.matchedKeywords = matchedKeywords;
        this.analysisReason = analysisReason;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public double getMatchScore() {
        return matchScore;
    }
    
    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }
    
    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }
    
    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }
    
    public String getAnalysisReason() {
        return analysisReason;
    }
    
    public void setAnalysisReason(String analysisReason) {
        this.analysisReason = analysisReason;
    }
}