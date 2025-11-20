package com.example.appointment.service;

import com.example.appointment.dto.PostCategoryDTO;
import java.util.List;

public interface PostCategoryService {
    List<PostCategoryDTO> getAllCategories();
    
    PostCategoryDTO getCategoryById(Long id);
    
    PostCategoryDTO createCategory(PostCategoryDTO categoryDTO);
    
    PostCategoryDTO updateCategory(Long id, PostCategoryDTO categoryDTO);
    
    void deleteCategory(Long id);
} 