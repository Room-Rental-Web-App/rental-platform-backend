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
@CrossOrigin(origins = "http://localhost:5173") // React ka port
public class AdminController {

    @Autowired
    private AdminService adminService;

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
    // New: Get all owners with PENDING status
    @GetMapping("/pending-owners")
    public ResponseEntity<List<User>> getPendingOwners() {
        return ResponseEntity.ok(adminService.getPendingOwners());
    }

    // New: Approve an owner (changes status to APPROVED)
    @PutMapping("/approve-owner/{id}")
    public ResponseEntity<String> approveOwner(@PathVariable Long id) {
        adminService.updateOwnerStatus(id, "APPROVED");
        return ResponseEntity.ok("Owner approved successfully");
    }

    // New: Reject an owner (changes status to REJECTED)
    @PutMapping("/reject-owner/{id}")
    public ResponseEntity<String> rejectOwner(@PathVariable Long id) {
        adminService.updateOwnerStatus(id, "REJECTED");
        return ResponseEntity.ok("Owner request rejected");
    }
}