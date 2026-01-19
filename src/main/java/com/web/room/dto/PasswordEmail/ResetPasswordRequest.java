package com.web.room.dto.PasswordEmail;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String password;
}
