package org.example.repository;

import org.example.model.pricing.PricingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingInfoRepository extends JpaRepository<PricingInfo, Long> {
}
