package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.controllers.CategoryController;
import com.ciconiasystems.ecommerceappbackend.controllers.ProductController;
import com.ciconiasystems.ecommerceappbackend.dto.ProductDTO;
import com.ciconiasystems.ecommerceappbackend.entities.Category;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartupService {
    private final ProductController productController;
    private final CategoryController categoryController;

    @Value("classpath:data-categories.json")
    private Resource categoryData;

    @Value("classpath:data-products.json")
    private Resource productData;

    @EventListener(ApplicationReadyEvent.class)
    public void startApplication() throws Exception {
        addCategoriesFromJsonFile();
        addProductsFromJsonFile();
    }


    public void addCategoriesFromJsonFile() throws Exception {
        // 1. Read the JSON file
        InputStream inputStream = categoryData.getInputStream();
        byte[] jsonData = inputStream.readAllBytes();

        // 2. Deserialize the JSON data
        ObjectMapper objectMapper = new ObjectMapper();
        List<Category> categories = objectMapper.readValue(jsonData, new TypeReference<>() {
        });

        // 3. Pass the deserialized data to your method
        categoryController.addCategoriesBulk(categories);

        log.info("Added categories");
    }

    public void addProductsFromJsonFile() throws Exception {
        // 1. Read the JSON file
        InputStream inputStream = productData.getInputStream();
        byte[] jsonData = inputStream.readAllBytes();

        // 2. Deserialize the JSON data
        ObjectMapper objectMapper = new ObjectMapper();
        List<ProductDTO> productDTOs = objectMapper.readValue(jsonData, new TypeReference<>() {
        });

        // 3. Pass the deserialized data to your method
        productController.addProductsBulk(productDTOs);

        log.info("Added products");
    }
}
