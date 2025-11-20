package com.example.appointment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生评价DTO
 */
@Data
public class DoctorReviewDTO {
    
    private Long id;
    
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    private String doctorName;
    private String doctorTitle;
    private String departmentName;
    
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    
    private String patientName;
    
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1星")
    @Max(value = 5, message = "评分不能大于5星")
    private Integer rating; // 总体评分：1-5星
    
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title; // 评价标题
    
    @NotBlank(message = "评价内容不能为空")
    @Size(max = 1000, message = "评价内容长度不能超过1000字符")
    private String content; // 评价内容
    
    @NotNull(message = "服务态度评分不能为空")
    @Min(value = 1, message = "服务态度评分不能小于1星")
    @Max(value = 5, message = "服务态度评分不能大于5星")
    private Integer serviceAttitude; // 服务态度评分：1-5
    
    @NotNull(message = "专业技能评分不能为空")
    @Min(value = 1, message = "专业技能评分不能小于1星")
    @Max(value = 5, message = "专业技能评分不能大于5星")
    private Integer professionalSkill; // 专业技能评分：1-5
    
    @NotNull(message = "沟通能力评分不能为空")
    @Min(value = 1, message = "沟通能力评分不能小于1星")
    @Max(value = 5, message = "沟通能力评分不能大于5星")
    private Integer communicationAbility; // 沟通能力评分：1-5
    
    @NotNull(message = "治疗效果评分不能为空")
    @Min(value = 1, message = "治疗效果评分不能小于1星")
    @Max(value = 5, message = "治疗效果评分不能大于5星")
    private Integer treatmentEffect; // 治疗效果评分：1-5
    
    private Boolean isAnonymous = false; // 是否匿名评价
    
    private String status = "PENDING"; // 状态：PENDING, APPROVED, REJECTED
    
    private String adminReply; // 管理员回复
    
    private LocalDateTime adminReplyTime; // 管理员回复时间
    
    private Integer helpfulCount = 0; // 有帮助计数
    
    private Integer unhelpfulCount = 0; // 无帮助计数
    
    private LocalDateTime createTime; // 评价创建时间
    
    private LocalDateTime updateTime; // 评价更新时间
    
    /**
     * 计算平均评分
     */
    public Double getAverageRating() {
        if (serviceAttitude != null && professionalSkill != null && 
            communicationAbility != null && treatmentEffect != null) {
            return (serviceAttitude + professionalSkill + communicationAbility + treatmentEffect) / 4.0;
        }
        return rating != null ? rating.doubleValue() : 0.0;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public Long getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getServiceAttitude() {
        return serviceAttitude;
    }
    
    public void setServiceAttitude(Integer serviceAttitude) {
        this.serviceAttitude = serviceAttitude;
    }
    
    public Integer getProfessionalSkill() {
        return professionalSkill;
    }
    
    public void setProfessionalSkill(Integer professionalSkill) {
        this.professionalSkill = professionalSkill;
    }
    
    public Integer getCommunicationAbility() {
        return communicationAbility;
    }
    
    public void setCommunicationAbility(Integer communicationAbility) {
        this.communicationAbility = communicationAbility;
    }
    
    public Integer getTreatmentEffect() {
        return treatmentEffect;
    }
    
    public void setTreatmentEffect(Integer treatmentEffect) {
        this.treatmentEffect = treatmentEffect;
    }
    
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }
    
    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAdminReply() {
        return adminReply;
    }
    
    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }
    
    public LocalDateTime getAdminReplyTime() {
        return adminReplyTime;
    }
    
    public void setAdminReplyTime(LocalDateTime adminReplyTime) {
        this.adminReplyTime = adminReplyTime;
    }
    
    public Integer getHelpfulCount() {
        return helpfulCount;
    }
    
    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }
    
    public Integer getUnhelpfulCount() {
        return unhelpfulCount;
    }
    
    public void setUnhelpfulCount(Integer unhelpfulCount) {
        this.unhelpfulCount = unhelpfulCount;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
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
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}