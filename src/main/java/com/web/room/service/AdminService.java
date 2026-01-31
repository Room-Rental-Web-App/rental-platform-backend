package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Manual Autowired ki jagah ye clean hai
public class AdminService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;

    // --- User Methods ---
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
        return userRepository.findByRoleAndStatus("ROLE_OWNER", "PENDING");
    }

    public List<User> getPendingUsers() {
        return userRepository.findByRoleAndStatus("ROLE_USER", "PENDING");
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

    // --- Room Approval Logic ---

    /**
     * FIX: Updated method name to match new Repository
     */
    public List<Room> getPendingRooms() {
        return roomRepository.findByApprovedByAdminFalse();
    }

    /**
     * Approve or Reject a Room listing
     */
    public void updateRoomApprovalStatus(Long roomId, boolean approve) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (approve) {
            // FIX: Using new Boolean wrapper setter
            room.setApprovedByAdmin(true);
            roomRepository.save(room);

            String subject = "Room Listing Approved - " + room.getTitle();
            String body = "Good news! Your room listing '" + room.getTitle() + "' has been approved and is now live.";
            emailService.sendSimpleEmail(room.getOwnerEmail(), subject, body);
        } else {
            String subject = "Room Listing Update";
            String body = "Your room listing '" + room.getTitle() + "' was not approved. Please check details.";
            emailService.sendSimpleEmail(room.getOwnerEmail(), subject, body);
            // roomRepository.delete(room); // Optional: Delete if rejected
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));

        if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
            for (String url : room.getImageUrls()) {
                String publicId = extractPublicId(url);
                if (publicId != null) cloudinaryService.deleteFile(publicId, "image");
            }
        }

        if (room.getVideoUrl() != null) {
            String videoPublicId = extractPublicId(room.getVideoUrl());
            if (videoPublicId != null) cloudinaryService.deleteFile(videoPublicId, "video");
        }

        room.getImageUrls().clear();
        roomRepository.saveAndFlush(room);
        roomRepository.delete(room);
    }

    private String extractPublicId(String url) {
        if (url == null || !url.contains("/upload/")) return null;
        try {
            String part = url.split("/upload/")[1];
            String idWithExtension = part.substring(part.indexOf("/") + 1);
            return idWithExtension.substring(0, idWithExtension.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }

    public List<Room> getHighInterestRooms(int limit) {
        return roomRepository.findHighInterestRooms(limit);
    }

    @Transactional
    public void markRoomAsBooked(Long roomId, String adminEmail) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // FIX: Using new 'available' setter
        room.setAvailable(false);
        room.setStatusUpdatedBy(adminEmail);
        room.setPriorityScore(0);

        roomRepository.save(room);

        String subject = "Room Status Updated by Admin";
        String body = "Your room '" + room.getTitle() + "' has been marked as BOOKED by Admin: " + adminEmail;
        emailService.sendSimpleEmail(room.getOwnerEmail(), subject, body);
    }
}