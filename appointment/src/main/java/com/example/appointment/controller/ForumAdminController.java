package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.CommentDTO;
import com.example.appointment.dto.PostCategoryDTO;
import com.example.appointment.dto.PostDTO;
import com.example.appointment.service.CommentService;
import com.example.appointment.service.PostCategoryService;
import com.example.appointment.service.PostService;
import com.example.appointment.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛管理接口
 */
@RestController
@RequestMapping("/api/admin/forum")
public class ForumAdminController {
    
    private final PostService postService;
    private final CommentService commentService;
    private final PostCategoryService postCategoryService;
    private final UserService userService;
    
    public ForumAdminController(
            PostService postService, 
            CommentService commentService, 
            PostCategoryService postCategoryService,
            UserService userService) {
        this.postService = postService;
        this.commentService = commentService;
        this.postCategoryService = postCategoryService;
        this.userService = userService;
    }
    
    /**
     * 管理员获取所有帖子（带筛选条件）
     */
    @GetMapping("/posts")
    @RequireRole({"ADMIN"})
    public Result<Page<PostDTO>> getAllPosts(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostDTO> posts;
        
        // 记录请求参数
        System.out.println("管理员获取帖子列表，参数: authorId=" + authorId 
            + ", categoryId=" + categoryId 
            + ", keyword=" + keyword
            + ", query=" + query
            + ", status=" + status
            + ", category=" + category
            + ", page=" + page
            + ", size=" + size);
        
        // 使用query参数作为关键词搜索，与keyword参数合并处理
        String searchKeyword = keyword;
        if (query != null && !query.isEmpty()) {
            searchKeyword = query;
        }
        
        // 处理分类筛选 - 如果传入的是分类名称而非ID
        Long filterCategoryId = categoryId;
        if (category != null && !category.isEmpty()) {
            try {
                // 先尝试将category参数转为Long，如果能转换，说明传的是ID
                filterCategoryId = Long.parseLong(category);
            } catch (NumberFormatException e) {
                // 如果转换失败，则可能传入的是分类名称或标识，需要查询对应的ID
                // 这里可以调用服务获取分类ID，这里简化处理
                System.out.println("分类名称筛选: " + category);
            }
        }
        
        if (authorId != null) {
            posts = postService.getPostsByAuthor(authorId, pageable);
        } else if (filterCategoryId != null) {
            posts = postService.getPostsByCategory(filterCategoryId, pageable);
        } else if (searchKeyword != null && !searchKeyword.isEmpty()) {
            posts = postService.searchPosts(searchKeyword, pageable);
        } else {
            posts = postService.getPosts(pageable);
        }
        
        System.out.println("查询结果: 总条数=" + posts.getTotalElements() + ", 总页数=" + posts.getTotalPages());
        
        return Result.success(posts);
    }
    
    /**
     * 管理员删除帖子
     */
    @DeleteMapping("/posts/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success();
    }
    
    /**
     * 管理员获取所有评论
     */
    @GetMapping("/comments")
    @RequireRole({"ADMIN"})
    public Result<Page<CommentDTO>> getAllComments(
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (postId != null) {
            return Result.success(commentService.getCommentsByPostId(postId, pageable));
        }
        
        // 这里需要实现一个通用的获取所有评论的方法
        // 为简化，这里假设该方法已存在
        return Result.success(commentService.getAllComments(pageable));
    }
    
    /**
     * 管理员删除评论
     */
    @DeleteMapping("/comments/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteCommentById(id);
        return Result.success();
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    @RequireRole({"ADMIN"})
    public Result<List<PostCategoryDTO>> getAllCategories() {
        return Result.success(postCategoryService.getAllCategories());
    }
    
    /**
     * 创建分类
     */
    @PostMapping("/categories")
    @RequireRole({"ADMIN"})
    public Result<PostCategoryDTO> createCategory(@RequestBody PostCategoryDTO categoryDTO) {
        return Result.success(postCategoryService.createCategory(categoryDTO));
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    @RequireRole({"ADMIN"})
    public Result<PostCategoryDTO> updateCategory(
            @PathVariable Long id, 
            @RequestBody PostCategoryDTO categoryDTO) {
        return Result.success(postCategoryService.updateCategory(id, categoryDTO));
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> deleteCategory(@PathVariable Long id) {
        postCategoryService.deleteCategory(id);
        return Result.success();
    }
} 