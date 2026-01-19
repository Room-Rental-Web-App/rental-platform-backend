package com.web.room.dto.PasswordEmail;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String otp;
}
