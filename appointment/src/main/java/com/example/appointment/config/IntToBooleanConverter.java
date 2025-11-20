package com.example.appointment.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Integer到Boolean的转换器
 * 用于处理数据库中int类型到Java中Boolean类型的转换
 */
@Converter
public class IntToBooleanConverter implements AttributeConverter<Boolean, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return 0;
        }
        return attribute ? 1 : 0;
    }
    
    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return false;
        }
        return dbData != 0;
    }
}