package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.IncludedItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.IncludedItemRepository;
import com.ciconiasystems.ecommerceappbackend.services.IncludedItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncludedItemServiceTest {

    @Mock
    private IncludedItemRepository includedItemRepository;

    @InjectMocks
    private IncludedItemService includedItemService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        IncludedItem includedItem = new IncludedItem();
        includedItem.setItem("Test Item");
        includedItem.setQuantity(5);

        // Mocking the save method to return the includedItem when called
        when(includedItemRepository.save(any(IncludedItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testAddIncludedItem() {
        includedItemService.addIncludedItem(product, "Test Item", 5);

        assertNotNull(product.getIncludes());
        assertEquals(1, product.getIncludes().size());
        IncludedItem result = product.getIncludes().get(0);
        assertEquals("Test Item", result.getItem());
        assertEquals(5, result.getQuantity());
        verify(includedItemRepository, times(1)).save(any(IncludedItem.class));
    }
}
