package com.example.appointment.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间段工具类
 * 用于处理时间段相关的工具方法
 */
@Component
public class TimeSlotUtil {
    
    /**
     * 根据时间获取时间段标识
     * @param dateTime 日期时间
     * @return 时间段标识 (MORNING/AFTERNOON/EVENING)
     */
    public String getTimeSlot(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        
        if (time.isAfter(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(12, 0))) {
            return "MORNING";
        } else if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            return "AFTERNOON";
        } else if (time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(21, 0))) {
            return "EVENING";
        }
        
        return "OTHER";
    }
    
    /**
     * 获取时间段的中文名称
     * @param timeSlot 时间段标识
     * @return 中文名称
     */
    public String getTimeSlotName(String timeSlot) {
        switch (timeSlot) {
            case "MORNING":
                return "上午";
            case "AFTERNOON":
                return "下午";
            case "EVENING":
                return "晚上";
            default:
                return "其他";
        }
    }
    
    /**
     * 判断是否为工作时间
     * @param dateTime 日期时间
     * @return 是否为工作时间
     */
    public boolean isWorkingTime(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        
        // 上午工作时间：8:00-12:00
        boolean isMorning = time.isAfter(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(12, 0));
        // 下午工作时间：14:00-18:00
        boolean isAfternoon = time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(18, 0));
        
        return isMorning || isAfternoon;
    }
    
    /**
     * 获取下一个可用时间段
     * @param dateTime 当前时间
     * @return 下一个可用时间段
     */
    public LocalDateTime getNextAvailableTimeSlot(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        
        // 如果在上午工作时间内，返回当前时间
        if (time.isAfter(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(12, 0))) {
            return dateTime;
        }
        
        // 如果在下午工作时间内，返回当前时间
        if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            return dateTime;
        }
        
        // 如果早于上午工作时间，返回上午9:00
        if (time.isBefore(LocalTime.of(8, 0))) {
            return dateTime.with(LocalTime.of(9, 0));
        }
        
        // 如果上午工作时间已过但早于下午工作时间，返回下午14:00
        if (time.isAfter(LocalTime.of(12, 0)) && time.isBefore(LocalTime.of(14, 0))) {
            return dateTime.with(LocalTime.of(14, 0));
        }
        
        // 如果晚于下午工作时间，返回第二天上午9:00
        return dateTime.plusDays(1).with(LocalTime.of(9, 0));
    }
}