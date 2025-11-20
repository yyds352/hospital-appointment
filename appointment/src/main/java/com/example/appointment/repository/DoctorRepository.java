package com.example.appointment.repository;

import com.example.appointment.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN department dept ON d.department_id = dept.id " +
           "WHERE d.name LIKE CONCAT('%', :name, '%') AND dept.id = :departmentId AND d.status = :status", nativeQuery = true)
    Page<Doctor> findDoctorsByNameAndDepartmentAndStatus(
        @Param("name") String name,
        @Param("departmentId") Long departmentId,
        @Param("status") Integer status,
        Pageable pageable
    );
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.name LIKE CONCAT('%', :name, '%') AND d.status = :status", nativeQuery = true)
    Page<Doctor> findDoctorsByNameAndStatus(
        @Param("name") String name,
        @Param("status") Integer status,
        Pageable pageable
    );
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.department_id = :departmentId AND d.status = :status", nativeQuery = true)
    Page<Doctor> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") Integer status, Pageable pageable);
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.department_id = :departmentId", nativeQuery = true)
    List<Doctor> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query(value = "SELECT COUNT(*) FROM doctors d WHERE d.name = :name AND d.department_id = :departmentId", nativeQuery = true)
    long countByNameAndDepartmentId(@Param("name") String name, @Param("departmentId") Long departmentId);
    
    default boolean existsByNameAndDepartmentId(String name, Long departmentId) {
        return countByNameAndDepartmentId(name, departmentId) > 0;
    }

    @Query(value = "SELECT d.* FROM doctors d WHERE d.user_id = :userId", nativeQuery = true)
    Optional<Doctor> findByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN department dept ON d.department_id = dept.id " +
           "WHERE dept.code = :departmentCode", nativeQuery = true)
    List<Doctor> findByDepartmentCode(@Param("departmentCode") String departmentCode);
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.name LIKE CONCAT('%', :name, '%')", nativeQuery = true)
    List<Doctor> findDoctorsByName(@Param("name") String name);
    
    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN department dept ON d.department_id = dept.id " +
           "WHERE dept.code = :departmentCode AND d.name LIKE CONCAT('%', :name, '%')", nativeQuery = true)
    List<Doctor> findDoctorsByDepartmentCodeAndName(@Param("departmentCode") String departmentCode, @Param("name") String name);
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.status = 1", nativeQuery = true)
    List<Doctor> findActiveDoctors();
    
    @Query(value = "SELECT d.* FROM doctors d " +
           "JOIN department dept ON d.department_id = dept.id " +
           "WHERE dept.code = :departmentCode AND d.status = 1", nativeQuery = true)
    List<Doctor> findActiveDoctorsByDepartment(@Param("departmentCode") String departmentCode);
    
    @Query(value = "SELECT COUNT(*) FROM doctor d WHERE d.status = 1", nativeQuery = true)
    long countActiveDoctors();
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.id IN :doctorIds", nativeQuery = true)
    List<Doctor> findByIdIn(@Param("doctorIds") List<Long> doctorIds);
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.username = :username", nativeQuery = true)
    Optional<Doctor> findByUsername(@Param("username") String username);
    
    @Query(value = "SELECT COUNT(*) FROM doctors d WHERE d.username = :username", nativeQuery = true)
    long countByUsername(@Param("username") String username);
    
    default boolean existsByUsername(String username) {
        return countByUsername(username) > 0;
    }

    @Query(value = "SELECT COUNT(*) FROM doctors d WHERE d.email = :email", nativeQuery = true)
    long countByEmail(@Param("email") String email);
    
    default boolean existsByEmail(String email) {
        return countByEmail(email) > 0;
    }

    @Query(value = "SELECT COUNT(*) FROM doctors d WHERE d.phone = :phone", nativeQuery = true)
    long countByPhone(@Param("phone") String phone);
    
    default boolean existsByPhone(String phone) {
        return countByPhone(phone) > 0;
    }
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.specialty LIKE CONCAT('%', :specialty, '%')", nativeQuery = true)
    List<Doctor> findDoctorsBySpecialty(@Param("specialty") String specialty);
    
    @Query(value = "SELECT d.* FROM doctors d WHERE d.department_id = :departmentId AND d.id != :doctorId", nativeQuery = true)
    List<Doctor> findByDepartmentIdAndIdNot(@Param("departmentId") Long departmentId, @Param("doctorId") Long doctorId);
    
    @Query(value = "SELECT COUNT(*) FROM doctors d WHERE d.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}