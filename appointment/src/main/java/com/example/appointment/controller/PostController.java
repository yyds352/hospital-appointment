package com.example.appointment.controller;

import com.example.appointment.common.Result;
import com.example.appointment.dto.PostDTO;
import com.example.appointment.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public Result<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        return Result.success(postService.createPost(postDTO));
    }

    @GetMapping
    public Result<Page<PostDTO>> getPosts(Pageable pageable) {
        return Result.success(postService.getPosts(pageable));
    }

    @GetMapping("/{id}")
    public Result<PostDTO> getPost(@PathVariable Long id) {
        return Result.success(postService.getPost(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success();
    }
} 