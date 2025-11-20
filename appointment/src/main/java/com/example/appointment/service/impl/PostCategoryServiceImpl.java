package com.example.appointment.service.impl;

import com.example.appointment.dto.PostCategoryDTO;
import com.example.appointment.entity.PostCategory;
import com.example.appointment.repository.PostCategoryRepository;
import com.example.appointment.service.PostCategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostCategoryServiceImpl implements PostCategoryService {
    private final PostCategoryRepository categoryRepository;

    public PostCategoryServiceImpl(PostCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<PostCategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostCategoryDTO getCategoryById(Long id) {
        PostCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return convertToDTO(category);
    }

    @Override
    public PostCategoryDTO createCategory(PostCategoryDTO categoryDTO) {
        PostCategory category = new PostCategory();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        PostCategory savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    public PostCategoryDTO updateCategory(Long id, PostCategoryDTO categoryDTO) {
        PostCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        PostCategory updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private PostCategoryDTO convertToDTO(PostCategory category) {
        PostCategoryDTO dto = new PostCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }
} 