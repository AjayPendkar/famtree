package com.famtree.famtree.repository;

import com.famtree.famtree.entity.User;
import com.famtree.famtree.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobile(String mobile);
    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
    List<User> findByIsFamilyHeadTrue();
    Optional<User> findByFamilyAndIsFamilyHeadIsTrue(Family family);
    @Query("SELECT COUNT(u) FROM User u WHERE u.mobile = ?1")
    long countByMobile(String mobile);
} 