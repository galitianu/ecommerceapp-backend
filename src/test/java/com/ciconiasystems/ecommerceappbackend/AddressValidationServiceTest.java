package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.PlaceInformation;
import com.ciconiasystems.ecommerceappbackend.repositories.PlaceInfoRepository;
import com.ciconiasystems.ecommerceappbackend.services.AddressValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressValidationServiceTest {

    @Mock
    private PlaceInfoRepository placeInfoRepository;

    @InjectMocks
    private AddressValidationService addressValidationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(addressValidationService, "apiKey", "test-api-key");
    }

    @Test
    void testGetValidPlaceInformation_ExistingPlace() {
        String address = "123 Main St";
        String requestCity = "Test City";
        String targetCity = "Test City";

        PlaceInformation existingPlace = new PlaceInformation();
        existingPlace.setAddress(address);
        existingPlace.setCity(targetCity);

        when(placeInfoRepository.findByAddress(address)).thenReturn(existingPlace);

        PlaceInformation result = addressValidationService.getValidPlaceInformation(address, requestCity, targetCity);

        assertNotNull(result);
        assertEquals(existingPlace, result);
        verify(placeInfoRepository, never()).save(any());
    }

    @Test
    void testGetValidPlaceInformation_InvalidCity() {
        String address = "123 Main St";
        String requestCity = "Another City";
        String targetCity = "Test City";

        PlaceInformation result = addressValidationService.getValidPlaceInformation(address, requestCity, targetCity);

        assertNull(result);
        verify(placeInfoRepository, never()).save(any());
    }
}
