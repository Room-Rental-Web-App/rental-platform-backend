package com.web.room.controller;

import com.web.room.dto.JwtResponse;
import com.web.room.dto.LoginRequest;
import com.web.room.dto.OtpRequest;
import com.web.room.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register-request")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        try {
            String res = authService.registerRequest(req.get("email"), req.get("password"), req.get("role"));
            return ResponseEntity.ok(Map.of("message", res));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody OtpRequest req) {
        try {
            String token = authService.verifyAndActivate(req.getEmail(), req.getOtp());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/login-request")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            JwtResponse response = authService.loginUser(req.getEmail(), req.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}