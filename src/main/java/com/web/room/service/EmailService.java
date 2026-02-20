package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.RoomAvailabilityRequest;
import com.web.room.model.User;
import com.web.room.repository.RoomAvailabilityRequestRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RoomAvailabilityRequestRepository requestRepository;

    // --- EXISTING METHODS (No changes here) ---

    public String sendOtpEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("roomdekhobharat@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("OTP Email sent successfully to: " + to);
            return "OTP Sent";
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email service is currently unavailable.");
        }
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("roomdekhobharat@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Simple Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void notifyWaitingUsers(Room room) {
        List<RoomAvailabilityRequest> requests = requestRepository.findByRoomIdAndNotifiedFalse(room.getId());
        for (RoomAvailabilityRequest req : requests) {
            User user = req.getUser();
            String subject = "Room Available: " + room.getTitle();
            String body = "Hello " + user.getFullName() + ",\n\n" + "Good news! The room you requested is now available.\n\n" + "Room Details:\n" + " Room-id: " + room.getId() + "\n" + "Title: " + room.getTitle() + "\n" + "Address: " + room.getAddress() + ", " + room.getCity() + "\n" + "Price: ₹" + room.getPrice() + "\n\n" + "Please book it as soon as possible.\n\n" + "Regards,\n" + "Room Management Team";
            sendSimpleEmail(user.getEmail(), subject, body);
            req.setNotified(true);
        }
        requestRepository.saveAll(requests);
    }

    // --- NEW METHOD FOR INVOICE & EXPIRY (MimeMessage required) ---

    /**
     * Premium Invoice bhejone ke liye ya Expiry notification ke liye
     */
    public void sendEmailWithInvoice(String to, String subject, String body, byte[] pdfContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true flag ka matlab hai multipart message (attachment ke liye)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("roomdekhobharat@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            // Agar PDF data hai toh attachment add karo, varna simple mail jayega
            if (pdfContent != null && pdfContent.length > 0) {
                helper.addAttachment("RoomsDekho_Invoice.pdf", new ByteArrayResource(pdfContent));
            }

            mailSender.send(message);
            System.out.println("Invoice/Premium Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send Mime email: " + e.getMessage());
        }
    }
}