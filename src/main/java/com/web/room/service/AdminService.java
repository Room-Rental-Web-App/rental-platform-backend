package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private  EmailService emailService;

    // Fetch all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Fetch all rooms
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Fetch only owners who are already APPROVED
    public List<User> getAllOwners() {
        return userRepository.findAll().stream()
                .filter(user -> "ROLE_OWNER".equals(user.getRole()) && "APPROVED".equals(user.getStatus()))
                .collect(Collectors.toList());
    }

    // New: Fetch all Owners waiting for Approval
    public List<User> getPendingOwners() {
        return userRepository.findByStatus("PENDING");
    }

    // New: Approve or Reject an Owner
    public void updateOwnerStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ROLE_OWNER".equals(user.getRole())) {
            throw new RuntimeException("User is not an Owner");
        }

        user.setStatus(status); // APPROVED or REJECTED
        userRepository.save(user);
        // --- Logic to send Email Notification ---
        if ("APPROVED".equals(status)) {
            String subject = "Account Approved - RentalRoom";
            String message = "Congratulations! Your Owner account has been approved by the Admin. You can now log in and start adding your room listings.";
            emailService.sendSimpleEmail(user.getEmail(), subject, message);
        }
        else if ("REJECTED".equals(status)) {
            String subject = "Account Update - RentalRoom";
            String message = "We regret to inform you that your Owner registration request was not approved at this time. Please ensure your ID proof is clear and try again or contact support.";
            emailService.sendSimpleEmail(user.getEmail(), subject, message);
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}