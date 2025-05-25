package com.ciconiasystems.ecommerceappbackend.controllers;

import com.ciconiasystems.ecommerceappbackend.dto.ProductDTO;
import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.entities.ProductImageGallery;
import com.ciconiasystems.ecommerceappbackend.exceptions.ConflictException;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ForbiddenException;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.services.CategoryService;
import com.ciconiasystems.ecommerceappbackend.services.IncludedItemService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final IncludedItemService includedItemService;
    private final KeycloakService keycloakService;

    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(required = false) Boolean hero,
            @RequestParam(required = false) Boolean featured) {
        return productService.findAllByHeroAndFeatured(hero, featured);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        Optional<Product> product = productService.findBySlug(slug);
        return responseFromOptional(product);
    }

    @GetMapping("/{slug}/recommendations")
    public List<Product> getRelatedProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "3") int size) {
        return productService.getRelatedProducts(slug, size);
    }

    private Product handleAddProduct(ProductDTO productDTO) throws ValidationException {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setSlug(productDTO.getSlug());

        Optional<Category> category = categoryService.findCategoryBySlug(productDTO.getCategory());
        if (category.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_CATEGORY);
        }
        product.setCategory(category.get());
        product.setImage(productDTO.getImage());
        product.setRecentlyAdded(productDTO.isNew());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setFeatures(productDTO.getFeatures());
        product.setHero(productDTO.isHero());
        product.setFeatured(productDTO.isFeatured());

        for (ProductDTO.Include includeItem : productDTO.getIncludes()) {
            includedItemService.addIncludedItem(product, includeItem.getItem(), includeItem.getQuantity());
        }

        ProductImageGallery imageGallery = product.getImageGallery();
        if (imageGallery == null) {
            imageGallery = new ProductImageGallery();
        }
        imageGallery.setImageGallery1(productDTO.getImageGallery1());
        imageGallery.setImageGallery2(productDTO.getImageGallery2());
        imageGallery.setImageGallery3(productDTO.getImageGallery3());
        product.setImageGallery(imageGallery);

        return productService.saveProduct(product);
    }

    public void addProductsBulk(@RequestBody List<ProductDTO> productDTOs) throws ValidationException {
        for (ProductDTO productDTO : productDTOs) {
            if (!productService.existsByName(productDTO.getName())) {
                handleAddProduct(productDTO);
            }
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> addProduct(@RequestBody ProductDTO productDTO, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (productService.getProductBySlug(productDTO.getSlug()).isPresent()) {
            throw new ConflictException(ErrorCode.DUPLICATE_SLUG);
        }
        if (isAdmin(username)) {
            Product product = handleAddProduct(productDTO);
            return new ResponseEntity<>(product, HttpStatus.CREATED);
        } else {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }
    }

    @PatchMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID productId, @RequestBody ProductDTO updatedProduct, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (isAdmin(username)) {
            if (!productService.productExists(productId)) {
                throw new ValidationException(ErrorCode.INVALID_PRODUCT);
            }
            Optional<Category> category = categoryService.findCategoryBySlug(updatedProduct.getCategory());
            if (category.isEmpty()) {
                throw new ValidationException(ErrorCode.INVALID_CATEGORY);
            } else {
                Product existingProduct = productService.findProductById(productId).orElseThrow(() -> new ValidationException(ErrorCode.INVALID_PRODUCT));
                Product updated;
                if (existingProduct.getPrice() != updatedProduct.getPrice()) {
                    updated = productService.createNewProductWithUpdatedPrice(existingProduct, updatedProduct, category.get());
                } else {
                    updated = productService.updateProduct(productId, updatedProduct, category.get());
                }
                return new ResponseEntity<>(updated, HttpStatus.OK);
            }
        } else {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }
    }


    @GetMapping("/disabled")
    @PreAuthorize("isAuthenticated()")
    public List<Product> getDisabledProducts(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (isAdmin(username)) {
            return productService.findDisabledProducts();
        } else {
            throw new ValidationException(ErrorCode.INVALID_USER);
        }
    }

    @PatchMapping("/enable/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Product> enableProduct(@PathVariable UUID productId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (isAdmin(username)) {
            if (!productService.productExistsAndDisabled(productId)) {
                throw new ValidationException(ErrorCode.INVALID_PRODUCT);
            }
            Product product = productService.enableProduct(productId);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> disableProduct(@PathVariable UUID productId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (isAdmin(username)) {
            if (!productService.productExistsAndEnabled(productId)) {
                throw new ValidationException(ErrorCode.INVALID_PRODUCT);
            }
            productService.disableProductById(productId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }
    }

    private boolean isAdmin(String username) {
        return keycloakService.isWebUserAdmin(username);
    }
}
