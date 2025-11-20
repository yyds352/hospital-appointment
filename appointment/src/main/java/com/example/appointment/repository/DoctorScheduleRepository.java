package com.example.appointment.repository;

import com.example.appointment.entity.DoctorSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    @Query(value = "SELECT COUNT(*) FROM doctor_schedule ds WHERE ds.doctor_id = :doctorId AND ds.schedule_date = :scheduleDate", nativeQuery = true)
    long countByDoctorIdAndScheduleDate(@Param("doctorId") Long doctorId, @Param("scheduleDate") LocalDate scheduleDate);
    
    default boolean existsByDoctorIdAndScheduleDate(Long doctorId, LocalDate scheduleDate) {
        return countByDoctorIdAndScheduleDate(doctorId, scheduleDate) > 0;
    }
    
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.doctor_id = :doctorId AND ds.schedule_date = :scheduleDate", nativeQuery = true)
    List<DoctorSchedule> findByDoctorIdAndScheduleDate(@Param("doctorId") Long doctorId, @Param("scheduleDate") LocalDate scheduleDate);
    
    @Query(value = "SELECT ds.* FROM doctor_schedule ds " +
           "JOIN doctor d ON ds.doctor_id = d.id " +
           "WHERE d.department_id = :departmentId AND ds.schedule_date = :scheduleDate", nativeQuery = true)
    List<DoctorSchedule> findByDepartmentIdAndScheduleDate(@Param("departmentId") Long departmentId, @Param("scheduleDate") LocalDate scheduleDate);
    
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.doctor_id = :doctorId AND ds.schedule_date >= :scheduleDate", nativeQuery = true)
    List<DoctorSchedule> findDoctorSchedulesFromDate(@Param("doctorId") Long doctorId, @Param("scheduleDate") LocalDate scheduleDate);
    
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.department_id = :departmentId AND ds.schedule_date >= :scheduleDate ORDER BY ds.schedule_date", nativeQuery = true)
    List<DoctorSchedule> findDepartmentSchedulesFromDate(@Param("departmentId") Long departmentId, @Param("scheduleDate") LocalDate scheduleDate);
    
    @Modifying
    @Query(value = "UPDATE doctor_schedule SET available_appointments = available_appointments + :change WHERE id = :scheduleId", nativeQuery = true)
    void updateAvailableAppointments(@Param("scheduleId") Long scheduleId, @Param("change") int change);
    
    /**
     * 获取今天及以后的排班，按日期升序排列
     */
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.schedule_date >= :scheduleDate ORDER BY ds.schedule_date ASC", nativeQuery = true)
    List<DoctorSchedule> findSchedulesFromDateOrdered(@Param("scheduleDate") LocalDate scheduleDate);
    
    /**
     * 获取指定日期之前的排班，按日期降序排列
     */
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.schedule_date < :scheduleDate ORDER BY ds.schedule_date DESC", nativeQuery = true)
    List<DoctorSchedule> findSchedulesBeforeDateOrdered(@Param("scheduleDate") LocalDate scheduleDate);
    
    /**
     * 获取指定日期之前的排班，按日期降序排列，限制返回数量
     * 使用Pageable参数代替limit参数
     */
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.schedule_date < :scheduleDate ORDER BY ds.schedule_date DESC", nativeQuery = true)
    List<DoctorSchedule> findSchedulesBeforeDateOrdered(@Param("scheduleDate") LocalDate scheduleDate, Pageable pageable);
    
    /**
     * 使用原生SQL查询
     */
    @Query(value = "SELECT * FROM doctor_schedule WHERE doctor_id = :doctorId AND schedule_date = :scheduleDate", nativeQuery = true)
    List<DoctorSchedule> findSchedulesByNativeQuery(@Param("doctorId") Long doctorId, @Param("scheduleDate") String scheduleDate);

    /**
     * 根据医生ID和日期范围查询排班记录，按日期和时段升序排序
     * @param doctorId 医生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班记录列表
     */
    @Query(value = "SELECT ds.* FROM doctor_schedule ds WHERE ds.doctor_id = :doctorId AND ds.schedule_date BETWEEN :startDate AND :endDate ORDER BY ds.schedule_date ASC, ds.period ASC", nativeQuery = true)
    List<DoctorSchedule> findDoctorSchedulesBetweenDatesOrdered(
        @Param("doctorId") Long doctorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
        
    /**
     * 检查指定医生在指定日期的指定时段是否可用
     * @param doctorId 医生ID
     * @param dayOfWeek 星期几
     * @param timeSlot 时段
     * @param status 状态：1-正常, 0-停诊
     * @return 是否存在符合条件的排班
     */
    @Query(value = "SELECT COUNT(*) FROM doctor_schedule ds WHERE ds.doctor_id = :doctorId AND ds.schedule_date = :dayOfWeek AND ds.period = :timeSlot AND ds.status = :status", nativeQuery = true)
    long countDoctorScheduleAvailability(
        @Param("doctorId") Long doctorId, @Param("dayOfWeek") LocalDate dayOfWeek, @Param("timeSlot") String timeSlot, @Param("status") Integer status);
    
    default boolean checkDoctorScheduleAvailability(Long doctorId, LocalDate dayOfWeek, String timeSlot, Integer status) {
        return countDoctorScheduleAvailability(doctorId, dayOfWeek, timeSlot, status) > 0;
    }
    
    /**
     * 检查医生在指定日期的指定时段是否已存在排班
     * @param doctorId 医生ID
     * @param scheduleDate 排班日期
     * @param period 时间段（上午/下午/晚上）
     * @return 是否已存在排班
     */
    boolean existsByDoctorIdAndScheduleDateAndPeriod(Long doctorId, LocalDate scheduleDate, String period);
    
    @Query(value = "SELECT COUNT(*) FROM doctor_schedule ds WHERE ds.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}