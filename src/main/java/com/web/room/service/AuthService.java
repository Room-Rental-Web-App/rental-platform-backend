package com.web.room.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.room.dto.Request.RegistrationRequest;
import com.web.room.dto.Response.JwtResponse;
import com.web.room.model.User;
import com.web.room.repository.UserRepository;
import com.web.room.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
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
        emailService.sendOtpEmail (req.getEmail (), otp);
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
}