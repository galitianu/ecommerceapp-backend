package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> findCategoryBySlug(String categorySlug) {
        return categoryRepository.findBySlug(categorySlug);
    }

    public List<Category> findAll() {
        return categoryRepository.findByDisabledFalse();
    }

    public boolean categoryExists(String categorySlug) {
        return categoryRepository.existsBySlugAndDisabledFalse(categorySlug);
    }

    public boolean categoryDoesNotExistById(UUID categoryId) {
        return !categoryRepository.existsById(categoryId);
    }

    public Category updateCategory(UUID categoryId, Category updatedCategory) {
        Optional<Category> existingCategoryOptional = categoryRepository.findById(categoryId);
        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();
            existingCategory.setName(updatedCategory.getName());
            existingCategory.setImage(updatedCategory.getImage());
            existingCategory.setSlug(updatedCategory.getSlug());
            return categoryRepository.save(existingCategory);
        } else {
            throw new IllegalArgumentException("Category with slug " + categoryId + " not found");
        }
    }

    public void disableCategoryById(UUID categoryId) {
        Optional<Category> existingCategoryOptional = categoryRepository.findById(categoryId);
        if (existingCategoryOptional.isPresent()) {
            disableAllProductsByCategory(existingCategoryOptional.get().getSlug());
            Category existingCategory = existingCategoryOptional.get();
            existingCategory.setDisabled(true);
            categoryRepository.save(existingCategory);
        } else {
            throw new IllegalArgumentException("Category with ID " + categoryId + " not found");
        }
    }

    private void disableAllProductsByCategory(String categorySlug) {
        List<Product> products = productService.getProductsByCategory(categorySlug);
        for (Product product : products) {
            productService.disableProductById(product.getId());
        }
    }

    public List<Category> findDisabledCategories() {
        return categoryRepository.findByDisabledTrue();
    }

    public Category enableCategory(UUID categoryId) {
        Optional<Category> existingCategoryOptional = categoryRepository.findById(categoryId);
        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();
            existingCategory.setDisabled(false);
            return categoryRepository.save(existingCategory);
        } else {
            throw new IllegalArgumentException("Category with ID " + categoryId + " not found");
        }
    }

    public boolean existsByName(String name) {
        return categoryRepository.findByName(name).isPresent();
    }
}
