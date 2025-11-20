package com.example.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
@Schema(description = "医生推荐数据传输对象")
public class DoctorRecommendationDTO {
    
    @Schema(description = "医生ID")
    private Long doctorId;
    
    @Schema(description = "医生姓名")
    private String doctorName;
    
    @Schema(description = "医生职称")
    private String doctorTitle;
    
    @Schema(description = "科室名称")
    private String departmentName;
    
    @Schema(description = "科室代码")
    private String departmentCode;
    
    @Schema(description = "平均评分")
    private Double averageRating;
    
    @Schema(description = "评价数量")
    private Long reviewCount;
    
    @Schema(description = "推荐分数")
    private Double score;
    
    @Schema(description = "评分分数")
    private Double ratingScore;
    
    @Schema(description = "可用性分数")
    private Double availabilityScore;
    
    @Schema(description = "经验分数")
    private Double experienceScore;
    
    @Schema(description = "症状匹配分数")
    private Double symptomMatchScore;
    
    @Schema(description = "患者历史分数")
    private Double patientHistoryScore;
    
    @Schema(description = "相似患者选择分数")
    private Double similarPatientChoiceScore;
    
    @Schema(description = "推荐理由")
    private String recommendationReason;
    
    @Schema(description = "评分分布")
    private Map<Integer, Long> ratingDistribution;
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Long getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public Double getRatingScore() {
        return ratingScore;
    }
    
    public void setRatingScore(Double ratingScore) {
        this.ratingScore = ratingScore;
    }
    
    public Double getAvailabilityScore() {
        return availabilityScore;
    }
    
    public void setAvailabilityScore(Double availabilityScore) {
        this.availabilityScore = availabilityScore;
    }
    
    public Double getExperienceScore() {
        return experienceScore;
    }
    
    public void setExperienceScore(Double experienceScore) {
        this.experienceScore = experienceScore;
    }
    
    public Double getSymptomMatchScore() {
        return symptomMatchScore;
    }
    
    public void setSymptomMatchScore(Double symptomMatchScore) {
        this.symptomMatchScore = symptomMatchScore;
    }
    
    public Double getPatientHistoryScore() {
        return patientHistoryScore;
    }
    
    public void setPatientHistoryScore(Double patientHistoryScore) {
        this.patientHistoryScore = patientHistoryScore;
    }
    
    public Double getSimilarPatientChoiceScore() {
        return similarPatientChoiceScore;
    }
    
    public void setSimilarPatientChoiceScore(Double similarPatientChoiceScore) {
        this.similarPatientChoiceScore = similarPatientChoiceScore;
    }
    
    public String getRecommendationReason() {
        return recommendationReason;
    }
    
    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }
    
    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }
    
    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }
}