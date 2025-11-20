package com.example.appointment.repository;

import com.example.appointment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "SELECT c.* FROM post_comments c WHERE c.post_id = :postId ORDER BY c.created_at DESC", nativeQuery = true)
    Page<Comment> findCommentsByPostOrdered(@Param("postId") Long postId, Pageable pageable);
    
    @Query(value = "SELECT c.* FROM post_comments c WHERE c.post_id = :postId", nativeQuery = true)
    List<Comment> findCommentsByPostIdDirect(@Param("postId") Long postId);
    
    @Modifying
    @Query(value = "UPDATE post_comments SET likes = likes + 1 WHERE id = :commentId", nativeQuery = true)
    void incrementLikes(@Param("commentId") Long commentId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM post_comments WHERE post_id = :postId", nativeQuery = true)
    void deleteByPostId(@Param("postId") Long postId);
    
    List<Comment> findTopByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query(value = "SELECT COUNT(*) FROM post_comments", nativeQuery = true)
    Long countTotalComments();
    
    @Query(value = "SELECT COUNT(*) FROM post_comments WHERE created_at >= :today", nativeQuery = true)
    Long countTodayComments(@Param("today") LocalDateTime today);

    @Query(value = "SELECT COUNT(*) FROM post_comments WHERE post_id = :postId", nativeQuery = true)
    int countByPostId(@Param("postId") Long postId);
    
    @Query(value = "SELECT COUNT(*) FROM post_comments c WHERE c.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }
}