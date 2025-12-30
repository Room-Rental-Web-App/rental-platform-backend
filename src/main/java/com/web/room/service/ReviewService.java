package com.web.room.service;

import com.web.room.model.Review;
import com.web.room.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ReviewService {

    private final ReviewRepository repo;

    public ReviewService(ReviewRepository repo) {
        this.repo = repo;
    }

    // Add review with validation
    public Review addReview(Review review) {

        if (review.getRating() < 1 || review.getRating() > 5)
            throw new RuntimeException("Rating must be between 1 and 5");

        if (review.getRoomId() == null)
            throw new RuntimeException("Room ID is required");

        if (review.getUserEmail() == null || review.getUserEmail().isEmpty())
            throw new RuntimeException("User email is required");

        Review existing = repo.findByRoomIdAndUserEmail(
                review.getRoomId(),
                review.getUserEmail()
        ).orElse(null);

        if (existing != null) {
            // UPDATE instead of creating duplicate
            existing.setRating(review.getRating());
            existing.setComment(review.getComment());
            return repo.save(existing);
        }

        // INSERT only if not exists
        return repo.save(review);
    }

    // Fetch reviews & calculate average
    public Map<String, Object> getRoomReviews(Long roomId) {
        List<Review> reviews = repo.findByRoomIdOrderByCreatedAtDesc(roomId);
        Double avg = repo.findAverageRating(roomId);

        Map<String, Object> map = new HashMap<>();
        map.put("reviews", reviews);
        map.put("averageRating", avg == null ? 0 : Math.round(avg * 10.0) / 10.0);
        map.put("totalReviews", reviews.size());

        return map;
    }
}
