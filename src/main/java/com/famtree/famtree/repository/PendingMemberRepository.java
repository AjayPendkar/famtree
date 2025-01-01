package com.famtree.famtree.repository;

import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.PendingMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingMemberRepository extends JpaRepository<PendingMember, Long> {
    List<PendingMember> findByFamilyAndIsVerifiedFalse(Family family);
    List<PendingMember> findByFamily(Family family);
    Optional<PendingMember> findByMobileAndIsVerifiedFalse(String mobile);
    Optional<PendingMember> findByMobile(String mobile);
    Optional<PendingMember> findByVerificationCode(String verificationCode);
    Optional<PendingMember> findByMobileAndFamily(String mobile, Family family);
    boolean existsByMobileAndIsVerifiedFalse(String mobile);
    void deleteByFamily(Family family);
} 