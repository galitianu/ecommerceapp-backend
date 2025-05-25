package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.CategoryRepository;
import com.ciconiasystems.ecommerceappbackend.services.CategoryService;
import com.ciconiasystems.ecommerceappbackend.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveCategory() {
        Category category = new Category();
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = categoryService.saveCategory(category);

        assertNotNull(result);
        assertEquals(category, result);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void testFindCategoryBySlug() {
        String categorySlug = "test-slug";
        Category expectedCategory = new Category();
        when(categoryRepository.findBySlug(categorySlug)).thenReturn(Optional.of(expectedCategory));

        Optional<Category> result = categoryService.findCategoryBySlug(categorySlug);

        assertTrue(result.isPresent());
        assertEquals(expectedCategory, result.get());
    }

    @Test
    void testFindCategoryBySlugNotFound() {
        String categorySlug = "test-slug";
        when(categoryRepository.findBySlug(categorySlug)).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.findCategoryBySlug(categorySlug);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        List<Category> expectedCategories = List.of(new Category(), new Category());
        when(categoryRepository.findByDisabledFalse()).thenReturn(expectedCategories);

        List<Category> result = categoryService.findAll();

        assertNotNull(result);
        assertEquals(expectedCategories.size(), result.size());
    }

    @Test
    void testCategoryExists() {
        String categorySlug = "test-slug";
        when(categoryRepository.existsBySlugAndDisabledFalse(categorySlug)).thenReturn(true);

        boolean result = categoryService.categoryExists(categorySlug);

        assertTrue(result);
    }

    @Test
    void testCategoryDoesNotExistById() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        boolean result = categoryService.categoryDoesNotExistById(categoryId);

        assertTrue(result);
    }

    @Test
    void testUpdateCategory() {
        UUID categoryId = UUID.randomUUID();
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        Category updatedCategory = new Category();
        updatedCategory.setName("Updated Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        Category result = categoryService.updateCategory(categoryId, updatedCategory);

        assertNotNull(result);
        assertEquals(updatedCategory.getName(), result.getName());
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void testUpdateCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        Category updatedCategory = new Category();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(categoryId, updatedCategory));
        assertEquals("Category with slug " + categoryId + " not found", exception.getMessage());
    }

    @Test
    void testDisableCategoryById() {
        UUID categoryId = UUID.randomUUID();
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setSlug("test-slug");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        Product product1 = new Product();
        product1.setId(UUID.randomUUID());
        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        when(productService.getProductsByCategory("test-slug")).thenReturn(List.of(product1, product2));
        doNothing().when(productService).disableProductById(any(UUID.class));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);
        categoryService.disableCategoryById(categoryId);

        assertTrue(existingCategory.isDisabled());
        verify(productService, times(1)).disableProductById(product1.getId());
        verify(productService, times(1)).disableProductById(product2.getId());
        verify(categoryRepository, times(1)).save(existingCategory);
    }


    @Test
    void testDisableCategoryByIdNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> categoryService.disableCategoryById(categoryId));
        assertEquals("Category with ID " + categoryId + " not found", exception.getMessage());
    }

    @Test
    void testFindDisabledCategories() {
        List<Category> expectedCategories = List.of(new Category(), new Category());
        when(categoryRepository.findByDisabledTrue()).thenReturn(expectedCategories);

        List<Category> result = categoryService.findDisabledCategories();

        assertNotNull(result);
        assertEquals(expectedCategories.size(), result.size());
    }

    @Test
    void testEnableCategory() {
        UUID categoryId = UUID.randomUUID();
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setDisabled(true);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        Category result = categoryService.enableCategory(categoryId);

        assertNotNull(result);
        assertFalse(result.isDisabled());
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void testEnableCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> categoryService.enableCategory(categoryId));
        assertEquals("Category with ID " + categoryId + " not found", exception.getMessage());
    }
}
