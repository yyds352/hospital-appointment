package com.example.appointment.service;

import com.example.appointment.dto.DepartmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentDTO create(DepartmentDTO departmentDTO);
    DepartmentDTO update(Long id, DepartmentDTO departmentDTO);
    void delete(Long id);
    DepartmentDTO getById(Long id);
    Page<DepartmentDTO> search(String name, Integer status, Pageable pageable);
}