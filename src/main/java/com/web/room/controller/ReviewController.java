package com.web.room.controller;

import com.web.room.model.Review;
import com.web.room.service.ReviewService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public Review addReview(@RequestBody Review review) {
        return service.addReview(review);
    }

    @GetMapping("/room/{roomId}")
    public Map<String,Object> get(@PathVariable Long roomId) {
        return service.getRoomReviews(roomId);
    }
}
