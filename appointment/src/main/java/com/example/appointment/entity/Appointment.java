package com.example.appointment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    public User getPatient() {
        return patient;
    }
    
    public void setPatient(User patient) {
        this.patient = patient;
    }

    @Column(nullable = false)
    private LocalDateTime appointmentTime;
    
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    @Column(nullable = false)
    private String status;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    @Column(length = 500)
    private String description;

    // 提醒相关字段
    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "notification_time")
    private LocalDateTime notificationTime;

    @Column(name = "conflict_checked")
    private Boolean conflictChecked = false;

    @Column(name = "conflict_level")
    private String conflictLevel;

    @Column(name = "appointment_number", unique = true, nullable = false)
    private String appointmentNumber;
    
    public String getAppointmentNumber() {
        return appointmentNumber;
    }
    
    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    @Column(name = "reminded")
    private Boolean reminded = false;
    
    public boolean isReminded() {
        return reminded != null && reminded;
    }
    
    public void setReminded(boolean reminded) {
        this.reminded = reminded;
    }
    
    @Column(name = "time_slot")
    private String timeSlot;
    
    public String getTimeSlot() {
        return timeSlot;
    }
    
    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
    
    @Column(name = "symptoms")
    private String symptoms;
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    @Column(name = "created_by")
    private String createdBy;
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getReminderSent() {
        return reminderSent;
    }
    
    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }
    
    public LocalDateTime getReminderTime() {
        return reminderTime;
    }
    
    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public Boolean getNotificationSent() {
        return notificationSent;
    }
    
    public void setNotificationSent(Boolean notificationSent) {
        this.notificationSent = notificationSent;
    }
    
    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }
    
    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }
    
    public Boolean getConflictChecked() {
        return conflictChecked;
    }
    
    public void setConflictChecked(Boolean conflictChecked) {
        this.conflictChecked = conflictChecked;
    }
    
    public String getConflictLevel() {
        return conflictLevel;
    }
    
    public void setConflictLevel(String conflictLevel) {
        this.conflictLevel = conflictLevel;
    }
}