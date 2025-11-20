package com.example.appointment.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    STUDENT("学生"),
    TEACHER("教师"),
    DOCTOR("医生"),
    ADMIN("管理员");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }
} 