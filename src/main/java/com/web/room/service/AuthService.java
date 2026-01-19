package com.web.room.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.room.dto.Request.RegistrationRequest;
import com.web.room.dto.Response.JwtResponse;
import com.web.room.model.User;
import com.web.room.repository.UserRepository;
import com.web.room.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private Cloudinary cloudinary;

    @Transactional
    public String registerRequest(RegistrationRequest req) {
        if (userRepository.findByEmail (req.getEmail ()).isPresent ()) {
            throw new RuntimeException ("User already exists!");
        }

        User newUser = new User ();
        newUser.setEmail (req.getEmail ());
        newUser.setPassword (encoder.encode (req.getPassword ()));
        newUser.setRole (req.getRole ());
        newUser.setPhone (req.getPhone ()); // Mobile number save kar rahe hain
        newUser.setEnabled (false);


        // --- Logic for Cloudinary & Status ---
        newUser.setStatus ("PENDING"); // Owner approval ke liye rukega
        if (req.getAadharCard () != null && !req.getAadharCard ().isEmpty ()) {
            try {
                // Uploading to Cloudinary
                Map uploadResult = cloudinary.uploader ().upload (req.getAadharCard ().getBytes (),
                        ObjectUtils.asMap ("folder", "aadhar_cards"));

                String secureUrl = (String) uploadResult.get ("secure_url");
                newUser.setAadharUrl (secureUrl); // Database mein URL save hoga
            } catch (IOException e) {
                throw new RuntimeException ("Image upload failed: " + e.getMessage ());
            }
        } else {
            throw new RuntimeException ("Aadhar Card photo is required for Owners!");
        }


        // --- OTP Logic ---
        String otp = String.format ("%06d", new Random ().nextInt (999999));
        System.out.println (otp);
        newUser.setOtp (otp);

        newUser.setOtpExpiry (LocalDateTime.now ().plusMinutes (5));

        userRepository.save (newUser);
        String subject = "Login OTP - Room Web App";

        String body = "Dear User,\n\n" +
                "Your OTP for logging into Room Web App is: " + otp + "\n" +
                "This OTP is valid for 5 minutes only.\n\n" +
                "If you didn't request this, please ignore this email.";

        emailService.sendOtpEmail (req.getEmail (), subject, body);
        return "OTP_SENT";
    }

    public String verifyAndActivate(String email, String otp) {
        User user = userRepository.findByEmail (email)
                .orElseThrow (() -> new RuntimeException ("User not found"));

        if (user.getOtp () != null && user.getOtp ().equals (otp) && user.getOtpExpiry ().isAfter (LocalDateTime.now ())) {
            user.setEnabled (true);
            user.setOtp (null);
            user.setOtpExpiry (null);
            userRepository.save (user);
            return jwtUtils.generateToken (email, user.getRole ());
        }
        throw new RuntimeException ("Invalid or Expired OTP");
    }

    public JwtResponse loginUser(String email, String password) {
        User user = userRepository.findByEmail (email)
                .orElseThrow (() -> new RuntimeException ("User not found"));

        if (!user.isEnabled ()) {
            throw new RuntimeException ("Account not verified. Please verify OTP first.");
        }

        // Important Check: Agar status PENDING hai toh error message do
        if ("ROLE_OWNER".equals (user.getRole ()) && "PENDING".equals (user.getStatus ())) {
            throw new RuntimeException ("Your account is pending for Admin Approval. Please wait.");
        }

        if ("REJECTED".equals (user.getStatus ())) {
            throw new RuntimeException ("Your application was rejected by Admin. Please contact support.");
        }

        if (encoder.matches (password, user.getPassword ())) {
            String token = jwtUtils.generateToken (email, user.getRole ());
            return new JwtResponse (token, user.getRole (), user.getEmail ());
        } else {
            throw new RuntimeException ("Invalid Credentials");
        }
    }

    public ResponseEntity<?> forgotPasswordOtp(String email) {
        Optional<User> optionalUser = userRepository.findByEmail (email);

        if (optionalUser.isEmpty ()) {
            return ResponseEntity.badRequest ().body ("Email not registered");
        }

        User user = optionalUser.get ();

        // Generate OTP
        String otp = String.format ("%06d", new Random ().nextInt (999999));

        user.setOtp (otp);
        user.setOtpExpiry (LocalDateTime.now ().plusMinutes (5));

        userRepository.save (user);

        // Prepare email content
        String subject = "Password Reset OTP - Room Web App";

        String body = "Dear User,\n\n" +
                "Your OTP for resetting password is: " + otp + "\n" +
                "This OTP is valid for 5 minutes only.\n\n" +
                "If you didn't request this, please ignore this email.";

        // Send email using generic service
        emailService.sendOtpEmail (email, subject, body);
        return ResponseEntity.ok ("OTP_SENT");
    }


    public ResponseEntity<?> forgotVerifyOtp(String email, String otp) {

        Optional<User> optionalUser = userRepository.findByEmail (email);

        if (optionalUser.isEmpty ()) {
            return ResponseEntity.badRequest ().body ("Email not found");
        }

        User user = optionalUser.get ();

        // Check OTP match
        if (user.getOtp () == null || !user.getOtp ().equals (otp)) {
            return ResponseEntity.badRequest ().body ("Invalid OTP");
        }

        // Check expiry
        if (user.getOtpExpiry () == null || user.getOtpExpiry ().isBefore (LocalDateTime.now ())) {
            return ResponseEntity.badRequest ().body ("OTP Expired");
        }

        // OTP verified successfully
        user.setOtp (null);
        user.setOtpExpiry (null);

        userRepository.save (user);

        return ResponseEntity.ok ("OTP_VERIFIED");
    }

    public ResponseEntity<?> forgotPassword(String email, String password) {

        Optional<User> optionalUser = userRepository.findByEmail (email);

        if (optionalUser.isEmpty ()) {
            return ResponseEntity.badRequest ().body ("Email not found");
        }

        User user = optionalUser.get ();

        // Security check - ensure OTP process was completed
        if (user.getOtp () != null) {
            return ResponseEntity.badRequest ().body ("OTP verification pending");
        }

        // Encrypt password before saving
        user.setPassword (encoder.encode (password));
        userRepository.save (user);
        return ResponseEntity.ok ("Password reset successful");
    }

    public ResponseEntity<?> resetPassword(String email, String oldPassword, String newPassword) {

        System.out.println (email + " " + oldPassword + " " + newPassword);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        System.out.println (optionalUser);
        if (optionalUser.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = optionalUser.get();

        // Validate old password
        if (!encoder.matches(oldPassword, user.getPassword())) return ResponseEntity.badRequest().body("Old password is incorrect");

        // Set new password
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

}