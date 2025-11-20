package com.example.appointment.repository;

import com.example.appointment.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query(value = "SELECT p.* FROM posts p WHERE p.user_id = :authorId ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findPostsByAuthorOrdered(@Param("authorId") Long authorId, Pageable pageable);
    
    @Query(value = "SELECT p.* FROM posts p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%", nativeQuery = true)
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);
    
    @Modifying
    @Query(value = "UPDATE posts SET likes = likes + 1 WHERE id = :id", nativeQuery = true)
    void incrementLikes(@Param("id") Long id);
    
    @Modifying
    @Query(value = "UPDATE posts SET views = views + 1 WHERE id = :id", nativeQuery = true)
    void incrementViews(@Param("id") Long id);
    

    
    // 统计帖子总数
    @Query(value = "SELECT COUNT(*) FROM posts", nativeQuery = true)
    Long countTotalPosts();
    
    // 统计今日发布的帖子数
    @Query(value = "SELECT COUNT(*) FROM posts WHERE created_at >= :today", nativeQuery = true)
    Long countTodayPosts(@Param("today") LocalDateTime today);
    
    // 获取帖子按分类查询
    @Query(value = "SELECT p.* FROM posts p WHERE p.category_id = :categoryId ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findPostsByCategoryOrdered(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 根据标签查询帖子
     * @param tags 标签（支持模糊匹配）
     * @param pageable 分页参数
     * @return 帖子分页列表
     */
    @Query(value = "SELECT p.* FROM posts p WHERE p.tags LIKE :tags ORDER BY p.created_at DESC", nativeQuery = true)
    Page<Post> findPostsByTags(@Param("tags") String tags, Pageable pageable);

    /**
     * 根据作者ID查询帖子
     * @param authorId 作者ID
     * @param pageable 分页参数
     * @return 帖子分页列表
     */
    @Query(value = "SELECT p.* FROM posts p WHERE p.user_id = :authorId", nativeQuery = true)
    Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    /**
     * 自定义existsById方法
     */
    @Query(value = "SELECT COUNT(*) FROM posts p WHERE p.id = :id", nativeQuery = true)
    long countByIdCustom(@Param("id") Long id);
    
    default boolean existsByIdCustom(Long id) {
        return countByIdCustom(id) > 0;
    }

}