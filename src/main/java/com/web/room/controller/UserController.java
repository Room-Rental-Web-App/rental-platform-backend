package com.web.room.controller;

import com.web.room.dto.Request.UserRequest;
import com.web.room.model.User;
import com.web.room.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/roomOwner/{roomId}/{userEmail}")
    public User getRoomOwner(@PathVariable Long roomId, @PathVariable String userEmail) {
        return service.getRoomOwner (roomId, userEmail);
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
