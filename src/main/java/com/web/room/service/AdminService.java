package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private EmailService emailService;

    // --- Existing User Methods ---
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<User> getAllOwners() {
        return userRepository.findAll().stream()
                .filter(user -> "ROLE_OWNER".equals(user.getRole()) && "APPROVED".equals(user.getStatus()))
                .collect(Collectors.toList());
    }

    public List<User> getPendingOwners() {
        return userRepository.findByStatus("PENDING");
    }

    public void updateOwnerStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);

        if ("APPROVED".equals(status)) {
            emailService.sendSimpleEmail(user.getEmail(), "Account Approved", "Your Owner account is approved!");
        } else if ("REJECTED".equals(status)) {
            emailService.sendSimpleEmail(user.getEmail(), "Account Rejected", "Your request was rejected.");
        }
    }

    // --- NEW: Room Approval Logic ---

    /**
     * Fetch all rooms that are waiting for Admin approval (isApprovedByAdmin = false)
     */
    public List<Room> getPendingRooms() {
        return roomRepository.findByIsApprovedByAdminFalse();
    }

    /**
     * Approve or Reject a Room listing
     */
    public void updateRoomApprovalStatus(Long roomId, boolean approve) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (approve) {
            room.setApprovedByAdmin(true);
            roomRepository.save(room);

            // Notify Owner via Email
            String subject = "Room Listing Approved - " + room.getTitle();
            String body = "Good news! Your room listing '" + room.getTitle() + "' has been approved and is now live for tenants to see.";
            emailService.sendSimpleEmail(room.getOwnerEmail(), subject, body);
        } else {
            // If rejected, you might want to delete it or keep it as rejected
            // For now, let's just delete the rejected request or notify them
            String subject = "Room Listing Update";
            String body = "Your room listing '" + room.getTitle() + "' was not approved. Please check the details and try again.";
            emailService.sendSimpleEmail(room.getOwnerEmail(), subject, body);

            // Optional: roomRepository.delete(room);
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    @Transactional

    public void deleteRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Step 1: Manually clear the images list (This deletes rows in room_images table)
        room.getImageUrls().clear();

        // Step 2: Flush the changes to the database
        roomRepository.saveAndFlush(room);

        // Step 3: Now delete the room safely
        roomRepository.delete(room);
    }
}