package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.BillingInformation;
import com.ciconiasystems.ecommerceappbackend.repositories.BillingInformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BillingInformationService {
    private final BillingInformationRepository billingInformationRepository;

    public BillingInformation save(BillingInformation billingInformation) {
        return billingInformationRepository.save(billingInformation);
    }
}
