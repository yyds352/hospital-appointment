package com.example.appointment.enums;

public enum UserRole {
    STUDENT("学生"),
    TEACHER("教师"),
    DOCTOR("医生"),
    ADMIN("管理员");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 