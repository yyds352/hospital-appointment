package com.example.appointment.dto;

import lombok.Data;
import java.util.Map;

/**
 * 医生评价统计DTO
 */
@Data
public class DoctorReviewStatsDTO {
    
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String departmentName;
    
    private Double averageRating; // 平均评分
    private Long totalReviews; // 总评价数
    private Long approvedReviews; // 已审核评价数
    
    private Map<Integer, Long> ratingDistribution; // 评分分布：1-5星各有多少
    private Double averageServiceAttitude; // 平均服务态度评分
    private Double averageProfessionalSkill; // 平均专业技能评分
    private Double averageCommunicationAbility; // 平均沟通能力评分
    private Double averageTreatmentEffect; // 平均治疗效果评分
    
    private String ratingLevel; // 评级：EXCELLENT, GOOD, AVERAGE, POOR
    private String recommendationRate; // 推荐率（4-5星评价的百分比）
    
    public String getRatingLevel() {
        if (averageRating == null) return "NO_RATING";
        if (averageRating >= 4.5) return "EXCELLENT";
        if (averageRating >= 4.0) return "GOOD";
        if (averageRating >= 3.0) return "AVERAGE";
        return "POOR";
    }
    
    public String getRecommendationRate() {
        if (ratingDistribution == null || totalReviews == null || totalReviews == 0) {
            return "0%";
        }
        long highRatings = ratingDistribution.getOrDefault(4, 0L) + ratingDistribution.getOrDefault(5, 0L);
        return String.format("%.1f%%", (highRatings * 100.0) / totalReviews);
    }
    
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
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Long getTotalReviews() {
        return totalReviews;
    }
    
    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
    
    public Long getApprovedReviews() {
        return approvedReviews;
    }
    
    public void setApprovedReviews(Long approvedReviews) {
        this.approvedReviews = approvedReviews;
    }
    
    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }
    
    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }
    
    public Double getAverageServiceAttitude() {
        return averageServiceAttitude;
    }
    
    public void setAverageServiceAttitude(Double averageServiceAttitude) {
        this.averageServiceAttitude = averageServiceAttitude;
    }
    
    public Double getAverageProfessionalSkill() {
        return averageProfessionalSkill;
    }
    
    public void setAverageProfessionalSkill(Double averageProfessionalSkill) {
        this.averageProfessionalSkill = averageProfessionalSkill;
    }
    
    public Double getAverageCommunicationAbility() {
        return averageCommunicationAbility;
    }
    
    public void setAverageCommunicationAbility(Double averageCommunicationAbility) {
        this.averageCommunicationAbility = averageCommunicationAbility;
    }
    
    public Double getAverageTreatmentEffect() {
        return averageTreatmentEffect;
    }
    
    public void setAverageTreatmentEffect(Double averageTreatmentEffect) {
        this.averageTreatmentEffect = averageTreatmentEffect;
    }
    
    public void setRatingLevel(String ratingLevel) {
        this.ratingLevel = ratingLevel;
    }
    
    public void setRecommendationRate(String recommendationRate) {
        this.recommendationRate = recommendationRate;
    }
}