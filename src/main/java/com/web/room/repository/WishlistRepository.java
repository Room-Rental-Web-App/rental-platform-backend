package com.web.room.repository;

import com.web.room.model.Wishlist;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserEmail(String userEmail);

    Optional<Wishlist> findByUserEmailAndRoomId(String userEmail, Long roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.userEmail = :email AND w.room.id = :roomId")
    int deleteWishlist(@Param("email") String email, @Param("roomId") Long roomId);
}
