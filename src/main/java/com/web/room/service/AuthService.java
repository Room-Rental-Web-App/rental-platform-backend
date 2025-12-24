package com.web.room.service;

import com.web.room.dto.JwtResponse;
import com.web.room.model.User;
import com.web.room.repository.UserRepository;
import com.web.room.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private EmailService emailService;
    @Autowired private JwtUtils jwtUtils;

    @Transactional
    public String registerRequest(String email, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists!");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encoder.encode(password));
        newUser.setRole(role);
        newUser.setEnabled(false);

        String otp = String.format("%06d", new Random().nextInt(999999));
        System.out.println(otp);
        newUser.setOtp(otp);
        newUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(newUser);
        emailService.sendOtpEmail(email, otp);
        return "OTP_SENT";
    }
    public String verifyAndActivate(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() != null && user.getOtp().equals(otp) && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            user.setEnabled(true);
            user.setOtp(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return jwtUtils.generateToken(email,user.getRole());
        }
        throw new RuntimeException("Invalid or Expired OTP");
    }

    public JwtResponse loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify OTP first.");
        }

        if (encoder.matches(password, user.getPassword())) {
            String token = jwtUtils.generateToken(email, user.getRole());

            // Sab kuch JwtResponse mein pack karke return karein
            return new JwtResponse(token, user.getRole(), user.getEmail());
        } else {
            throw new RuntimeException("Invalid Credentials");
        }
    }
}