package com.example.appointment.service.impl;

import com.example.appointment.dto.DoctorReviewDTO;
import com.example.appointment.dto.DoctorReviewStatsDTO;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.DoctorReview;
import com.example.appointment.entity.User;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.DoctorReviewRepository;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.service.DoctorReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorReviewServiceImpl implements DoctorReviewService {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorReviewServiceImpl.class);
    
    private final DoctorReviewRepository doctorReviewRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Override
    @Transactional
    public DoctorReviewDTO createReview(DoctorReviewDTO reviewDTO) {
        log.info("创建医生评价: {}", reviewDTO);
        
        // 验证患者是否可以评价此预约
        if (!canReviewAppointment(reviewDTO.getPatientId(), reviewDTO.getAppointmentId())) {
            throw new RuntimeException("无法对此预约进行评价");
        }
        
        // 检查是否已评价
        if (doctorReviewRepository.existsByAppointmentId(reviewDTO.getAppointmentId())) {
            throw new RuntimeException("该预约已评价");
        }
        
        Doctor doctor = doctorRepository.findById(reviewDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        
        User patient = userRepository.findById(reviewDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("患者不存在"));
        
        Appointment appointment = appointmentRepository.findById(reviewDTO.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        DoctorReview review = new DoctorReview();
        review.setDoctor(doctor);
        review.setPatient(patient);
        review.setAppointment(appointment);
        review.setRating(reviewDTO.getRating());
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setServiceAttitude(reviewDTO.getServiceAttitude());
        review.setProfessionalSkill(reviewDTO.getProfessionalSkill());
        review.setCommunicationAbility(reviewDTO.getCommunicationAbility());
        review.setTreatmentEffect(reviewDTO.getTreatmentEffect());
        review.setIsAnonymous(reviewDTO.getIsAnonymous());
        review.setStatus("PENDING");
        
        DoctorReview savedReview = doctorReviewRepository.save(review);
        log.info("医生评价创建成功: {}", savedReview.getId());
        
        return convertToDTO(savedReview);
    }
    
    @Override
    @Transactional
    public DoctorReviewDTO updateReview(Long reviewId, DoctorReviewDTO reviewDTO) {
        log.info("更新医生评价: {}", reviewId);
        
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        
        // 只能更新自己的评价且状态为待审核
        if (!review.getPatient().getId().equals(reviewDTO.getPatientId())) {
            throw new RuntimeException("只能更新自己的评价");
        }
        
        if (!"PENDING".equals(review.getStatus())) {
            throw new RuntimeException("只能更新待审核的评价");
        }
        
        review.setRating(reviewDTO.getRating());
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        review.setServiceAttitude(reviewDTO.getServiceAttitude());
        review.setProfessionalSkill(reviewDTO.getProfessionalSkill());
        review.setCommunicationAbility(reviewDTO.getCommunicationAbility());
        review.setTreatmentEffect(reviewDTO.getTreatmentEffect());
        review.setIsAnonymous(reviewDTO.getIsAnonymous());
        
        DoctorReview updatedReview = doctorReviewRepository.save(review);
        log.info("医生评价更新成功: {}", reviewId);
        
        return convertToDTO(updatedReview);
    }
    
    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        log.info("删除医生评价: {}", reviewId);
        
        if (!doctorReviewRepository.existsByIdCustom(reviewId)) {
            throw new RuntimeException("评价不存在");
        }
        
        doctorReviewRepository.deleteById(reviewId);
        log.info("医生评价删除成功: {}", reviewId);
    }
    
    @Override
    public DoctorReviewDTO getReviewById(Long reviewId) {
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        
        return convertToDTO(review);
    }
    
    @Override
    public Page<DoctorReviewDTO> getDoctorReviews(Long doctorId, String status, Pageable pageable) {
        Page<DoctorReview> reviews;
        
        if (status != null && !status.isEmpty()) {
            reviews = doctorReviewRepository.findDoctorReviewsByStatusOrdered(doctorId, status, pageable);
        } else {
            reviews = doctorReviewRepository.findDoctorReviewsByStatusOrdered(doctorId, "APPROVED", pageable);
        }
        
        return reviews.map(this::convertToDTO);
    }
    
    @Override
    public Page<DoctorReviewDTO> getPatientReviews(Long patientId, Pageable pageable) {
        Page<DoctorReview> reviews = doctorReviewRepository.findPatientReviewsOrdered(patientId, pageable);
        return reviews.map(this::convertToDTO);
    }
    
    @Override
    public DoctorReviewStatsDTO getDoctorReviewStats(Long doctorId) {
        log.info("获取医生评价统计: {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        
        DoctorReviewStatsDTO stats = new DoctorReviewStatsDTO();
        stats.setDoctorId(doctorId);
        stats.setDoctorName(doctor.getName());
        stats.setDoctorTitle(doctor.getTitle());
        stats.setDepartmentName(doctor.getDepartment().getName());
        
        // 获取平均评分
        Double averageRating = doctorReviewRepository.getAverageRatingByDoctorId(doctorId);
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        
        // 获取评价数量统计
        long totalReviews = doctorReviewRepository.countByDoctorIdAndStatus(doctorId, "APPROVED");
        stats.setTotalReviews(totalReviews);
        stats.setApprovedReviews(totalReviews);
        
        // 获取评分分布
        List<Object[]> ratingDistribution = doctorReviewRepository.getRatingDistributionByDoctorId(doctorId);
        Map<Integer, Long> distributionMap = new HashMap<>();
        for (Object[] row : ratingDistribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distributionMap.put(rating, count);
        }
        stats.setRatingDistribution(distributionMap);
        
        log.info("医生评价统计获取成功: {}", doctorId);
        return stats;
    }
    
    @Override
    @Transactional
    public DoctorReviewDTO approveReview(Long reviewId, String adminReply) {
        log.info("审核通过医生评价: {}", reviewId);
        
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        
        review.setStatus("APPROVED");
        review.setAdminReply(adminReply);
        review.setAdminReplyTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        
        DoctorReview approvedReview = doctorReviewRepository.save(review);
        log.info("医生评价审核通过: {}", reviewId);
        
        return convertToDTO(approvedReview);
    }
    
    @Override
    @Transactional
    public DoctorReviewDTO rejectReview(Long reviewId, String reason) {
        log.info("拒绝医生评价: {}", reviewId);
        
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        
        review.setStatus("REJECTED");
        review.setAdminReply(reason);
        review.setAdminReplyTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        
        DoctorReview rejectedReview = doctorReviewRepository.save(review);
        log.info("医生评价已拒绝: {}", reviewId);
        
        return convertToDTO(rejectedReview);
    }
    
    @Override
    @Transactional
    public void markReviewHelpful(Long reviewId, Long userId, boolean helpful) {
        log.info("标记评价是否有帮助: reviewId={}, userId={}, helpful={}", reviewId, userId, helpful);
        
        DoctorReview review = doctorReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        
        if (helpful) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        } else {
            review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        }
        
        doctorReviewRepository.save(review);
        log.info("评价帮助标记成功: {}", reviewId);
    }
    
    @Override
    public boolean canReviewAppointment(Long patientId, Long appointmentId) {
        // 检查预约是否存在且属于该患者
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> appointment.getPatient().getId().equals(patientId) && 
                                   "COMPLETED".equals(appointment.getStatus()))
                .orElse(false);
    }
    
    @Override
    public Page<DoctorReviewDTO> getPendingReviews(Pageable pageable) {
        Page<DoctorReview> reviews = doctorReviewRepository.findReviewsByStatusOrdered("PENDING", pageable);
        return reviews.map(this::convertToDTO);
    }
    
    @Override
    public List<DoctorReviewDTO> getDoctorLatestReviews(Long doctorId, int limit) {
        List<DoctorReview> reviews = doctorReviewRepository.findLatestReviewsByDoctorId(doctorId, Pageable.ofSize(limit));
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public Page<DoctorReviewDTO> searchReviews(String keyword, String status, Pageable pageable) {
        // 这里可以实现基于关键词的搜索逻辑
        // 暂时返回所有评价，后续可以添加全文搜索
        if (status != null && !status.isEmpty()) {
            return doctorReviewRepository.findReviewsByStatusOrdered(status, pageable).map(this::convertToDTO);
        } else {
            return doctorReviewRepository.findAll(pageable).map(this::convertToDTO);
        }
    }
    
    private DoctorReviewDTO convertToDTO(DoctorReview review) {
        DoctorReviewDTO dto = new DoctorReviewDTO();
        dto.setId(review.getId());
        dto.setDoctorId(review.getDoctor().getId());
        dto.setDoctorName(review.getDoctor().getName());
        dto.setDoctorTitle(review.getDoctor().getTitle());
        dto.setDepartmentName(review.getDoctor().getDepartment().getName());
        dto.setPatientId(review.getPatient().getId());
        dto.setPatientName(review.getIsAnonymous() ? "匿名用户" : review.getPatient().getName());
        dto.setAppointmentId(review.getAppointment().getId());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setServiceAttitude(review.getServiceAttitude());
        dto.setProfessionalSkill(review.getProfessionalSkill());
        dto.setCommunicationAbility(review.getCommunicationAbility());
        dto.setTreatmentEffect(review.getTreatmentEffect());
        dto.setIsAnonymous(review.getIsAnonymous());
        dto.setStatus(review.getStatus());
        dto.setAdminReply(review.getAdminReply());
        dto.setAdminReplyTime(review.getAdminReplyTime());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setUnhelpfulCount(review.getUnhelpfulCount());
        dto.setCreateTime(review.getCreateTime());
        dto.setUpdateTime(review.getUpdateTime() != null ? review.getUpdateTime() : review.getCreateTime());
        
        return dto;
    }
}