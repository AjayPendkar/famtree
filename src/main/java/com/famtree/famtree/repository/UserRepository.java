package com.famtree.famtree.repository;

import com.famtree.famtree.entity.User;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobile(String mobile);
    Optional<User> findByEmail(String email);
    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByRole(UserRole role);
    long countByMobile(String mobile);
    Optional<User> findByFamilyAndIsFamilyHeadIsTrue(Family family);
    List<User> findByIsFamilyHeadTrue();
    Optional<User> findByMemberUid(String memberUid);
} 