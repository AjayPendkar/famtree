package com.famtree.famtree.repository;

import com.famtree.famtree.entity.Subscription;
import com.famtree.famtree.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByFamily(Family family);
} 