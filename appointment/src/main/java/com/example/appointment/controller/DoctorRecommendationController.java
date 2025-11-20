package com.example.appointment.controller;

import com.example.appointment.dto.DoctorRecommendationDTO;
import com.example.appointment.service.DoctorRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/doctor-recommendations")
@RequiredArgsConstructor
@Tag(name = "医生推荐", description = "医生推荐相关接口")
public class DoctorRecommendationController {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorRecommendationController.class);
    
    private final DoctorRecommendationService doctorRecommendationService;
    
    @GetMapping("/for-patient")
    @Operation(summary = "为患者推荐医生", description = "基于患者历史记录和偏好推荐医生")
    public ResponseEntity<List<DoctorRecommendationDTO>> recommendDoctorsForPatient(
            @RequestParam @Parameter(description = "患者ID") Long patientId,
            @RequestParam @Parameter(description = "科室代码") String departmentCode,
            @RequestParam(required = false) @Parameter(description = "症状描述") String symptom) {
        log.info("为患者推荐医生请求: patientId={}, departmentCode={}, symptom={}", patientId, departmentCode, symptom);
        List<DoctorRecommendationDTO> recommendations = doctorRecommendationService.recommendDoctorsForPatient(patientId, departmentCode, symptom);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "推荐高评分医生", description = "基于评分和统计数据推荐高评分医生")
    public ResponseEntity<List<DoctorRecommendationDTO>> recommendTopRatedDoctors(
            @RequestParam @Parameter(description = "科室代码") String departmentCode,
            @RequestParam(defaultValue = "5") @Parameter(description = "推荐数量") int limit) {
        log.info("推荐高评分医生请求: departmentCode={}, limit={}", departmentCode, limit);
        List<DoctorRecommendationDTO> recommendations = doctorRecommendationService.recommendTopRatedDoctors(departmentCode, limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/by-symptom")
    @Operation(summary = "基于症状推荐医生", description = "根据症状描述推荐合适的医生")
    public ResponseEntity<List<DoctorRecommendationDTO>> recommendDoctorsBySymptom(
            @RequestParam @Parameter(description = "症状描述") String symptom,
            @RequestParam @Parameter(description = "科室代码") String departmentCode) {
        log.info("基于症状推荐医生请求: symptom={}, departmentCode={}", symptom, departmentCode);
        List<DoctorRecommendationDTO> recommendations = doctorRecommendationService.recommendDoctorsBySymptom(symptom, departmentCode);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/by-similar-patients")
    @Operation(summary = "基于相似患者推荐医生", description = "根据相似患者的选择推荐医生")
    public ResponseEntity<List<DoctorRecommendationDTO>> recommendDoctorsBySimilarPatients(
            @RequestParam @Parameter(description = "患者ID") Long patientId,
            @RequestParam @Parameter(description = "科室代码") String departmentCode) {
        log.info("基于相似患者推荐医生请求: patientId={}, departmentCode={}", patientId, departmentCode);
        List<DoctorRecommendationDTO> recommendations = doctorRecommendationService.recommendDoctorsBySimilarPatients(patientId, departmentCode);
        return ResponseEntity.ok(recommendations);
    }
}