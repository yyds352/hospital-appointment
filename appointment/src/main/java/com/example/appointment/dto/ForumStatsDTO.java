package com.example.appointment.dto;

import lombok.Data;

@Data
public class ForumStatsDTO {
    private Long totalPosts;
    
    public void setTotalPosts(Long totalPosts) {
        this.totalPosts = totalPosts;
    }
    
    private Long totalComments;
    
    public void setTotalComments(Long totalComments) {
        this.totalComments = totalComments;
    }
    
    private Long totalUsers;
    
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    private Long postsToday;
    
    public void setPostsToday(Long postsToday) {
        this.postsToday = postsToday;
    }
    
    private Long commentsToday;
    
    public void setCommentsToday(Long commentsToday) {
        this.commentsToday = commentsToday;
    }
    
    public ForumStatsDTO() {
        this.totalPosts = 0L;
        this.totalComments = 0L;
        this.totalUsers = 0L;
        this.postsToday = 0L;
        this.commentsToday = 0L;
    }
}