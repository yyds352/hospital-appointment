package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.PostCategoryDTO;
import com.example.appointment.service.PostCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-categories")
public class PostCategoryController {
    private final PostCategoryService categoryService;

    public PostCategoryController(PostCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<PostCategoryDTO>> getAllCategories() {
        List<PostCategoryDTO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<PostCategoryDTO> getCategory(@PathVariable Long id) {
        PostCategoryDTO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<PostCategoryDTO> createCategory(@RequestBody PostCategoryDTO categoryDTO) {
        PostCategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return Result.success(createdCategory);
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<PostCategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody PostCategoryDTO categoryDTO) {
        PostCategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return Result.success(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
} 