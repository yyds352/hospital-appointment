package com.example.appointment.repository;

import com.example.appointment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT u.* FROM user u WHERE u.username = :username", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);
    @Query(value = "SELECT COUNT(*) FROM user u WHERE u.username = :username", nativeQuery = true)
    long countByUsername(@Param("username") String username);
    
    default boolean existsByUsername(String username) {
        return countByUsername(username) > 0;
    }

    @Query(value = "SELECT COUNT(*) FROM user u WHERE u.email = :email", nativeQuery = true)
    long countByEmail(@Param("email") String email);
    
    default boolean existsByEmail(String email) {
        return countByEmail(email) > 0;
    }
    
    @Modifying
    @Query(value = "UPDATE user SET name = ?2 WHERE id = ?1", nativeQuery = true)
    int updateName(Long id, String name);
    
    @Query(value = "SELECT u.* FROM user u WHERE u.id = :id", nativeQuery = true)
    Optional<User> findByIdWithDetails(@Param("id") Long id);
    
    @Query(value = "SELECT COUNT(*) FROM user u WHERE u.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}