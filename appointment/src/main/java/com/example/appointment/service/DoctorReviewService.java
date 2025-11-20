package com.example.appointment.service;

import com.example.appointment.dto.DoctorReviewDTO;
import com.example.appointment.dto.DoctorReviewStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 医生评价服务接口
 */
public interface DoctorReviewService {
    
    /**
     * 创建医生评价
     */
    DoctorReviewDTO createReview(DoctorReviewDTO reviewDTO);
    
    /**
     * 更新医生评价
     */
    DoctorReviewDTO updateReview(Long reviewId, DoctorReviewDTO reviewDTO);
    
    /**
     * 删除医生评价
     */
    void deleteReview(Long reviewId);
    
    /**
     * 根据ID获取评价详情
     */
    DoctorReviewDTO getReviewById(Long reviewId);
    
    /**
     * 获取医生的评价列表
     */
    Page<DoctorReviewDTO> getDoctorReviews(Long doctorId, String status, Pageable pageable);
    
    /**
     * 获取患者的评价列表
     */
    Page<DoctorReviewDTO> getPatientReviews(Long patientId, Pageable pageable);
    
    /**
     * 获取医生的评价统计信息
     */
    DoctorReviewStatsDTO getDoctorReviewStats(Long doctorId);
    
    /**
     * 审核评价
     */
    DoctorReviewDTO approveReview(Long reviewId, String adminReply);
    
    /**
     * 拒绝评价
     */
    DoctorReviewDTO rejectReview(Long reviewId, String reason);
    
    /**
     * 评价是否有帮助
     */
    void markReviewHelpful(Long reviewId, Long userId, boolean helpful);
    
    /**
     * 检查患者是否可以对预约进行评价
     */
    boolean canReviewAppointment(Long patientId, Long appointmentId);
    
    /**
     * 获取待审核的评价列表
     */
    Page<DoctorReviewDTO> getPendingReviews(Pageable pageable);
    
    /**
     * 获取医生的最新评价
     */
    List<DoctorReviewDTO> getDoctorLatestReviews(Long doctorId, int limit);
    
    /**
     * 搜索评价
     */
    Page<DoctorReviewDTO> searchReviews(String keyword, String status, Pageable pageable);
}