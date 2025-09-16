package com.pdnt.restaurant.service;

import com.pdnt.restaurant.entity.Category;
import com.pdnt.restaurant.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCates() {
        return categoryRepository.findAll();
    }
}
