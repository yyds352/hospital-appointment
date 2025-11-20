package com.example.appointment.service;

import com.example.appointment.dto.DoctorDTO;
import com.example.appointment.dto.DoctorRecommendationDTO;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.User;
import com.example.appointment.entity.Appointment;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.DoctorReviewRepository;
import com.example.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorRecommendationService {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorRecommendationService.class);
    
    private final DoctorRepository doctorRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final AppointmentRepository appointmentRepository;
    
    /**
     * 基于患者历史记录和偏好推荐医生
     */
    public List<DoctorRecommendationDTO> recommendDoctorsForPatient(Long patientId, String departmentCode, String symptom) {
        log.info("为患者推荐医生: patientId={}, departmentCode={}, symptom={}", patientId, departmentCode, symptom);
        
        // 获取科室内的医生
        List<Doctor> doctors = doctorRepository.findByDepartmentCode(departmentCode);
        
        // 计算每个医生的推荐分数
        List<DoctorRecommendationDTO> recommendations = doctors.stream()
                .map(doctor -> calculateDoctorScore(doctor, patientId, symptom))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(5) // 返回前5个推荐
                .collect(Collectors.toList());
        
        log.info("医生推荐完成，返回 {} 个推荐", recommendations.size());
        return recommendations;
    }
    
    /**
     * 基于评分和统计数据的医生推荐
     */
    public List<DoctorRecommendationDTO> recommendTopRatedDoctors(String departmentCode, int limit) {
        log.info("推荐高评分医生: departmentCode={}, limit={}", departmentCode, limit);
        
        // 获取科室内的医生及其平均评分
        List<Object[]> doctorRatings = doctorReviewRepository.getAverageRatingByDepartment(departmentCode);
        
        return doctorRatings.stream()
                .map(result -> {
                    Doctor doctor = (Doctor) result[0];
                    Double avgRating = (Double) result[1];
                    Long reviewCount = (Long) result[2];
                    
                    DoctorRecommendationDTO recommendation = new DoctorRecommendationDTO();
                    recommendation.setDoctorId(doctor.getId());
                    recommendation.setDoctorName(doctor.getName());
                    recommendation.setDoctorTitle(doctor.getTitle());
                    recommendation.setDepartmentName(doctor.getDepartment().getName());
                    recommendation.setDepartmentCode(doctor.getDepartment().getCode());
                    recommendation.setAverageRating(avgRating);
                    recommendation.setReviewCount(reviewCount);
                    recommendation.setScore(avgRating * Math.log(reviewCount + 1)); // 基于评分和评论数量的综合分数
                    
                    return recommendation;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 基于症状的智能医生推荐
     */
    public List<DoctorRecommendationDTO> recommendDoctorsBySymptom(String symptom, String departmentCode) {
        log.info("基于症状推荐医生: symptom={}, departmentCode={}", symptom, departmentCode);
        
        // 获取相关科室的医生
        List<Doctor> doctors = doctorRepository.findByDepartmentCode(departmentCode);
        
        // 基于症状匹配度和医生专长进行推荐
        return doctors.stream()
                .map(doctor -> {
                    double symptomMatchScore = calculateSymptomMatchScore(doctor, symptom);
                    double ratingScore = getDoctorRatingScore(doctor.getId());
                    double availabilityScore = getDoctorAvailabilityScore(doctor.getId());
                    
                    DoctorRecommendationDTO recommendation = new DoctorRecommendationDTO();
                    recommendation.setDoctorId(doctor.getId());
                    recommendation.setDoctorName(doctor.getName());
                    recommendation.setDoctorTitle(doctor.getTitle());
                    recommendation.setDepartmentName(doctor.getDepartment().getName());
                    recommendation.setDepartmentCode(doctor.getDepartment().getCode());
                    recommendation.setAverageRating(getDoctorAverageRating(doctor.getId()));
                    recommendation.setReviewCount(getDoctorReviewCount(doctor.getId()));
                    recommendation.setSymptomMatchScore(symptomMatchScore);
                    recommendation.setAvailabilityScore(availabilityScore);
                    recommendation.setScore(symptomMatchScore * 0.4 + ratingScore * 0.4 + availabilityScore * 0.2);
                    
                    return recommendation;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取相似患者的医生推荐
     */
    public List<DoctorRecommendationDTO> recommendDoctorsBySimilarPatients(Long patientId, String departmentCode) {
        log.info("基于相似患者推荐医生: patientId={}, departmentCode={}", patientId, departmentCode);
        
        // 获取患者的历史预约记录
        List<Appointment> patientAppointments = appointmentRepository.findByPatientId(patientId);
        
        // 获取相似患者的预约记录（这里简化处理，实际可以更复杂）
        Set<Long> similarPatientIds = findSimilarPatients(patientId, patientAppointments);
        
        // 基于相似患者的选择推荐医生
        Map<Long, Double> doctorFrequency = new HashMap<>();
        for (Long similarPatientId : similarPatientIds) {
            List<Appointment> similarAppointments = appointmentRepository.findByPatientId(similarPatientId);
            for (Appointment appointment : similarAppointments) {
                if (departmentCode.equals(appointment.getDoctor().getDepartment().getCode())) {
                    doctorFrequency.merge(appointment.getDoctor().getId(), 1.0, Double::sum);
                }
            }
        }
        
        return doctorFrequency.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Doctor doctor = doctorRepository.findById(entry.getKey()).orElse(null);
                    if (doctor == null) return null;
                    
                    DoctorRecommendationDTO recommendation = new DoctorRecommendationDTO();
                    recommendation.setDoctorId(doctor.getId());
                    recommendation.setDoctorName(doctor.getName());
                    recommendation.setDoctorTitle(doctor.getTitle());
                    recommendation.setDepartmentName(doctor.getDepartment().getName());
                    recommendation.setDepartmentCode(doctor.getDepartment().getCode());
                    recommendation.setAverageRating(getDoctorAverageRating(doctor.getId()));
                    recommendation.setReviewCount(getDoctorReviewCount(doctor.getId()));
                    recommendation.setSimilarPatientChoiceScore(entry.getValue());
                    recommendation.setScore(entry.getValue() * 1.0);
                    
                    return recommendation;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private DoctorRecommendationDTO calculateDoctorScore(Doctor doctor, Long patientId, String symptom) {
        DoctorRecommendationDTO recommendation = new DoctorRecommendationDTO();
        recommendation.setDoctorId(doctor.getId());
        recommendation.setDoctorName(doctor.getName());
        recommendation.setDoctorTitle(doctor.getTitle());
        recommendation.setDepartmentName(doctor.getDepartment().getName());
        recommendation.setDepartmentCode(doctor.getDepartment().getCode());
        
        // 计算各项分数
        double ratingScore = getDoctorRatingScore(doctor.getId());
        double availabilityScore = getDoctorAvailabilityScore(doctor.getId());
        double experienceScore = getDoctorExperienceScore(doctor);
        double symptomMatchScore = calculateSymptomMatchScore(doctor, symptom);
        double patientHistoryScore = getPatientHistoryScore(doctor.getId(), patientId);
        
        // 综合分数
        double totalScore = ratingScore * 0.3 + availabilityScore * 0.25 + 
                           experienceScore * 0.2 + symptomMatchScore * 0.15 + 
                           patientHistoryScore * 0.1;
        
        recommendation.setAverageRating(getDoctorAverageRating(doctor.getId()));
        recommendation.setReviewCount(getDoctorReviewCount(doctor.getId()));
        recommendation.setRatingScore(ratingScore);
        recommendation.setAvailabilityScore(availabilityScore);
        recommendation.setExperienceScore(experienceScore);
        recommendation.setSymptomMatchScore(symptomMatchScore);
        recommendation.setPatientHistoryScore(patientHistoryScore);
        recommendation.setScore(totalScore);
        
        return recommendation;
    }
    
    private double getDoctorRatingScore(Long doctorId) {
        Double avgRating = doctorReviewRepository.getAverageRatingByDoctorId(doctorId);
        Long reviewCount = doctorReviewRepository.countByDoctorIdAndStatus(doctorId, "APPROVED");
        
        if (avgRating == null || reviewCount == 0) {
            return 0.5; // 默认分数
        }
        
        // 基于评分和评论数量的综合分数
        double ratingScore = (avgRating / 5.0) * 0.7;
        double countScore = Math.min(reviewCount / 10.0, 1.0) * 0.3;
        
        return ratingScore + countScore;
    }
    
    private double getDoctorAvailabilityScore(Long doctorId) {
        // 这里可以查询医生的排班和预约情况
        // 简化处理：返回一个基于历史数据的可用性分数
        long upcomingAppointments = appointmentRepository.countUpcomingAppointmentsByDoctor(doctorId, LocalDateTime.now());
        
        // 假设医生每天最多看20个病人
        double availability = Math.max(0, 1.0 - (upcomingAppointments / 20.0));
        return availability;
    }
    
    private double getDoctorExperienceScore(Doctor doctor) {
        // 基于医生的职称和工作年限计算经验分数
        double titleScore = 0.0;
        switch (doctor.getTitle()) {
            case "主任医师":
                titleScore = 1.0;
                break;
            case "副主任医师":
                titleScore = 0.8;
                break;
            case "主治医师":
                titleScore = 0.6;
                break;
            default:
                titleScore = 0.4;
        }
        
        // 假设工作年限影响经验（这里简化处理）
        double experienceScore = 0.7; // 可以基于实际数据计算
        
        return titleScore * 0.6 + experienceScore * 0.4;
    }
    
    private double calculateSymptomMatchScore(Doctor doctor, String symptom) {
        if (symptom == null || symptom.isEmpty()) {
            return 0.5; // 默认分数
        }
        
        // 基于症状和医生专长的匹配度
        // 这里可以实现更复杂的症状匹配逻辑
        String doctorSpecialty = doctor.getSpecialty();
        if (doctorSpecialty != null && doctorSpecialty.toLowerCase().contains(symptom.toLowerCase())) {
            return 0.9;
        }
        
        // 基于科室的匹配
        String departmentName = doctor.getDepartment().getName();
        if (departmentName.contains("内科") && symptom.contains("发烧")) {
            return 0.8;
        } else if (departmentName.contains("外科") && symptom.contains("疼痛")) {
            return 0.8;
        } else if (departmentName.contains("儿科") && symptom.contains("儿童")) {
            return 0.9;
        }
        
        return 0.6; // 默认匹配分数
    }
    
    private double getPatientHistoryScore(Long doctorId, Long patientId) {
        // 检查患者是否之前看过这个医生
        long previousAppointments = appointmentRepository.countByPatientIdAndDoctorId(patientId, doctorId);
        
        if (previousAppointments > 0) {
            return 0.8; // 有历史记录，分数较高
        }
        
        return 0.3; // 无历史记录
    }
    
    private double getDoctorAverageRating(Long doctorId) {
        Double avgRating = doctorReviewRepository.getAverageRatingByDoctorId(doctorId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    private long getDoctorReviewCount(Long doctorId) {
        return doctorReviewRepository.countByDoctorIdAndStatus(doctorId, "APPROVED");
    }
    
    private Set<Long> findSimilarPatients(Long patientId, List<Appointment> patientAppointments) {
        // 简化处理：返回有相似预约历史的患者ID
        // 实际实现可以更复杂，基于年龄、性别、症状等特征
        Set<Long> similarPatients = new HashSet<>();
        
        for (Appointment appointment : patientAppointments) {
            String departmentCode = appointment.getDoctor().getDepartment().getCode();
            List<Appointment> sameDepartmentAppointments = appointmentRepository
                    .findByDoctorDepartmentCodeAndPatientIdNot(departmentCode, patientId);
            
            for (Appointment sameDeptAppt : sameDepartmentAppointments) {
                similarPatients.add(sameDeptAppt.getPatient().getId());
                if (similarPatients.size() >= 10) { // 限制数量
                    break;
                }
            }
        }
        
        return similarPatients;
    }
}