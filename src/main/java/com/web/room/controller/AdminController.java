package com.web.room.controller;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // --- User & Owner Management ---

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(adminService.getAllRooms());
    }

    @GetMapping("/owners")
    public ResponseEntity<List<User>> getAllOwners() {
        return ResponseEntity.ok(adminService.getAllOwners());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/pending-owners")
    public ResponseEntity<List<User>> getPendingOwners() {
        return ResponseEntity.ok(adminService.getPendingOwners());
    }
    @GetMapping("/pending-users")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(adminService.getPendingUsers());
    }


    @PutMapping("/approve-owner/{id}")
    public ResponseEntity<String> approveOwner(@PathVariable Long id) {
        adminService.updateOwnerStatus(id, "APPROVED");
        return ResponseEntity.ok("Owner approved successfully");
    }

    @PutMapping("/reject-owner/{id}")
    public ResponseEntity<String> rejectOwner(@PathVariable Long id) {
        adminService.updateOwnerStatus(id, "REJECTED");
        return ResponseEntity.ok("Owner request rejected");
    }

    // --- NEW: Room Approval Management ---

    /**
     * Fetch all rooms currently waiting for admin verification.
     */
    @GetMapping("/pending-rooms")
    public ResponseEntity<List<Room>> getPendingRooms() {
        return ResponseEntity.ok(adminService.getPendingRooms());
    }

    /**
     * Approve a room listing to make it public.
     */
    @PutMapping("/approve-room/{id}")
    public ResponseEntity<String> approveRoom(@PathVariable Long id) {
        adminService.updateRoomApprovalStatus(id, true);
        return ResponseEntity.ok("Room listing approved and is now live!");
    }

    /**
     * Reject a room listing.
     */
    @PutMapping("/reject-room/{id}")
    public ResponseEntity<String> rejectRoom(@PathVariable Long id) {
        adminService.updateRoomApprovalStatus(id, false);
        return ResponseEntity.ok("Room listing has been rejected.");
    }
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        adminService.deleteRoomById(id); // We need to add this in AdminService
        return ResponseEntity.ok("Room deleted successfully");
    }
}