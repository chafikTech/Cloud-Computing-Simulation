package org.example.controller;

import org.example.model.pricing.PricingInfo;
import org.example.repository.PricingInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private PricingInfoRepository pricingRepo;

    @GetMapping
    public List<PricingInfo> getAllPricing() {
        return pricingRepo.findAll();
    }

    @PostMapping
    public PricingInfo createPricing(@RequestBody PricingInfo pricingInfo) {
        return pricingRepo.save(pricingInfo);
    }
}
