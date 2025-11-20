package com.example.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorInfoDTO {
    @NotBlank(message = "职称不能为空")
    @Size(min = 2, max = 50, message = "职称长度在2-50个字符之间")
    private String title;
    
    @NotNull(message = "科室ID不能为空")
    private Long departmentId;
    
    @NotBlank(message = "专业特长不能为空")
    @Size(min = 2, max = 200, message = "专业特长长度在2-200个字符之间")
    private String specialty;
    
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String introduction;
}