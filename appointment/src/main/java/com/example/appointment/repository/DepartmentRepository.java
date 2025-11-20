package com.example.appointment.repository;

import com.example.appointment.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query(value = "SELECT COUNT(*) FROM department d WHERE d.name = :name", nativeQuery = true)
    long countByName(@Param("name") String name);
    
    default boolean existsByName(String name) {
        return countByName(name) > 0;
    }
    @Query(value = "SELECT d.* FROM department d WHERE d.name LIKE CONCAT('%', :name, '%')", nativeQuery = true)
    Page<Department> findDepartmentsByName(@Param("name") String name, Pageable pageable);
    @Query(value = "SELECT d.* FROM department d WHERE d.status = :status", nativeQuery = true)
    Page<Department> findByStatus(@Param("status") Integer status, Pageable pageable);
    
    @Query(value = "SELECT COUNT(*) FROM department d WHERE d.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}