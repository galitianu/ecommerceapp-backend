package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.PlaceInformation;
import com.ciconiasystems.ecommerceappbackend.repositories.PlaceInfoRepository;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AddressValidationService {

    private final PlaceInfoRepository placeInfoRepository;

    @Value("${google.maps.api-key}")
    private String apiKey;

    public AddressValidationService(PlaceInfoRepository placeInfoRepository) {
        this.placeInfoRepository = placeInfoRepository;
    }


    public PlaceInformation getValidPlaceInformation(String address, String requestCity, String targetCity) {
        if (!Objects.equals(requestCity, targetCity) && !Objects.equals(requestCity, "Cluj-Napoca")) {
            return null;
        }

        PlaceInformation existingPlace = placeInfoRepository.findByAddress(address);
        if (existingPlace != null && existingPlace.getCity().equalsIgnoreCase(targetCity)) {
            return existingPlace;
        }

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address + ", " + targetCity).await();
            for (GeocodingResult result : results) {
                if (result.formattedAddress.toLowerCase().contains(targetCity.toLowerCase()) && !result.partialMatch) {
                    LatLng location = result.geometry.location;

                    PlaceInformation newPlace = new PlaceInformation();
                    newPlace.setAddress(address);
                    newPlace.setCity(targetCity);
                    newPlace.setLatitude(location.lat);
                    newPlace.setLongitude(location.lng);
                    placeInfoRepository.save(newPlace);
                    return newPlace;
                }
            }
            return null;
        }
        catch (Exception ignored) {}
        return null;
    }
}
