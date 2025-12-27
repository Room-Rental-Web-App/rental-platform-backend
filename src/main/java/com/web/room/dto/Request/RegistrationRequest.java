package com.web.room.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String email;
    private String password;
    private String role;

    private String phone;
    private MultipartFile aadharCard;
}