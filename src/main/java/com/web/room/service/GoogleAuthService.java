package com.web.room.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.web.room.dto.Request.RegistrationRequest;
import com.web.room.dto.Response.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.web.room.model.User;
import com.web.room.repository.UserRepository;
import com.web.room.security.JwtUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String googleClientId;
    @Autowired
    private Cloudinary cloudinary;


    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;

    public JwtResponse loginWithGoogle(String googleToken) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                .Builder (new NetHttpTransport (), new JacksonFactory ())
                .setAudience (Collections.singletonList (googleClientId))
                .build ();

        GoogleIdToken idToken = verifier.verify (googleToken);
        if (idToken == null) {
            throw new RuntimeException ("Invalid Google token");
        }


        Payload payload = idToken.getPayload ();
        String email = payload.getEmail ();

        User user = userRepo.findByEmail (email).orElseGet (() -> {
            User u = new User ();
            u.setEmail (email);
            u.setPassword ("");
            u.setEnabled (true);
            u.setStatus ("PENDING");
            return userRepo.save (u);
        });

        String jwtToken = jwtUtils.generateToken (user.getEmail (), user.getRole ());
        JwtResponse response = new JwtResponse (jwtToken,user.getRole (),user.getEmail (), user.getId (), user.getFullName (),user.getPhone ());
        return response;
    }

    public JwtResponse completeRegistrationAndLogin(RegistrationRequest req) {
        User user = userRepo.findByEmail (req.getEmail ()).orElseThrow (() -> new RuntimeException ("User not found"));

        if (req.getAadharCard () != null && !req.getAadharCard ().isEmpty ()) {
            try {
                // Uploading to Cloudinary
                Map uploadResult = cloudinary.uploader ().upload (req.getAadharCard ().getBytes (),
                        ObjectUtils.asMap ("folder", "aadhar_cards"));

                String secureUrl = (String) uploadResult.get ("secure_url");
                user.setRole (req.getRole ());
                user.setPhone (req.getPhone ());
                user.setAadharUrl (secureUrl);
                userRepo.save (user);
                return new JwtResponse (jwtUtils.generateToken (user.getEmail (), user.getRole ()),user.getRole (),user.getEmail (), user.getId (), user.getFullName (),user.getPhone ());
            } catch (IOException e) {
                throw new RuntimeException ("Image upload failed: " + e.getMessage ());
            }
        } else {
            throw new RuntimeException ("Aadhar Card photo is required for Registration");
        }


    }
}
