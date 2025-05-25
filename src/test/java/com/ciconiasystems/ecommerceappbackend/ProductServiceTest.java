package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.dto.ProductDTO;
import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.entities.ProductImageGallery;
import com.ciconiasystems.ecommerceappbackend.repositories.ProductRepository;
import com.ciconiasystems.ecommerceappbackend.services.IncludedItemService;
import com.ciconiasystems.ecommerceappbackend.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IncludedItemService includedItemService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setSlug("test-slug");
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setDisabled(false);

        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Test Category");
        product.setCategory(category);

        ProductImageGallery imageGallery = new ProductImageGallery();
        imageGallery.setImageGallery1("image1");
        imageGallery.setImageGallery2("image2");
        imageGallery.setImageGallery3("image3");
        product.setImageGallery(imageGallery);
    }

    @Test
    void testSaveProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product savedProduct = productService.saveProduct(product);

        assertNotNull(savedProduct);
        assertEquals(product.getId(), savedProduct.getId());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testFindBySlug() {
        when(productRepository.findBySlug(anyString())).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.findBySlug("test-slug");

        assertTrue(foundProduct.isPresent());
        assertEquals(product.getId(), foundProduct.get().getId());
        verify(productRepository, times(1)).findBySlug("test-slug");
    }

    @Test
    void testGetProductsByCategory() {
        List<Product> products = Collections.singletonList(product);
        when(productRepository.findByCategorySlugAndDisabledFalse(anyString())).thenReturn(products);

        List<Product> productsByCategory = productService.getProductsByCategory("test-category");

        assertNotNull(productsByCategory);
        assertEquals(1, productsByCategory.size());
        assertEquals(product.getId(), productsByCategory.get(0).getId());
        verify(productRepository, times(1)).findByCategorySlugAndDisabledFalse("test-category");
    }

    @Test
    void testFindProductById() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.findProductById(product.getId());

        assertTrue(foundProduct.isPresent());
        assertEquals(product.getId(), foundProduct.get().getId());
        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    void testDisableProductById() {
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

        productService.disableProductById(product.getId());

        assertTrue(product.isDisabled());
        verify(productRepository, times(1)).findById(product.getId());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testEnableProduct() {
        product.setDisabled(true);
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

        Product enabledProduct = productService.enableProduct(product.getId());

        assertNotNull(enabledProduct);
        assertFalse(enabledProduct.isDisabled());
        verify(productRepository, times(1)).findById(product.getId());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct() {
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setName("Updated Product");
        updatedProduct.setSlug("updated-slug");
        updatedProduct.setPrice(200.0);
        updatedProduct.setDescription("Updated description");
        updatedProduct.setFeatures("Updated features");
        updatedProduct.setImage("updated-image");
        updatedProduct.setNew(true);
        updatedProduct.setHero(true);
        updatedProduct.setFeatured(true);
        updatedProduct.setImageGallery1("updated-image1");
        updatedProduct.setImageGallery2("updated-image2");
        updatedProduct.setImageGallery3("updated-image3");
        ProductDTO.Include include = new ProductDTO.Include();
        include.setItem("item1");
        include.setQuantity(10);
        updatedProduct.setIncludes(List.of(include));

        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.updateProduct(product.getId(), updatedProduct, category);

        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("updated-slug", result.getSlug());
        assertEquals(200.0, result.getPrice());
        assertEquals("Updated description", result.getDescription());
        assertEquals("Updated features", result.getFeatures());
        assertEquals("updated-image", result.getImage());
        assertTrue(result.isRecentlyAdded());
        assertTrue(result.isHero());
        assertTrue(result.isFeatured());
        assertEquals("updated-image1", result.getImageGallery().getImageGallery1());
        assertEquals("updated-image2", result.getImageGallery().getImageGallery2());
        assertEquals("updated-image3", result.getImageGallery().getImageGallery3());
        verify(productRepository, times(1)).findById(product.getId());
        verify(productRepository, times(1)).save(product);
        verify(includedItemService, times(1)).addIncludedItem(any(Product.class), eq("item1"), eq(10));
    }
}
