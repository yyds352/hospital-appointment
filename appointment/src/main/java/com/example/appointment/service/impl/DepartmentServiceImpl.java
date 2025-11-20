package com.example.appointment.service.impl;

import com.example.appointment.dto.DepartmentDTO;
import com.example.appointment.entity.Department;
import com.example.appointment.repository.DepartmentRepository;
import com.example.appointment.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentDTO create(DepartmentDTO departmentDTO) {
        // 检查科室名称是否已存在
        if (departmentRepository.existsByName(departmentDTO.getName())) {
            throw new RuntimeException("科室名称已存在");
        }

        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setStatus(departmentDTO.getStatus());

        department = departmentRepository.save(department);
        return convertToDTO(department);
    }

    @Override
    @Transactional
    public DepartmentDTO update(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("科室不存在"));

        // 如果修改了名称，需要检查新名称是否已存在
        if (!department.getName().equals(departmentDTO.getName()) &&
                departmentRepository.existsByName(departmentDTO.getName())) {
            throw new RuntimeException("科室名称已存在");
        }

        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setStatus(departmentDTO.getStatus());

        department = departmentRepository.save(department);
        return convertToDTO(department);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsByIdCustom(id)) {
            throw new RuntimeException("科室不存在");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    public DepartmentDTO getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("科室不存在"));
        return convertToDTO(department);
    }

    @Override
    public Page<DepartmentDTO> search(String name, Integer status, Pageable pageable) {
        Page<Department> departments;
        if (name != null && !name.isEmpty()) {
            departments = departmentRepository.findDepartmentsByName(name, pageable);
        } else if (status != null) {
            departments = departmentRepository.findByStatus(status, pageable);
        } else {
            departments = departmentRepository.findAll(pageable);
        }
        return departments.map(this::convertToDTO);
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setStatus(department.getStatus());
        return dto;
    }
}