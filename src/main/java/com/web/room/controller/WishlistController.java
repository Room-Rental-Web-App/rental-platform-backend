package com.web.room.controller;

import com.web.room.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:5173")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<?> add(@PathVariable Long roomId, @RequestParam String email) {
        return ResponseEntity.ok(wishlistService.add(roomId, email));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> remove(@PathVariable Long roomId, @RequestParam String email) {
        System.out.println (roomId + " " + email);
        return ResponseEntity.ok( wishlistService.remove(roomId, email));
    }

    @GetMapping
    public ResponseEntity<?> myWishlist(@RequestParam String email) {
        return ResponseEntity.ok(wishlistService.getUserWishlist(email));
    }
}
