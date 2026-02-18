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
public class
AdminController {

    @Autowired
    private AdminService adminService;

    // --- User & Owner Management ---

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String email) {
        return ResponseEntity.ok(adminService.getAllUsers(email));
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


    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingRoles(@RequestParam(required = false) String role, @RequestParam(required = false) String email) {
        return ResponseEntity.ok (adminService.getPendingRole (role, email));
    }
    @GetMapping("/pending-users")
    public ResponseEntity<List<User>> getPendingUsers(
            @RequestParam(required = false) String email) {

        return ResponseEntity.ok(
                adminService.getPendingRole("ROLE_USER", email)
        );
    }

    @GetMapping("/pending-owners")
    public ResponseEntity<List<User>> getPendingOwners(
            @RequestParam(required = false) String email) {

        return ResponseEntity.ok(
                adminService.getPendingRole("ROLE_OWNER", email)
        );
    }

    @GetMapping("/allUsers")
    public List<User> getUsersByEmailAndRole(@RequestParam(required = false) String email, @RequestParam(required = false) String role) {
        System.out.println (role);
        return adminService.findUsersByRoleAndOptionalEmail (email, role);
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
       return adminService.updateRoomApprovalStatus(id, true);
    }

    @PutMapping("/reject-room/{id}")
    public ResponseEntity<String> rejectRoom(@PathVariable Long id) {
        return adminService.updateRoomApprovalStatus(id, false);
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        adminService.deleteRoomById(id); // We need to add this in AdminService
        return ResponseEntity.ok("Room deleted successfully");
    }
    @GetMapping("/high-interest-rooms")
    public ResponseEntity<List<Room>> getHighInterestRooms(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(adminService.getHighInterestRooms(limit));
    }
    @PatchMapping("/mark-booked/{id}")
    public ResponseEntity<String> markAsBooked(
            @PathVariable Long id,
            @RequestParam String adminEmail) {
        adminService.markRoomAsBooked(id, adminEmail);
        return ResponseEntity.ok("Room marked as Booked and Owner notified.");
    }
}