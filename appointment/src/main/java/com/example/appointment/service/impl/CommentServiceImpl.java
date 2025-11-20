package com.example.appointment.service.impl;

import com.example.appointment.dto.CommentDTO;
import com.example.appointment.entity.Comment;
import com.example.appointment.entity.Post;
import com.example.appointment.entity.User;
import com.example.appointment.repository.CommentRepository;
import com.example.appointment.repository.PostRepository;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.service.CommentService;
import com.example.appointment.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                            PostRepository postRepository,
                            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<CommentDTO> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findCommentsByPostOrdered(postId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CommentDTO createComment(Long postId, CommentDTO commentDTO, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setLikes(0);

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Long postId, Long commentId, CommentDTO commentDTO, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalStateException("Comment does not belong to the specified post");
        }

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to update this comment");
        }

        comment.setContent(commentDTO.getContent());

        Comment updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalStateException("Comment does not belong to the specified post");
        }

        // 获取当前用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // 如果是管理员或者评论的作者，则允许删除
        if (!comment.getAuthor().getId().equals(userId) && !"ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void likeComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalStateException("Comment does not belong to the specified post");
        }

        commentRepository.incrementLikes(commentId);
    }

    @Override
    public List<CommentDTO> getLatestComments(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Comment> comments = commentRepository.findTopByOrderByCreatedAtDesc(pageable);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentDTO> getAllComments(Pageable pageable) {
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void deleteCommentById(Long commentId) {
        if (!commentRepository.existsByIdCustom(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<Comment> findCommentsByPostIdDirect(Long postId) {
        return commentRepository.findCommentsByPostIdDirect(postId);
    }

    public CommentDTO convertToDTO(Comment comment) {
        System.out.println("Converting comment to DTO: " + comment);
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        
        try {
            dto.setPostId(comment.getPost().getId());
            dto.setAuthorId(comment.getAuthor().getId());
            dto.setAuthorName(comment.getAuthor().getName());
        } catch (Exception e) {
            System.err.println("Error during comment DTO conversion: " + e.getMessage());
            e.printStackTrace();
            
            // 使用安全的方式设置属性
            if (comment.getPost() != null) {
                dto.setPostId(comment.getPost().getId());
            }
            
            if (comment.getAuthor() != null) {
                dto.setAuthorId(comment.getAuthor().getId());
                dto.setAuthorName(comment.getAuthor().getName() != null ? 
                    comment.getAuthor().getName() : comment.getAuthor().getUsername());
            }
        }
        
        System.out.println("Converted to DTO: " + dto);
        return dto;
    }
}