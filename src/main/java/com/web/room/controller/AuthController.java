package com.web.room.controller;

import com.web.room.dto.PasswordEmail.ChangePasswordRequest;
import com.web.room.dto.PasswordEmail.EmailRequest;
import com.web.room.dto.PasswordEmail.OtpVerifyRequest;
import com.web.room.dto.PasswordEmail.ResetPasswordRequest;
import com.web.room.dto.Response.JwtResponse;
import com.web.room.dto.Request.RegistrationRequest;
import com.web.room.dto.Request.LoginRequest;
import com.web.room.dto.Request.OtpRequest;
import com.web.room.service.AuthService;
import com.web.room.service.EmailService;
import com.web.room.service.GoogleAuthService;
import com.web.room.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final EmailService emailService;
    private final UserService userService;

    /**
     * Handles user registration with Multipart Data (Aadhar Card Image).
     * Consumes: multipart/form-data
     */

    @PostMapping(value = "/register-request", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(@ModelAttribute RegistrationRequest req) {
        try {
            String res = authService.registerRequest(req);
            return ResponseEntity.ok(Map.of("message", res));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/google-login")
    public JwtResponse googleLogin(@RequestBody Map<String,String> body) throws GeneralSecurityException, IOException {
        return googleAuthService.loginWithGoogle(body.get("token"));
    }

    @PostMapping(value = "/google/complete-registration",consumes = {"multipart/form-data"})
    public JwtResponse completeRegistrationAndLogin(@ModelAttribute RegistrationRequest req) {
        return googleAuthService.completeRegistrationAndLogin(req);
    }

    @PostMapping("/send-otp/{email}")
    public ResponseEntity<?> sendOTP(@PathVariable String email) {
        try {
            String otp = authService.sendOTP (email);
            return ResponseEntity.ok (otp);
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
    @PostMapping("/forgot-password-otp")
    public ResponseEntity<?> forgotPasswordOtp(@RequestBody EmailRequest request){
        return authService.forgotPasswordOtp(request.getEmail());
    }

    @PostMapping("/forgot-verify-otp")
    public ResponseEntity<?> forgotVerifyOtp(@RequestBody OtpVerifyRequest request){
        return authService.forgotVerifyOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ResetPasswordRequest request){
        return authService.forgotPassword(request.getEmail(), request.getPassword());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ChangePasswordRequest request){
        return authService.resetPassword(
                request.getEmail(),
                request.getOldPassword(),
                request.getNewPassword()
        );
    }

    @PostMapping(value = "/upgrade-to-owner", consumes = {"multipart/form-data"})
    public ResponseEntity<?> upgradeToOwner(
            @RequestParam("aadharCard") org.springframework.web.multipart.MultipartFile aadharCard,
            @RequestParam("email") String email) {
        try {
            String res = authService.upgradeRequest(email, aadharCard);
            return ResponseEntity.ok(java.util.Map.of("message", res));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}