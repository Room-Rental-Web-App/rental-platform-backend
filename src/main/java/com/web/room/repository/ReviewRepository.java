package com.web.room.repository;

import com.web.room.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    Optional<Review> findByRoomIdAndUserEmail(Long roomId, String userEmail);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.roomId = :roomId")
    Double findAverageRating(Long roomId);
}
