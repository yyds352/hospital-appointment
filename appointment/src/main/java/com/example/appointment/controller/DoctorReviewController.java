package com.example.appointment.controller;

import com.example.appointment.dto.DoctorReviewDTO;
import com.example.appointment.dto.DoctorReviewStatsDTO;
import com.example.appointment.service.DoctorReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-reviews")
@RequiredArgsConstructor
@Tag(name = "医生评价管理", description = "医生评价相关接口")
public class DoctorReviewController {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorReviewController.class);
    
    private final DoctorReviewService doctorReviewService;
    
    @PostMapping
    @Operation(summary = "创建医生评价", description = "患者对医生进行评价")
    public ResponseEntity<DoctorReviewDTO> createReview(@Valid @RequestBody DoctorReviewDTO reviewDTO) {
        log.info("创建医生评价请求: {}", reviewDTO);
        DoctorReviewDTO createdReview = doctorReviewService.createReview(reviewDTO);
        return ResponseEntity.ok(createdReview);
    }
    
    @PutMapping("/{reviewId}")
    @Operation(summary = "更新医生评价", description = "患者更新自己的评价")
    public ResponseEntity<DoctorReviewDTO> updateReview(
            @PathVariable @Parameter(description = "评价ID") Long reviewId,
            @Valid @RequestBody DoctorReviewDTO reviewDTO) {
        log.info("更新医生评价请求: reviewId={}", reviewId);
        DoctorReviewDTO updatedReview = doctorReviewService.updateReview(reviewId, reviewDTO);
        return ResponseEntity.ok(updatedReview);
    }
    
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "删除医生评价", description = "删除评价")
    public ResponseEntity<Void> deleteReview(@PathVariable @Parameter(description = "评价ID") Long reviewId) {
        log.info("删除医生评价请求: reviewId={}", reviewId);
        doctorReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{reviewId}")
    @Operation(summary = "获取评价详情", description = "根据ID获取评价详情")
    public ResponseEntity<DoctorReviewDTO> getReviewById(
            @PathVariable @Parameter(description = "评价ID") Long reviewId) {
        log.info("获取评价详情请求: reviewId={}", reviewId);
        DoctorReviewDTO review = doctorReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "获取医生评价列表", description = "获取指定医生的评价列表")
    public ResponseEntity<Page<DoctorReviewDTO>> getDoctorReviews(
            @PathVariable @Parameter(description = "医生ID") Long doctorId,
            @RequestParam(required = false) @Parameter(description = "评价状态") String status,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("获取医生评价列表请求: doctorId={}, status={}", doctorId, status);
        Page<DoctorReviewDTO> reviews = doctorReviewService.getDoctorReviews(doctorId, status, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "获取患者评价列表", description = "获取指定患者的评价列表")
    public ResponseEntity<Page<DoctorReviewDTO>> getPatientReviews(
            @PathVariable @Parameter(description = "患者ID") Long patientId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("获取患者评价列表请求: patientId={}", patientId);
        Page<DoctorReviewDTO> reviews = doctorReviewService.getPatientReviews(patientId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/doctor/{doctorId}/stats")
    @Operation(summary = "获取医生评价统计", description = "获取指定医生的评价统计数据")
    public ResponseEntity<DoctorReviewStatsDTO> getDoctorReviewStats(
            @PathVariable @Parameter(description = "医生ID") Long doctorId) {
        log.info("获取医生评价统计请求: doctorId={}", doctorId);
        DoctorReviewStatsDTO stats = doctorReviewService.getDoctorReviewStats(doctorId);
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核通过评价", description = "管理员审核通过评价")
    public ResponseEntity<DoctorReviewDTO> approveReview(
            @PathVariable @Parameter(description = "评价ID") Long reviewId,
            @RequestParam(required = false) @Parameter(description = "管理员回复") String adminReply) {
        log.info("审核通过评价请求: reviewId={}", reviewId);
        DoctorReviewDTO approvedReview = doctorReviewService.approveReview(reviewId, adminReply);
        return ResponseEntity.ok(approvedReview);
    }
    
    @PutMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "拒绝评价", description = "管理员拒绝评价")
    public ResponseEntity<DoctorReviewDTO> rejectReview(
            @PathVariable @Parameter(description = "评价ID") Long reviewId,
            @RequestParam(required = false) @Parameter(description = "拒绝原因") String reason) {
        log.info("拒绝评价请求: reviewId={}", reviewId);
        DoctorReviewDTO rejectedReview = doctorReviewService.rejectReview(reviewId, reason);
        return ResponseEntity.ok(rejectedReview);
    }
    
    @PostMapping("/{reviewId}/helpful")
    @Operation(summary = "标记评价是否有帮助", description = "标记评价是否有帮助")
    public ResponseEntity<Void> markReviewHelpful(
            @PathVariable @Parameter(description = "评价ID") Long reviewId,
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "是否有帮助") boolean helpful) {
        log.info("标记评价是否有帮助请求: reviewId={}, userId={}, helpful={}", reviewId, userId, helpful);
        doctorReviewService.markReviewHelpful(reviewId, userId, helpful);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审核评价", description = "获取所有待审核的评价")
    public ResponseEntity<Page<DoctorReviewDTO>> getPendingReviews(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("获取待审核评价请求");
        Page<DoctorReviewDTO> pendingReviews = doctorReviewService.getPendingReviews(pageable);
        return ResponseEntity.ok(pendingReviews);
    }
    
    @GetMapping("/doctor/{doctorId}/latest")
    @Operation(summary = "获取医生最新评价", description = "获取指定医生的最新评价")
    public ResponseEntity<List<DoctorReviewDTO>> getDoctorLatestReviews(
            @PathVariable @Parameter(description = "医生ID") Long doctorId,
            @RequestParam(defaultValue = "5") @Parameter(description = "数量限制") int limit) {
        log.info("获取医生最新评价请求: doctorId={}, limit={}", doctorId, limit);
        List<DoctorReviewDTO> latestReviews = doctorReviewService.getDoctorLatestReviews(doctorId, limit);
        return ResponseEntity.ok(latestReviews);
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索评价", description = "根据关键词搜索评价")
    public ResponseEntity<Page<DoctorReviewDTO>> searchReviews(
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword,
            @RequestParam(required = false) @Parameter(description = "评价状态") String status,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("搜索评价请求: keyword={}, status={}", keyword, status);
        Page<DoctorReviewDTO> searchResults = doctorReviewService.searchReviews(keyword, status, pageable);
        return ResponseEntity.ok(searchResults);
    }
    
    @GetMapping("/appointment/{appointmentId}/can-review")
    @Operation(summary = "检查是否可以评价", description = "检查患者是否可以对指定预约进行评价")
    public ResponseEntity<Boolean> canReviewAppointment(
            @PathVariable @Parameter(description = "预约ID") Long appointmentId,
            @RequestParam @Parameter(description = "患者ID") Long patientId) {
        log.info("检查是否可以评价请求: appointmentId={}, patientId={}", appointmentId, patientId);
        boolean canReview = doctorReviewService.canReviewAppointment(patientId, appointmentId);
        return ResponseEntity.ok(canReview);
    }
}