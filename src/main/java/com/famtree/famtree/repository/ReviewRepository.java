package com.famtree.famtree.repository;

import com.famtree.famtree.entity.Review;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.ConnectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewedFamily(Family family);
    Optional<Review> findByConnectionAndReviewerFamily(ConnectionRequest connection, Family reviewerFamily);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewedFamily = ?1")
    Double getAverageRating(Family family);
} 