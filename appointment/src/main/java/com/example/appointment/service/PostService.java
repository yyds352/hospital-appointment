package com.example.appointment.service;

import com.example.appointment.dto.ForumStatsDTO;
import com.example.appointment.dto.PostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    Page<PostDTO> getPosts(Pageable pageable);
    PostDTO getPost(Long id);
    PostDTO updatePost(Long id, PostDTO postDTO, Long userId);
    void deletePost(Long id);

    /**
     * 用户删除自己的帖子
     * @param id 帖子ID
     * @param userId 用户ID
     */
    void deletePostByUser(Long id, Long userId);
    void likePost(Long id);
    Page<PostDTO> searchPosts(String keyword, Pageable pageable);
    Page<PostDTO> getPostsByAuthor(Long authorId, Pageable pageable);
    Page<PostDTO> getPostsByCategory(Long categoryId, Pageable pageable);
    Page<PostDTO> getPostsByTags(String tags, Pageable pageable);
    ForumStatsDTO getForumStats();
    /**
     * 获取用户发布的帖子
     * @param authorId 作者ID
     * @param pageable 分页参数
     * @return 帖子分页列表
     */
    Page<PostDTO> getPostsByAuthorId(Long authorId, Pageable pageable);

    /**
     * 获取最新帖子
     * @param pageable 分页参数
     * @return 帖子分页列表
     */
    Page<PostDTO> getLatestPosts(Pageable pageable);
}