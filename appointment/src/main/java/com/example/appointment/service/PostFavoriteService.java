package com.example.appointment.service;

import com.example.appointment.dto.PostFavoriteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostFavoriteService {
    /**
     * 添加收藏
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 收藏信息
     */
    PostFavoriteDTO addFavorite(Long postId, Long userId);
    
    /**
     * 取消收藏
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void removeFavorite(Long postId, Long userId);
    
    /**
     * 查询用户是否已收藏帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    boolean hasFavorite(Long postId, Long userId);
    
    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收藏列表
     */
    Page<PostFavoriteDTO> getUserFavorites(Long userId, Pageable pageable);
    
    /**
     * 获取帖子收藏数
     * @param postId 帖子ID
     * @return 收藏数量
     */
    long getPostFavoriteCount(Long postId);
} 