package com.example.appointment.repository;

import com.example.appointment.entity.DoctorReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 医生评价Repository
 */
@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {
    
    /**
     * 根据医生ID查询评价
     */
    @Query(value = "SELECT dr.* FROM doctor_reviews dr WHERE dr.doctor_id = :doctorId AND dr.status = :status ORDER BY dr.create_time DESC", nativeQuery = true)
    Page<DoctorReview> findDoctorReviewsByStatusOrdered(@Param("doctorId") Long doctorId, @Param("status") String status, Pageable pageable);
    
    /**
     * 根据患者ID查询评价
     */
    @Query(value = "SELECT dr.* FROM doctor_reviews dr WHERE dr.patient_id = :patientId ORDER BY dr.create_time DESC", nativeQuery = true)
    Page<DoctorReview> findPatientReviewsOrdered(@Param("patientId") Long patientId, Pageable pageable);
    
    /**
     * 根据预约ID查询评价
     */
    Optional<DoctorReview> findByAppointmentId(Long appointmentId);
    
    /**
     * 根据医生ID和状态统计评价数量
     */
    @Query(value = "SELECT COUNT(*) FROM doctor_reviews WHERE doctor_id = :doctorId AND status = :status", nativeQuery = true)
    long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);
    
    /**
     * 根据医生ID统计平均评分
     */
    @Query(value = "SELECT AVG(rating) FROM doctor_reviews WHERE doctor_id = :doctorId AND status = 'APPROVED'", nativeQuery = true)
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
    
    /**
     * 根据医生ID统计各评分数量
     */
    @Query(value = "SELECT dr.rating, COUNT(dr.id) FROM doctor_reviews dr " +
           "WHERE dr.doctor_id = :doctorId AND dr.status = 'APPROVED' " +
           "GROUP BY dr.rating", nativeQuery = true)
    List<Object[]> getRatingDistributionByDoctorId(@Param("doctorId") Long doctorId);
    
    /**
     * 查询指定时间范围内的评价
     */
    @Query(value = "SELECT dr.* FROM doctor_reviews dr WHERE dr.create_time BETWEEN :startTime AND :endTime AND dr.status = :status", nativeQuery = true)
    List<DoctorReview> findReviewsByTimeRangeAndStatus(
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            @Param("status") String status);
    
    /**
     * 检查患者是否已对某个预约进行评价
     */
    @Query(value = "SELECT COUNT(*) FROM doctor_reviews WHERE appointment_id = :appointmentId", nativeQuery = true)
    long countByAppointmentId(@Param("appointmentId") Long appointmentId);
    
    default boolean existsByAppointmentId(Long appointmentId) {
        return countByAppointmentId(appointmentId) > 0;
    }
    
    /**
     * 查询待审核的评价
     */
    @Query(value = "SELECT dr.* FROM doctor_reviews dr WHERE dr.status = :status ORDER BY dr.create_time DESC", nativeQuery = true)
    Page<DoctorReview> findReviewsByStatusOrdered(@Param("status") String status, Pageable pageable);
    
    /**
     * 根据医生ID查询最新评价
     */
    @Query(value = "SELECT dr.* FROM doctor_reviews dr WHERE dr.doctor_id = :doctorId AND dr.status = 'APPROVED' ORDER BY dr.create_time DESC", nativeQuery = true)
    List<DoctorReview> findLatestReviewsByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);
    
    /**
     * 根据科室代码获取医生平均评分
     */
    @Query(value = "SELECT dr.doctor_id, AVG(dr.rating), COUNT(dr.id) FROM doctor_reviews dr " +
           "JOIN doctor d ON dr.doctor_id = d.id " +
           "JOIN department dept ON d.department_id = dept.id " +
           "WHERE dept.code = :departmentCode AND dr.status = 'APPROVED' " +
           "GROUP BY dr.doctor_id", nativeQuery = true)
    List<Object[]> getAverageRatingByDepartment(@Param("departmentCode") String departmentCode);
    
    /**
     * 自定义existsById方法
     */
    @Query(value = "SELECT COUNT(*) FROM doctor_reviews dr WHERE dr.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}