package com.example.appointment.repository;

import com.example.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // 查询指定医生在某个时间段的预约
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId " +
           "AND a.appointment_time BETWEEN :startTime AND :endTime " +
           "AND a.status NOT IN ('CANCELLED')", nativeQuery = true)
    List<Appointment> findByDoctorAndTimeRange(
        @Param("doctorId") Long doctorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // 查询指定患者在某个时间段的预约
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId " +
           "AND a.appointment_time BETWEEN :startTime AND :endTime " +
           "AND a.status NOT IN ('CANCELLED')", nativeQuery = true)
    List<Appointment> findByPatientAndTimeRange(
        @Param("patientId") Long patientId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // 查询患者的预约记录 - 使用原生SQL避免JPA解析问题
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId ORDER BY a.appointment_time DESC", nativeQuery = true)
    Page<Appointment> findPatientAppointmentsOrdered(@Param("patientId") Long patientId, Pageable pageable);

    // 查询医生的预约记录 - 使用原生SQL避免JPA解析问题
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId ORDER BY a.appointment_time DESC", nativeQuery = true)
    Page<Appointment> findDoctorAppointmentsOrdered(@Param("doctorId") Long doctorId, Pageable pageable);

    // 查询科室的预约记录 - 使用原生SQL避免JPA解析问题
    @Query(value = "SELECT a.* FROM appointments a " +
           "JOIN doctors d ON a.doctor_id = d.id " +
           "WHERE d.department_id = :departmentId " +
           "ORDER BY a.appointment_time DESC", nativeQuery = true)
    Page<Appointment> findDepartmentAppointmentsOrdered(@Param("departmentId") Long departmentId, Pageable pageable);

    // 查询当天的预约数量
    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE DATE(a.appointment_time) = CURRENT_DATE " +
           "AND a.status NOT IN ('CANCELLED')", nativeQuery = true)
    Long countTodayAppointments();

    // 根据患者ID和日期筛选预约（可选状态筛选） - 使用原生SQL避免JPA解析问题
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId " +
           "AND (:appointmentDate IS NULL OR DATE(a.appointment_time) = :appointmentDate) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.appointment_time DESC", 
           countQuery = "SELECT COUNT(*) FROM appointments a WHERE a.patient_id = :patientId " +
           "AND (:appointmentDate IS NULL OR DATE(a.appointment_time) = :appointmentDate) " +
           "AND (:status IS NULL OR a.status = :status)",
           nativeQuery = true)
    Page<Appointment> findAppointmentsByPatientAndDateAndStatus(
        @Param("patientId") Long patientId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("status") String status,
        Pageable pageable
    );

    // 根据医生ID和日期筛选预约（可选状态筛选） - 使用原生SQL避免JPA解析问题
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId " +
           "AND (:appointmentDate IS NULL OR DATE(a.appointment_time) = :appointmentDate) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.appointment_time DESC", 
           countQuery = "SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId " +
           "AND (:appointmentDate IS NULL OR DATE(a.appointment_time) = :appointmentDate) " +
           "AND (:status IS NULL OR a.status = :status)",
           nativeQuery = true)
    Page<Appointment> findAppointmentsByDoctorAndDateAndStatus(
        @Param("doctorId") Long doctorId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("status") String status,
        Pageable pageable
    );
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId", nativeQuery = true)
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId", nativeQuery = true)
    List<Appointment> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId AND a.appointment_time BETWEEN :startTime AND :endTime", nativeQuery = true)
    List<Appointment> findDoctorAppointmentsBetween(@Param("doctorId") Long doctorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.appointment_time BETWEEN :startTime AND :endTime", nativeQuery = true)
    List<Appointment> findPatientAppointmentsBetween(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.appointment_number = :appointmentNumber", nativeQuery = true)
    Optional<Appointment> findByAppointmentNumber(@Param("appointmentNumber") String appointmentNumber);
    
    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.appointment_number = :appointmentNumber", nativeQuery = true)
    long countByAppointmentNumber(@Param("appointmentNumber") String appointmentNumber);
    
    default boolean existsByAppointmentNumber(String appointmentNumber) {
        return countByAppointmentNumber(appointmentNumber) > 0;
    }
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE doctor_id = :doctorId AND status = :status", nativeQuery = true)
    long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") String status);
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE patient_id = :patientId AND status = :status", nativeQuery = true)
    long countByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") String status);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId AND a.appointment_time >= :startTime AND a.appointment_time < :endTime", nativeQuery = true)
    List<Appointment> findByDoctorIdAndTimeRange(@Param("doctorId") Long doctorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.appointment_time >= :startTime AND a.appointment_time < :endTime", nativeQuery = true)
    List<Appointment> findByPatientIdAndTimeRange(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.status IN :statuses", nativeQuery = true)
    List<Appointment> findByPatientIdAndStatusIn(@Param("patientId") Long patientId, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.appointment_time BETWEEN :startTime AND :endTime AND a.status IN :statuses", nativeQuery = true)
    List<Appointment> findPatientAppointmentsInTimeRangeWithStatus(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.status = :status AND a.appointment_time >= :currentTime", nativeQuery = true)
    List<Appointment> findUpcomingAppointmentsByStatus(@Param("status") String status, @Param("currentTime") LocalDateTime currentTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.status = 'SCHEDULED' AND a.appointment_time BETWEEN :startTime AND :endTime", nativeQuery = true)
    List<Appointment> findScheduledAppointmentsInTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.appointment_time >= :currentTime ORDER BY a.appointment_time ASC", nativeQuery = true)
    List<Appointment> findFutureAppointments(@Param("currentTime") LocalDateTime currentTime);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.doctor_id = :doctorId", nativeQuery = true)
    List<Appointment> findByPatientIdAndDoctorId(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE patient_id = :patientId AND doctor_id = :doctorId", nativeQuery = true)
    long countByPatientIdAndDoctorId(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);
    
    @Query(value = "SELECT a.* FROM appointments a " +
           "JOIN doctors d ON a.doctor_id = d.id " +
           "JOIN department dept ON d.department_id = dept.id " +
           "WHERE dept.code = :departmentCode AND a.patient_id != :patientId", nativeQuery = true)
    List<Appointment> findByDoctorDepartmentCodeAndPatientIdNot(@Param("departmentCode") String departmentCode, @Param("patientId") Long patientId);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId AND a.status = 'COMPLETED' ORDER BY a.appointment_time DESC", nativeQuery = true)
    List<Appointment> findCompletedAppointmentsByDoctor(@Param("doctorId") Long doctorId);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.status = 'COMPLETED' ORDER BY a.appointment_time DESC", nativeQuery = true)
    List<Appointment> findCompletedAppointmentsByPatient(@Param("patientId") Long patientId);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId AND a.appointment_time >= :startDate AND a.appointment_time < :endDate", nativeQuery = true)
    List<Appointment> findByDoctorAndDateRange(@Param("doctorId") Long doctorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE doctor_id = :doctorId AND status = 'COMPLETED'", nativeQuery = true)
    long countCompletedAppointmentsByDoctor(@Param("doctorId") Long doctorId);
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE patient_id = :patientId AND status = 'COMPLETED'", nativeQuery = true)
    long countCompletedAppointmentsByPatient(@Param("patientId") Long patientId);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.appointment_time BETWEEN :startTime AND :endTime AND a.status IN :statuses AND a.reminded = false", nativeQuery = true)
    List<Appointment> findAppointmentsInTimeRangeWithStatusAndNotReminded(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND a.appointment_time > :currentTime AND a.status = :status", nativeQuery = true)
    List<Appointment> findPatientFutureAppointmentsByStatus(@Param("patientId") Long patientId, @Param("currentTime") LocalDateTime currentTime, @Param("status") String status);
    
    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId AND a.appointment_time BETWEEN :startTime AND :endTime AND a.status IN :statuses", nativeQuery = true)
    long countDoctorAppointmentsInTimeRangeWithStatus(@Param("doctorId") Long doctorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE patient_id = :patientId AND appointment_time BETWEEN :startTime AND :endTime AND status IN :statuses", nativeQuery = true)
    long countPatientAppointmentsInTimeRangeWithStatus(@Param("patientId") Long patientId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT COUNT(*) FROM appointments WHERE doctor_id = :doctorId AND appointment_time >= :currentTime", nativeQuery = true)
    long countUpcomingAppointmentsByDoctor(@Param("doctorId") Long doctorId, @Param("currentTime") LocalDateTime currentTime);
    
    // 使用更简单的命名避免JPA解析问题 - 使用原生SQL
    @Query(value = "SELECT a.* FROM appointments a WHERE a.patient_id = :patientId AND DATE(a.appointment_time) = :appointmentDate AND a.status IN :statuses", nativeQuery = true)
    List<Appointment> findPatientAppointmentsByDateAndStatus(@Param("patientId") Long patientId, @Param("appointmentDate") LocalDate appointmentDate, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT a.* FROM appointments a WHERE a.doctor_id = :doctorId AND DATE(a.appointment_time) = :appointmentDate AND a.status IN :statuses", nativeQuery = true)
    List<Appointment> findDoctorAppointmentsByDateAndStatus(@Param("doctorId") Long doctorId, @Param("appointmentDate") LocalDate appointmentDate, @Param("statuses") List<String> statuses);
    
    @Query(value = "SELECT COUNT(*) FROM appointments a WHERE a.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}