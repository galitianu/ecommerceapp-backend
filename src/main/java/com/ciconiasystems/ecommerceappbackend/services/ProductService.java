package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.dto.ProductDTO;
import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.entities.ProductImageGallery;
import com.ciconiasystems.ecommerceappbackend.repositories.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final IncludedItemService includedItemService;
    @Autowired
    private EntityManager entityManager;

    public List<Product> findAllByHeroAndFeatured(Boolean hero, Boolean featured) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get("disabled")));

        if (hero != null) {
            predicates.add(cb.equal(root.get("hero"), hero));
        }

        if (featured != null) {
            predicates.add(cb.equal(root.get("featured"), featured));
        }

        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public List<Product> getRelatedProducts(String slug, int size) {
        List<Product> products = productRepository.findAll();
        List<Product> filteredProducts = products.stream()
                .filter(product -> !product.getSlug().equals(slug))
                .collect(Collectors.toList());
        Collections.shuffle(filteredProducts);
        return filteredProducts.subList(0, Math.min(size, filteredProducts.size()));
    }


    public List<Product> getProductsByCategory(String categorySlug) {
        return productRepository.findByCategorySlugAndDisabledFalse(categorySlug);
    }

    public Optional<Product> findProductById(UUID productId) {
        return productRepository.findById(productId);
    }

    public List<Product> findDisabledProducts() {
        return productRepository.findByDisabledTrue();
    }

    public boolean productExistsAndEnabled(UUID productId) {
        return productRepository.findByIdAndDisabledFalse(productId).isPresent();
    }

    public void disableProductById(UUID productId) {
        Optional<Product> existingProductOptional = productRepository.findById(productId);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setDisabled(true);
            productRepository.save(existingProduct);
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
    }

    public Product enableProduct(UUID productId) {
        Optional<Product> existingProductOptional = productRepository.findById(productId);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setDisabled(false);
            productRepository.save(existingProduct);
            return existingProduct;
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
    }

    public boolean productExistsAndDisabled(UUID productId) {
        return productRepository.findByIdAndDisabledTrue(productId).isPresent();
    }

    public Optional<Category> findCategoryByProductId(UUID productId) {
        return productRepository.findById(productId)
                .map(Product::getCategory);
    }

    public boolean productExists(UUID productId) {
        return productRepository.existsById(productId);
    }


    public Product updateProduct(UUID productId, ProductDTO updatedProduct, Category updatedCategory) {
        Optional<Product> existingProductOptional = productRepository.findById(productId);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setImage(updatedProduct.getImage());
            existingProduct.setSlug(updatedProduct.getSlug());
            existingProduct.setRecentlyAdded(updatedProduct.isNew());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setFeatures(updatedProduct.getFeatures());
            existingProduct.setHero(updatedProduct.isHero());
            existingProduct.setFeatured(updatedProduct.isFeatured());
            existingProduct.setCategory(updatedCategory);

            existingProduct.getIncludes().clear();

            for (ProductDTO.Include includeItem : updatedProduct.getIncludes()) {
                includedItemService.addIncludedItem(existingProduct, includeItem.getItem(), includeItem.getQuantity());
            }

            ProductImageGallery imageGallery = existingProduct.getImageGallery();
            if (imageGallery == null) {
                imageGallery = new ProductImageGallery();
            }
            imageGallery.setImageGallery1(updatedProduct.getImageGallery1());
            imageGallery.setImageGallery2(updatedProduct.getImageGallery2());
            imageGallery.setImageGallery3(updatedProduct.getImageGallery3());
            existingProduct.setImageGallery(imageGallery);

            return productRepository.save(existingProduct);
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }


    public Product createNewProductWithUpdatedPrice(Product existingProduct, ProductDTO updatedProduct, Category category) {
        // Update the existing product's slug to be unique
        if (Objects.equals(existingProduct.getSlug(), updatedProduct.getSlug())) {
            String oldProductNewSlug = generateUniqueSlug(existingProduct.getSlug());
            existingProduct.setSlug(oldProductNewSlug);
        }
        existingProduct.setDisabled(true);
        productRepository.save(existingProduct);

        // Create a new product with the original slug and updated details
        Product newProduct = new Product();
        newProduct.setName(updatedProduct.getName());
        newProduct.setSlug(updatedProduct.getSlug());
        newProduct.setCategory(category);
        newProduct.setImage(updatedProduct.getImage());
        newProduct.setRecentlyAdded(updatedProduct.isNew());
        newProduct.setPrice(updatedProduct.getPrice());
        newProduct.setDescription(updatedProduct.getDescription());
        newProduct.setFeatures(updatedProduct.getFeatures());
        newProduct.setHero(updatedProduct.isHero());
        newProduct.setFeatured(updatedProduct.isFeatured());

        for (ProductDTO.Include includeItem : updatedProduct.getIncludes()) {
            includedItemService.addIncludedItem(newProduct, includeItem.getItem(), includeItem.getQuantity());
        }

        ProductImageGallery imageGallery = newProduct.getImageGallery();
        if (imageGallery == null) {
            imageGallery = new ProductImageGallery();
        }
        imageGallery.setImageGallery1(updatedProduct.getImageGallery1());
        imageGallery.setImageGallery2(updatedProduct.getImageGallery2());
        imageGallery.setImageGallery3(updatedProduct.getImageGallery3());
        newProduct.setImageGallery(imageGallery);

        return productRepository.save(newProduct);
    }

    private String generateUniqueSlug(String baseSlug) {
        String uniqueSlug = baseSlug;
        int counter = 1;
        while (productRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        return uniqueSlug;
    }


    public boolean existsByName(String name) {
        List<Product> products = productRepository.findByName(name);
        return !products.isEmpty();
    }
}
