package com.example.appointment.service.impl;

import com.example.appointment.dto.PostFavoriteDTO;
import com.example.appointment.entity.Post;
import com.example.appointment.entity.PostFavorite;
import com.example.appointment.entity.User;
import com.example.appointment.exception.ResourceNotFoundException;
import com.example.appointment.repository.PostFavoriteRepository;
import com.example.appointment.repository.PostRepository;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.service.PostFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostFavoriteServiceImpl implements PostFavoriteService {
    
    private static final Logger log = LoggerFactory.getLogger(PostFavoriteServiceImpl.class);
    
    private final PostFavoriteRepository postFavoriteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    public PostFavoriteServiceImpl(
            PostFavoriteRepository postFavoriteRepository,
            PostRepository postRepository,
            UserRepository userRepository) {
        this.postFavoriteRepository = postFavoriteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public PostFavoriteDTO addFavorite(Long postId, Long userId) {
        // 检查是否已收藏
        if (postFavoriteRepository.existsByPostIdAndUserId(postId, userId)) {
            log.info("用户 {} 已收藏帖子 {}", userId, postId);
            return postFavoriteRepository.findByPostIdAndUserId(postId, userId)
                    .map(this::convertToDTO)
                    .orElse(null);
        }
        
        // 查找帖子
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // 创建收藏
        PostFavorite favorite = new PostFavorite();
        favorite.setPost(post);
        favorite.setUser(user);
        
        PostFavorite savedFavorite = postFavoriteRepository.save(favorite);
        log.info("用户 {} 成功收藏帖子 {}", userId, postId);
        
        return convertToDTO(savedFavorite);
    }
    
    @Override
    @Transactional
    public void removeFavorite(Long postId, Long userId) {
        if (!postFavoriteRepository.existsByPostIdAndUserId(postId, userId)) {
            log.info("用户 {} 未收藏帖子 {}", userId, postId);
            return;
        }
        
        postFavoriteRepository.deleteByPostIdAndUserId(postId, userId);
        log.info("用户 {} 成功取消收藏帖子 {}", userId, postId);
    }
    
    @Override
    public boolean hasFavorite(Long postId, Long userId) {
        return postFavoriteRepository.existsByPostIdAndUserId(postId, userId);
    }
    
    @Override
    public Page<PostFavoriteDTO> getUserFavorites(Long userId, Pageable pageable) {
        Page<PostFavorite> favorites = postFavoriteRepository.findByUserId(userId, pageable);
        return favorites.map(this::convertToDTO);
    }
    
    @Override
    public long getPostFavoriteCount(Long postId) {
        return postFavoriteRepository.countByPostId(postId);
    }
    
    private PostFavoriteDTO convertToDTO(PostFavorite favorite) {
        PostFavoriteDTO dto = new PostFavoriteDTO();
        dto.setId(favorite.getId());
        dto.setPostId(favorite.getPost().getId());
        dto.setPostTitle(favorite.getPost().getTitle());
        dto.setUserId(favorite.getUser().getId());
        dto.setUsername(favorite.getUser().getUsername());
        dto.setCreatedAt(favorite.getCreatedAt());
        
        // 添加帖子内容
        dto.setContent(favorite.getPost().getContent());
        
        // 添加分类名称
        if (favorite.getPost().getCategory() != null) {
            dto.setCategoryName(favorite.getPost().getCategory().getName());
        } else {
            dto.setCategoryName("未分类");
        }
        
        return dto;
    }
}