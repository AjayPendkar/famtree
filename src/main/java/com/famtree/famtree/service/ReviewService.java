package com.famtree.famtree.service;

import com.famtree.famtree.entity.Review;
import com.famtree.famtree.entity.Family;
import com.famtree.famtree.entity.ConnectionRequest;
import com.famtree.famtree.enums.ConnectionStatus;
import com.famtree.famtree.repository.ReviewRepository;
import com.famtree.famtree.repository.FamilyRepository;
import com.famtree.famtree.repository.ConnectionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FamilyRepository familyRepository;
    private final ConnectionRequestRepository connectionRequestRepository;

    @Transactional
    public void submitReview(Long connectionId, String reviewerFamilyId, Double rating, String review) {
        ConnectionRequest connection = connectionRequestRepository.findById(connectionId)
            .orElseThrow(() -> new RuntimeException("Connection not found"));

        if (connection.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new RuntimeException("Can only review accepted connections");
        }

        Family reviewerFamily = familyRepository.findByFamilyUid(reviewerFamilyId)
            .orElseThrow(() -> new RuntimeException("Reviewer family not found"));

        // Determine which family is being reviewed
        Family reviewedFamily;
        if (connection.getRequesterFamily().equals(reviewerFamily)) {
            reviewedFamily = connection.getReceiverFamily();
        } else if (connection.getReceiverFamily().equals(reviewerFamily)) {
            reviewedFamily = connection.getRequesterFamily();
        } else {
            throw new RuntimeException("Reviewer family is not part of this connection");
        }

        // Check if review already exists
        reviewRepository.findByConnectionAndReviewerFamily(connection, reviewerFamily)
            .ifPresent(existingReview -> {
                throw new RuntimeException("Review already exists");
            });

        Review newReview = Review.builder()
            .connection(connection)
            .reviewerFamily(reviewerFamily)
            .reviewedFamily(reviewedFamily)
            .rating(rating)
            .review(review)
            .build();

        reviewRepository.save(newReview);
    }

    public List<Review> getFamilyReviews(String familyId) {
        Family family = familyRepository.findByFamilyUid(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found"));
        return reviewRepository.findByReviewedFamily(family);
    }

    public Double getFamilyRating(String familyId) {
        Family family = familyRepository.findByFamilyUid(familyId)
            .orElseThrow(() -> new RuntimeException("Family not found"));
        return reviewRepository.getAverageRating(family);
    }

    @Transactional
    public void updateReview(Long reviewId, String reviewerFamilyId, Double rating, String review) {
        Review existingReview = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));

        Family reviewerFamily = familyRepository.findByFamilyUid(reviewerFamilyId)
            .orElseThrow(() -> new RuntimeException("Reviewer family not found"));

        if (!existingReview.getReviewerFamily().equals(reviewerFamily)) {
            throw new RuntimeException("Not authorized to update this review");
        }

        existingReview.setRating(rating);
        existingReview.setReview(review);
        reviewRepository.save(existingReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, String reviewerFamilyId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));

        Family reviewerFamily = familyRepository.findByFamilyUid(reviewerFamilyId)
            .orElseThrow(() -> new RuntimeException("Reviewer family not found"));

        if (!review.getReviewerFamily().equals(reviewerFamily)) {
            throw new RuntimeException("Not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }
} 