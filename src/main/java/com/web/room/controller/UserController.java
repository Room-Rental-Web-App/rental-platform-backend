package com.web.room.controller;

import com.web.room.model.User;
import com.web.room.service.UserService;
import org.springframework.web.bind.annotation.*;

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
        return service.getRoomOwner(roomId, userEmail);
    }
}
