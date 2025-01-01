package com.famtree.famtree.repository;

import com.famtree.famtree.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByFamilyName(String familyName);
    Optional<Family> findByFamilyUid(String familyUid);
    boolean existsByFamilyName(String familyName);
} 