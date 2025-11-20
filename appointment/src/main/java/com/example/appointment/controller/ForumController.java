package com.example.appointment.controller;

import com.example.appointment.common.Result;
import com.example.appointment.dto.CommentDTO;
import com.example.appointment.dto.ForumStatsDTO;
import com.example.appointment.dto.PostCategoryDTO;
import com.example.appointment.dto.PostDTO;
import com.example.appointment.dto.PostFavoriteDTO;
import com.example.appointment.entity.Comment;
import com.example.appointment.repository.CommentRepository;
import com.example.appointment.service.CommentService;
import com.example.appointment.service.PostCategoryService;
import com.example.appointment.service.PostFavoriteService;
import com.example.appointment.service.PostService;
import com.example.appointment.service.UserService;
import com.example.appointment.utils.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final PostFavoriteService postFavoriteService;
    private final PostCategoryService categoryService;
    private final CommentRepository commentRepository;

    public ForumController(PostService postService, CommentService commentService, UserService userService, 
            PostFavoriteService postFavoriteService, PostCategoryService categoryService, CommentRepository commentRepository) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
        this.postFavoriteService = postFavoriteService;
        this.categoryService = categoryService;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/posts")
    public Result<Page<PostDTO>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tags) {
        Pageable pageable = PageRequest.of(page, size);
        
        // 如果有分类ID，按分类筛选
        if (categoryId != null) {
            return Result.success(postService.getPostsByCategory(categoryId, pageable));
        }
        
        // 如果有标签参数，按标签筛选
        if (tags != null && !tags.isEmpty()) {
            return Result.success(postService.getPostsByTags(tags, pageable));
        }
        
        // 默认返回所有帖子
        return Result.success(postService.getPosts(pageable));
    }

    @GetMapping("/posts/{id}")
    public Result<PostDTO> getPost(@PathVariable Long id) {
        return Result.success(postService.getPost(id));
    }

    @PostMapping("/posts")
    public Result<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        // 允许未登录用户发帖，使用默认用户ID或从请求中获取
        Long authorId = 1L; // 默认使用admin用户作为匿名发帖者
        try {
            authorId = userService.getCurrentUser().getId();
        } catch (Exception e) {
            // 用户未登录，使用默认用户
            System.out.println("未登录用户发帖，使用默认用户ID: " + authorId);
        }
        postDTO.setAuthorId(authorId);
        return Result.success(postService.createPost(postDTO));
    }

    @PutMapping("/posts/{id}")
    public Result<PostDTO> updatePost(
            @PathVariable Long id,
            @RequestBody PostDTO postDTO) {
        Long userId = userService.getCurrentUser().getId();
        return Result.success(postService.updatePost(id, postDTO, userId));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        postService.deletePostByUser(id, userId);
        return Result.success();
    }

    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        postService.likePost(id);
        return Result.success();
    }

    @GetMapping("/posts/search")
    public Result<Page<PostDTO>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.searchPosts(keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/posts/user/{authorId}")
    public Result<Page<PostDTO>> getPostsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.getPostsByAuthor(authorId, PageRequest.of(page, size)));
    }



    @GetMapping("/posts/latest")
    public Result<Page<PostDTO>> getLatestPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(postService.getLatestPosts(PageRequest.of(page, size)));
    }

    @GetMapping("/posts/{postId}/comments")
    public Result<Page<CommentDTO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("获取评论列表，帖子ID: " + postId + ", page: " + page + ", size: " + size);
        
        // 直接查询数据库，看看是否能找到评论
        List<Comment> directComments = commentRepository.findCommentsByPostIdDirect(postId);
        System.out.println("直接查询到的评论数量: " + directComments.size());
        
        for (Comment comment : directComments) {
            System.out.println("直接查询 - 评论ID: " + comment.getId() + ", 内容: " + comment.getContent());
        }
        
        // 执行分页查询
        Page<CommentDTO> comments = commentService.getCommentsByPostId(postId, PageRequest.of(page, size));
        System.out.println("评论列表结果，总数: " + comments.getTotalElements() + ", 页数: " + comments.getTotalPages());
        
        // 调试输出评论内容
        comments.getContent().forEach(comment -> 
            System.out.println("评论ID: " + comment.getId() + ", 内容: " + comment.getContent()));
        
        return Result.success(comments);
    }
    
    @GetMapping("/latest-comments")
    public Result<List<CommentDTO>> getLatestComments(
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(commentService.getLatestComments(limit));
    }

    @PostMapping("/posts/{postId}/comments")
    public Result<CommentDTO> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDTO commentDTO) {
        // 允许未登录用户评论，使用默认用户ID或从请求中获取
        Long userId = 1L; // 默认使用admin用户作为匿名评论者
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            // 用户未登录，使用默认用户
            System.out.println("未登录用户评论，使用默认用户ID: " + userId);
        }
        CommentDTO createdComment = commentService.createComment(postId, commentDTO, userId);
        return Result.success(createdComment);
    }

    @PutMapping("/posts/{postId}/comments/{commentId}")
    public Result<CommentDTO> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentDTO commentDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        CommentDTO updatedComment = commentService.updateComment(postId, commentId, commentDTO, userId);
        return Result.success(updatedComment);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.deleteComment(postId, commentId, userId);
        return Result.success();
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public Result<Void> likeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.likeComment(postId, commentId);
        return Result.success();
    }
    
    @GetMapping("/stats")
    public Result<ForumStatsDTO> getForumStats() {
        return Result.success(postService.getForumStats());
    }

    /**
     * 获取当前用户的帖子
     */
    @GetMapping("/my-posts")
    public Result<Page<PostDTO>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> postPage = postService.getPostsByAuthorId(userId, pageable);
        return Result.success(postPage);
    }

    /**
     * 收藏帖子
     */
    @PostMapping("/posts/{id}/favorite")
    public Result<PostFavoriteDTO> favoritePost(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(postFavoriteService.addFavorite(id, userId));
    }

    /**
     * 取消收藏帖子
     */
    @DeleteMapping("/posts/{id}/favorite")
    public Result<Void> unfavoritePost(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        postFavoriteService.removeFavorite(id, userId);
        return Result.success();
    }

    /**
     * 获取用户收藏的帖子列表
     */
    @GetMapping("/favorites")
    public Result<Page<PostFavoriteDTO>> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(postFavoriteService.getUserFavorites(userId, pageable));
    }

    /**
     * 检查帖子是否已收藏
     */
    @GetMapping("/posts/{id}/favorite")
    public Result<Boolean> checkPostFavorite(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(postFavoriteService.hasFavorite(id, userId));
    }

    @GetMapping("/categories")
    public Result<List<PostCategoryDTO>> getCategories() {
        // 调用CategoryService获取所有分类
        return Result.success(categoryService.getAllCategories());
    }



    @GetMapping("/posts/{postId}/comments-direct")
    public Result<List<CommentDTO>> getCommentsDirectly(@PathVariable Long postId) {
        System.out.println("直接获取评论列表，帖子ID: " + postId);
        List<Comment> comments = commentService.findCommentsByPostIdDirect(postId);
        System.out.println("直接获取到评论数量: " + comments.size());
        
        for (Comment comment : comments) {
            System.out.println("评论ID: " + comment.getId() + ", 内容: " + comment.getContent());
        }
        
        List<CommentDTO> commentDTOs = comments.stream()
                .map(comment -> {
                    try {
                        CommentDTO dto = new CommentDTO();
                        dto.setId(comment.getId());
                        dto.setContent(comment.getContent());
                        dto.setPostId(comment.getPost().getId());
                        dto.setAuthorId(comment.getAuthor().getId());
                        dto.setAuthorName(comment.getAuthor().getName());
                        dto.setLikes(comment.getLikes());
                        dto.setCreatedAt(comment.getCreatedAt());
                        dto.setUpdatedAt(comment.getUpdatedAt());
                        return dto;
                    } catch (Exception e) {
                        System.err.println("转换评论DTO时出错: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
                
        return Result.success(commentDTOs);
    }
}