package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.Wishlist;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepo;
    private final RoomRepository roomRepo;

    public String add(Long roomId, String email) {
        if (wishlistRepo.findByUserEmailAndRoomId(email, roomId).isPresent())
            return "Already added to wishlist";

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        wishlistRepo.save(
                Wishlist.builder()
                        .userEmail(email)
                        .room(room)
                        .build()
        );
        return "Added to wishlist";
    }

    @Transactional
    public String remove(Long roomId, String email) {

        int deleted = wishlistRepo.deleteWishlist(email, roomId);

        if (deleted == 0) {
            return "Not in wishlist";
        }
        return "Removed from wishlist";
    }

    public List<Wishlist> getUserWishlist(String email) {
        return wishlistRepo.findByUserEmail(email);
    }
}
