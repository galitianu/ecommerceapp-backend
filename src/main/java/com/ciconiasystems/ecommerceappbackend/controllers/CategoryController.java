package com.ciconiasystems.ecommerceappbackend.controllers;

import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ForbiddenException;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.services.CategoryService;
import com.ciconiasystems.ecommerceappbackend.services.KeycloakService;
import com.ciconiasystems.ecommerceappbackend.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final ProductService productService;
    private final KeycloakService keycloakService;

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> disableCategory(@PathVariable UUID categoryId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (!keycloakService.isWebUserAdmin(username)) {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }

        if (categoryService.categoryDoesNotExistById(categoryId)) {
            throw new ValidationException(ErrorCode.INVALID_CATEGORY);
        }

        categoryService.disableCategoryById(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Category> updateCategory(@PathVariable UUID categoryId, @RequestBody Category updatedCategory, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (!keycloakService.isWebUserAdmin(username)) {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }

        if (categoryService.categoryDoesNotExistById(categoryId)) {
            throw new ValidationException(ErrorCode.INVALID_CATEGORY);
        }

        Category category = categoryService.updateCategory(categoryId, updatedCategory);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Category> addCategory(@RequestBody Category category, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (!keycloakService.isWebUserAdmin(username)) {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }

        Category savedCategory = categoryService.saveCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Category> getCategoryByProductId(@PathVariable UUID productId) {
        Optional<Category> category = productService.findCategoryByProductId(productId);
        if (category.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT);
        }

        return new ResponseEntity<>(category.get(), HttpStatus.OK);
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/{categorySlug}/products")
    public List<Product> getProductsByCategoryId(@PathVariable String categorySlug) {
        if (!categoryService.categoryExists(categorySlug)) {
            throw new ValidationException(ErrorCode.INVALID_CATEGORY);
        }

        return productService.getProductsByCategory(categorySlug);
    }


    public void addCategoriesBulk(@RequestBody List<Category> categories) throws ValidationException {
        for (Category category : categories) {
            if (!categoryService.existsByName(category.getName())) {
                categoryService.saveCategory(category);
            }
        }
    }

    @GetMapping("/disabled")
    @PreAuthorize("isAuthenticated()")
    public List<Category> getDisabledCategories(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (!keycloakService.isWebUserAdmin(username)) {
            throw new ValidationException(ErrorCode.INVALID_USER);
        }

        return categoryService.findDisabledCategories();
    }

    @PatchMapping("/enable/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Category> enableCategory(@PathVariable UUID categoryId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (!keycloakService.isWebUserAdmin(username)) {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }

        if (categoryService.categoryDoesNotExistById(categoryId)) {
            throw new ValidationException(ErrorCode.INVALID_CATEGORY);
        }

        Category category = categoryService.enableCategory(categoryId);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }
}
