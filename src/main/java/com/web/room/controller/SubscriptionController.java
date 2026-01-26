package com.web.room.controller;

import com.web.room.interfaces.RevenueProjection;
import com.web.room.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping("/revenue")
    public List<RevenueProjection> revenueReport(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {

        System.out.println (role + days + from + to);
        return service.getRevenueReport(
                (role == null || role.isBlank()) ? null : role,
                days,
                from, to
        );
    }


}
