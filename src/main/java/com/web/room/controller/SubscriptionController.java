package com.web.room.controller;

import com.web.room.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @GetMapping("/check")
    public ResponseEntity<?> checkPremium(@RequestParam String email,
                                          @RequestParam String role) {
        return service.isPremium (email, role);
    }
    @GetMapping("/my-plans/{email}")
    public ResponseEntity<?> getAllSubscriptions(@PathVariable String email) {
        return service.getAllSubscriptions(email);
    }

}
