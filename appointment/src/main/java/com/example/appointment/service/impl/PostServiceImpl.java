package com.example.appointment.service.impl;

import com.example.appointment.dto.ForumStatsDTO;
import com.example.appointment.dto.PostDTO;
import com.example.appointment.entity.*;
import com.example.appointment.exception.ResourceNotFoundException;
import com.example.appointment.repository.*;
import com.example.appointment.service.PostService;
import com.example.appointment.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostCategoryRepository categoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostFavoriteRepository postFavoriteRepository;
    private final PostLikeRepository postLikeRepository;

    public PostServiceImpl(
            PostRepository postRepository,
            PostCategoryRepository categoryRepository,
            UserService userService,
            UserRepository userRepository,
            CommentRepository commentRepository,
            PostFavoriteRepository postFavoriteRepository,
            PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postFavoriteRepository = postFavoriteRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    public Page<PostDTO> getPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public PostDTO getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        return convertToDTO(post);
    }

    @Override
    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        // 允许未登录用户发帖，使用默认用户ID或从请求中获取
        Long userId = 1L; // 默认使用admin用户作为匿名发帖者
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
            userId = currentUser.getId();
        } catch (Exception e) {
            // 用户未登录，使用默认用户ID，并创建虚拟用户对象
            System.out.println("未登录用户发帖，使用默认用户ID: " + userId);
            currentUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("默认用户不存在"));
        }
        
        // 如果没有指定分类，使用默认分类
        Long categoryId = postDTO.getCategoryId();
        if (categoryId == null) {
            categoryId = 1L; // 默认分类ID
        }
        
        PostCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setCategory(category);
        post.setAuthor(currentUser);
        post.setLikes(0);
        post.setViews(0);
        
        // 处理标签信息
        if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
            String tagsString = String.join(",", postDTO.getTags());
            post.setTags(tagsString);
        }

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Override
    @Transactional
    public PostDTO updatePost(Long id, PostDTO postDTO, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("帖子不存在"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("您没有权限更新这篇帖子");
        }

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());

        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        try {
            // 删除帖子前先删除与该帖子关联的所有评论
            commentRepository.deleteByPostId(id);
            
            // 删除帖子前先删除与该帖子关联的所有收藏
            postFavoriteRepository.deleteByPostId(id);
            
            // 删除帖子前先删除与该帖子关联的所有点赞
            postLikeRepository.deleteByPostId(id);
            
            // 然后删除帖子
            postRepository.delete(post);
        } catch (Exception e) {
            throw new RuntimeException("删除帖子失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deletePostByUser(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        // 验证用户权限 - 只能删除自己的帖子
        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("您没有权限删除这篇帖子");
        }
        
        try {
            // 删除帖子前先删除与该帖子关联的所有评论
            commentRepository.deleteByPostId(id);
            
            // 删除帖子前先删除与该帖子关联的所有收藏
            postFavoriteRepository.deleteByPostId(id);
            
            // 删除帖子前先删除与该帖子关联的所有点赞
            postLikeRepository.deleteByPostId(id);
            
            // 然后删除帖子
            postRepository.delete(post);
        } catch (Exception e) {
            throw new RuntimeException("删除帖子失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void likePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("帖子不存在");
        }
        
        // 允许未登录用户点赞，使用默认用户ID或从请求中获取
        Long userId = 1L; // 默认使用admin用户作为匿名点赞者
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
            userId = currentUser.getId();
        } catch (Exception e) {
            // 用户未登录，使用默认用户ID，并创建虚拟用户对象
            System.out.println("未登录用户点赞，使用默认用户ID: " + userId);
            currentUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("默认用户不存在"));
        }
        
        // 检查是否已经点赞过
        if (postLikeRepository.existsByPostIdAndUserId(id, userId)) {
            // 已经点赞过，取消点赞
            postLikeRepository.deleteByPostIdAndUserId(id, userId);
            postLikeRepository.decrementLikes(id);
        } else {
            // 未点赞过，添加点赞
            PostLike postLike = new PostLike();
            postLike.setPost(postRepository.findById(id).orElseThrow());
            postLike.setUser(currentUser);
            postLikeRepository.save(postLike);
            postRepository.incrementLikes(id);
        }
    }

    @Override
    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPosts(pageable);
        }
        return postRepository.searchPosts(keyword.trim(), pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findPostsByAuthorOrdered(authorId, pageable)
                .map(this::convertToDTO);
    }



    @Override
    public Page<PostDTO> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findPostsByCategoryOrdered(categoryId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> getPostsByTags(String tags, Pageable pageable) {
        // 支持多个标签，用逗号分隔
        String tagsPattern = "%" + tags + "%";
        return postRepository.findPostsByTags(tagsPattern, pageable)
                .map(this::convertToDTO);
    }


    
    @Override
    public ForumStatsDTO getForumStats() {
        ForumStatsDTO stats = new ForumStatsDTO();
        
        // 获取今天的开始时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        // 设置统计信息
        stats.setTotalPosts(postRepository.countTotalPosts());
        stats.setTotalComments(commentRepository.countTotalComments());
        stats.setTotalUsers(userRepository.count());
        stats.setPostsToday(postRepository.countTodayPosts(todayStart));
        stats.setCommentsToday(commentRepository.countTodayComments(todayStart));
        
        return stats;
    }

    @Override
    public Page<PostDTO> getPostsByAuthorId(Long authorId, Pageable pageable) {
        Page<Post> posts = postRepository.findPostsByAuthorOrdered(authorId, pageable);
        return posts.map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> getLatestPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }



    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategoryId(post.getCategory().getId());
        dto.setAuthorName(post.getAuthor().getUsername());
        dto.setLikes(post.getLikes());
        dto.setViews(post.getViews());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        
        // 处理标签信息
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            dto.setTags(post.getTags());
        }
        
        // 获取当前用户ID（如果已登录）
        Long currentUserId = null;
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                currentUserId = currentUser.getId();
            }
        } catch (Exception e) {
            // 用户未登录，忽略
        }
        
        // 设置点赞状态和点赞数
        if (currentUserId != null) {
            dto.setLiked(postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId));
        } else {
            dto.setLiked(false);
        }
        
        // 设置收藏状态和收藏数
        if (currentUserId != null) {
            dto.setFavorite(postFavoriteRepository.existsByPostIdAndUserId(post.getId(), currentUserId));
        } else {
            dto.setFavorite(false);
        }
        dto.setFavoriteCount(postFavoriteRepository.countByPostId(post.getId()));
        
        // 设置评论数
        dto.setCommentCount(commentRepository.countByPostId(post.getId()));
        
        // 设置分类名称
        if (post.getCategory() != null) {
            dto.setCategoryName(post.getCategory().getName());
        }
        
        return dto;
    }
}