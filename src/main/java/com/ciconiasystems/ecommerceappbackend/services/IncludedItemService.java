package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.IncludedItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.IncludedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncludedItemService {
    private final IncludedItemRepository includedItemRepository;

    /**
     * Saves IncludedItems based on the provided product into the database.
     */
    public void addIncludedItem(Product product, String itemName, int quantity) {
        IncludedItem entity = new IncludedItem();
        entity.setQuantity(quantity);
        entity.setItem(itemName);
        includedItemRepository.save(entity);
        product.addIncludedItem(entity);
    }


}
