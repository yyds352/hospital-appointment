package com.example.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    
    private Long userId;
    
    public Long getUserId() {
        return userId;
    }
    
    @NotNull(message = "科室ID不能为空")
    private Long departmentId;
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    @NotBlank(message = "医生姓名不能为空")
    private String name;
    
    @NotBlank(message = "职称不能为空")
    private String title;
    
    private String specialty;
    
    private String introduction;
    
    private String photoUrl;
    
    private Integer status;

    // 创建医生时的用户信息
    private String username;
    
    private String password;
    
    private String confirmPassword;
    
    private String phone;
    
    private String email;
    
    // 关联信息
    private String departmentName;
    
    public String getName() {
        return name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public String getIntroduction() {
        return introduction;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
    
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}