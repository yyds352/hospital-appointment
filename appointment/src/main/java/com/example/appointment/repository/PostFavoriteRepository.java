package com.example.appointment.repository;

import com.example.appointment.entity.PostFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {
    @Query(value = "SELECT pf.* FROM post_favorites pf WHERE pf.user_id = :userId", nativeQuery = true)
    Page<PostFavorite> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query(value = "SELECT pf.* FROM post_favorites pf WHERE pf.post_id = :postId AND pf.user_id = :userId", nativeQuery = true)
    Optional<PostFavorite> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Query(value = "SELECT COUNT(*) FROM post_favorites WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
    long countByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    default boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return countByPostIdAndUserId(postId, userId) > 0;
    }
    
    @Modifying
    @Query(value = "DELETE FROM post_favorites WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
    void deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM post_favorites WHERE post_id = :postId", nativeQuery = true)
    void deleteByPostId(@Param("postId") Long postId);
    
    @Query(value = "SELECT COUNT(*) FROM post_favorites WHERE post_id = :postId", nativeQuery = true)
    long countByPostId(@Param("postId") Long postId);
    
    @Query(value = "SELECT COUNT(*) FROM post_favorites pf WHERE pf.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}