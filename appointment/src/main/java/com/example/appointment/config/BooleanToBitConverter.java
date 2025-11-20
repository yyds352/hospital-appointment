package com.example.appointment.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Boolean到Bit(1)类型的转换器
 * 用于解决MySQL bit(1)类型与Java Boolean类型的转换问题
 */
@Converter(autoApply = true)
public class BooleanToBitConverter implements AttributeConverter<Boolean, Integer> {

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
        return dbData.intValue() == 1;
    }
}