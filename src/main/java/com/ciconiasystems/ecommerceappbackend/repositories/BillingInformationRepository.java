package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.BillingInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BillingInformationRepository extends JpaRepository<BillingInformation, UUID> {
}
