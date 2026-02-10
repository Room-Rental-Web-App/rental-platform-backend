package com.web.room.controller;

import com.web.room.dto.Request.UserRequest;
import com.web.room.model.User;
import com.web.room.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/roomOwner/{roomId}/{userEmail}")
    public ResponseEntity<?> getRoomOwner(@PathVariable Long roomId, @PathVariable String userEmail) {
        try {
            User owner = service.getRoomOwner(roomId, userEmail);

            if (owner == null) {
                // Agar owner null hai (database mismatch), toh 404 bhejo
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Owner details not found for this room.");
            }

            return ResponseEntity.ok(owner);

        } catch (Exception e) {
            // Agar koi aur error aaye, toh 500 bhejo par message ke saath
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching owner: " + e.getMessage());
        }
    }

    @GetMapping("/allUsers")
    public List<User> getUsersByEmailAndRole(@RequestParam(required = false) String email, @RequestParam(required = false) String role) {
        System.out.println (role +" "+ email);
        return service.findUsersByRoleAndOptionalEmail (email, role);
    }

    @PatchMapping("/profile")
    public User updateUserProfile(@RequestBody UserRequest userRequest){
        System.out.println (userRequest.getId () + userRequest.getFullName ()+ userRequest.getPhone ());
        return  service.updateUserProfile(userRequest);
    }

}
