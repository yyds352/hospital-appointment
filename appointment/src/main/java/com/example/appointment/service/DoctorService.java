package com.example.appointment.service;

import com.example.appointment.dto.DoctorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    DoctorDTO create(DoctorDTO doctorDTO);
    DoctorDTO update(Long id, DoctorDTO doctorDTO);
    void delete(Long id);
    DoctorDTO getById(Long id);
    Page<DoctorDTO> search(String name, Long departmentId, Integer status, Pageable pageable);
    List<DoctorDTO> getByDepartment(Long departmentId);

    /**
     * 根据用户ID获取医生信息
     *
     * @param userId 用户ID
     * @return 医生信息
     */
    DoctorDTO getDoctorByUserId(Long userId);
}