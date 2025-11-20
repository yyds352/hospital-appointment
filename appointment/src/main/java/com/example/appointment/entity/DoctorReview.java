package com.example.appointment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.appointment.config.IntToBooleanConverter;

import java.time.LocalDateTime;

/**
 * 医生评价实体
 */
@Entity
@Table(name = "doctor_reviews")
@Data
@EntityListeners(AuditingEntityListener.class)
public class DoctorReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
    
    @Column(name = "rating", nullable = false)
    private Integer rating; // 评分：1-5星
    
    @Column(name = "title", length = 100)
    private String title; // 评价标题
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 评价内容
    
    @Column(name = "service_attitude")
    private Integer serviceAttitude; // 服务态度评分：1-5
    
    @Column(name = "professional_skill")
    private Integer professionalSkill; // 专业技能评分：1-5
    
    @Column(name = "communication_ability")
    private Integer communicationAbility; // 沟通能力评分：1-5
    
    @Column(name = "treatment_effect")
    private Integer treatmentEffect; // 治疗效果评分：1-5
    
    @Column(name = "is_anonymous", nullable = false)
    @Convert(converter = IntToBooleanConverter.class)
    private Boolean isAnonymous = false; // 是否匿名评价
    
    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // 状态：PENDING, APPROVED, REJECTED
    
    @Column(name = "admin_reply")
    private String adminReply; // 管理员回复
    
    @Column(name = "admin_reply_time")
    private LocalDateTime adminReplyTime; // 管理员回复时间
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0; // 有帮助计数
    
    @Column(name = "unhelpful_count")
    private Integer unhelpfulCount = 0; // 无帮助计数
    
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 评价创建时间
    
    @Column(name = "update_time")
    private LocalDateTime updateTime; // 评价更新时间
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
    
    public void setPatient(User patient) {
        this.patient = patient;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setServiceAttitude(Integer serviceAttitude) {
        this.serviceAttitude = serviceAttitude;
    }
    
    public void setProfessionalSkill(Integer professionalSkill) {
        this.professionalSkill = professionalSkill;
    }
    
    public void setCommunicationAbility(Integer communicationAbility) {
        this.communicationAbility = communicationAbility;
    }
    
    public void setTreatmentEffect(Integer treatmentEffect) {
        this.treatmentEffect = treatmentEffect;
    }
    
    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }
    
    public void setAdminReplyTime(LocalDateTime adminReplyTime) {
        this.adminReplyTime = adminReplyTime;
    }
    
    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }
    
    public void setUnhelpfulCount(Integer unhelpfulCount) {
        this.unhelpfulCount = unhelpfulCount;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Long getId() {
        return id;
    }
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public User getPatient() {
        return patient;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public Integer getServiceAttitude() {
        return serviceAttitude;
    }
    
    public Integer getProfessionalSkill() {
        return professionalSkill;
    }
    
    public Integer getCommunicationAbility() {
        return communicationAbility;
    }
    
    public Integer getTreatmentEffect() {
        return treatmentEffect;
    }
    
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getAdminReply() {
        return adminReply;
    }
    
    public LocalDateTime getAdminReplyTime() {
        return adminReplyTime;
    }
    
    public Integer getHelpfulCount() {
        return helpfulCount;
    }
    
    public Integer getUnhelpfulCount() {
        return unhelpfulCount;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}