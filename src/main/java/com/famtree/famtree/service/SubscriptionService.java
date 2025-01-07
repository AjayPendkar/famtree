package com.famtree.famtree.service;

import com.famtree.famtree.dto.SubscriptionDTO;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.Subscription;
import com.famtree.famtree.enums.SubscriptionPlan;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final FamilyRepository familyRepository;

    public SubscriptionDTO getSubscriptionDetails(String familyId) {
        Family family = familyRepository.findByFamilyUid(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found"));

        Subscription subscription = subscriptionRepository.findByFamily(family)
            .orElse(Subscription.builder()
                .plan(SubscriptionPlan.FREE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(100))
                .build());

        return SubscriptionDTO.builder()
            .status("active")
            .plan(subscription.getPlan().toString())
            .features(SubscriptionDTO.Features.builder()
                .maxRequests(subscription.getPlan().hasUnlimitedRequests() ? 
                    "unlimited" : String.valueOf(subscription.getPlan().getMaxRequests()))
                .expiresAt(subscription.getEndDate())
                .build())
            .build();
    }

    @Transactional
    public void upgradeSubscription(String familyId, String plan) {
        Family family = familyRepository.findByFamilyUid(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found"));

        SubscriptionPlan subscriptionPlan = SubscriptionPlan.valueOf(plan.toUpperCase());
        
        Subscription subscription = subscriptionRepository.findByFamily(family)
            .orElse(new Subscription());

        subscription.setFamily(family);
        subscription.setPlan(subscriptionPlan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));

        subscriptionRepository.save(subscription);
    }
} 