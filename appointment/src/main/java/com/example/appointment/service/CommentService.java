package com.example.appointment.service;

import com.example.appointment.dto.CommentDTO;
import com.example.appointment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    Page<CommentDTO> getCommentsByPostId(Long postId, Pageable pageable);
    
    CommentDTO createComment(Long postId, CommentDTO commentDTO, Long userId);
    
    CommentDTO updateComment(Long postId, Long commentId, CommentDTO commentDTO, Long userId);
    
    void deleteComment(Long postId, Long commentId, Long userId);
    
    void likeComment(Long postId, Long commentId);
    
    List<CommentDTO> getLatestComments(int limit);
    
    /**
     * 获取所有评论（管理员功能）
     * @param pageable 分页参数
     * @return 评论分页列表
     */
    Page<CommentDTO> getAllComments(Pageable pageable);
    
    /**
     * 根据ID删除评论（管理员功能）
     * @param commentId 评论ID
     */
    void deleteCommentById(Long commentId);
    
    List<Comment> findCommentsByPostIdDirect(Long postId);
    
    CommentDTO convertToDTO(Comment comment);
} 