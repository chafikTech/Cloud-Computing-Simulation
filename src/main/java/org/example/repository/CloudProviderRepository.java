package org.example.repository;


import org.example.model.pricing.CloudProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudProviderRepository extends JpaRepository<CloudProvider, Long> {
}
