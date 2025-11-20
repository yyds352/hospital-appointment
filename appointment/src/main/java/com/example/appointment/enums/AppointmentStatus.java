package com.example.appointment.enums;

public enum AppointmentStatus {
    PENDING("待确认"),
    CONFIRMED("已确认"),
    CANCELLED("已取消"),
    COMPLETED("已完成");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 