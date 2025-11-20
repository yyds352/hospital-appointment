package com.example.appointment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "appointment.conflict")
public class ConflictDetectionConfig {

    /**
     * 冲突检测时间窗口（分钟）
     */
    private int timeWindowMinutes = 60;

    /**
     * 最小冲突间隔时间（分钟）
     */
    private int minConflictIntervalMinutes = 30;

    /**
     * 是否启用冲突检测
     */
    private boolean enabled = true;

    /**
     * 高冲突风险阈值（同一时间段内的最大预约数）
     */
    private int highRiskThreshold = 3;

    /**
     * 中冲突风险阈值
     */
    private int mediumRiskThreshold = 2;

    /**
     * 提前提醒时间（小时）
     */
    private Duration reminderAdvanceTime = Duration.ofHours(24);

    /**
     * 重复提醒间隔（小时）
     */
    private Duration reminderRepeatInterval = Duration.ofHours(6);

    /**
     * 最大提醒次数
     */
    private int maxReminderCount = 3;

    /**
     * 是否启用智能建议
     */
    private boolean smartSuggestionsEnabled = true;

    /**
     * 建议时间窗口（天）
     */
    private int suggestionTimeWindowDays = 7;

    /**
     * 替代医生推荐数量
     */
    private int alternativeDoctorCount = 3;

    /**
     * 时间冲突权重
     */
    private double timeConflictWeight = 0.6;

    /**
     * 医生可用性权重
     */
    private double doctorAvailabilityWeight = 0.4;
}