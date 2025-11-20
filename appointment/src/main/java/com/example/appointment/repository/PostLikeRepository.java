package com.example.appointment.repository;

import com.example.appointment.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    @Query(value = "SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = :postId AND pl.user_id = :userId", nativeQuery = true)
    long countByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    default boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return countByPostIdAndUserId(postId, userId) > 0;
    }
    
    void deleteByPostIdAndUserId(Long postId, Long userId);
    
    @Modifying
    @Query(value = "DELETE FROM post_likes WHERE post_id = :postId", nativeQuery = true)
    void deleteByPostId(@Param("postId") Long postId);
    
    @Modifying
    @Query(value = "UPDATE posts SET likes = likes - 1 WHERE id = :postId", nativeQuery = true)
    void decrementLikes(@Param("postId") Long postId);
    
    @Query(value = "SELECT COUNT(*) FROM post_likes WHERE post_id = :postId", nativeQuery = true)
    long countByPostId(@Param("postId") Long postId);
    
    @Query(value = "SELECT COUNT(*) FROM post_likes pl WHERE pl.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}