package com.web.room.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * User ko login OTP bhejne ke liye method
     * @param to - User ki email id
     * @param otp - 6 digit generated OTP
     */
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("chanderisilkelegant@gmail.com"); // Aapka email address
            message.setTo(to);
            message.setSubject("Login OTP - Room Web App");
            message.setText("Dear User,\n\n" +
                    "Your OTP for logging into Room Web App is: " + otp + "\n" +
                    "This OTP is valid for 5 minutes only.\n\n" +
                    "If you didn't request this, please ignore this email.");

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email service is currently unavailable.");
        }
    }
        public void sendSimpleEmail(String to, String subject, String body) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("your-email@gmail.com"); // Matches your application.properties
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);

                mailSender.send(message);
                System.out.println("Email sent successfully to: " + to);
            } catch (Exception e) {
                System.err.println("Failed to send email: " + e.getMessage());
            }

    }
}