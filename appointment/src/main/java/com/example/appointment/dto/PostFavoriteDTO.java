package com.example.appointment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostFavoriteDTO {
    private Long id;
    
    public void setId(Long id) {
        this.id = id;
    }
    
    private Long postId;
    
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    
    private String postTitle;
    
    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }
    
    private Long userId;
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    private String username;
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    private LocalDateTime createdAt;
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    private String content;
    
    public void setContent(String content) {
        this.content = content;
    }
    
    private String categoryName;
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}