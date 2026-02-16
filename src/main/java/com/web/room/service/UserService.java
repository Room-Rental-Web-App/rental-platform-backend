package com.web.room.service;

import com.web.room.dto.Request.UserRequest;
import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.io.NotActiveException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    public User getRoomOwner(Long roomId, String userEmail) {
        System.out.println("Fetching owner for roomId: " + roomId);

        // 1. Room dhundo
        Room room = roomRepo.findById(roomId).orElse(null);
        if (room == null) {
            System.out.println("Room not found with ID: " + roomId);
            return null;
        }

        // 2. Owner dhundo (Typos handle karne ke liye trim aur lowercase use karein)
        String ownerEmail = room.getOwnerEmail().trim().toLowerCase();

        return userRepo.findByEmail(ownerEmail)
                .orElseGet(() -> {
                    System.out.println("Owner not found in DB with email: " + ownerEmail);
                    return null; // Crash ki jagah null return karega
                });
    }

    public long getRoomCount(String ownerEmail) {
        return roomRepo.countByOwnerEmail (ownerEmail);
    }

//    public List<User> getAllUsers() {
//        return userRepo.findAll ();
//    }
//
//    public List<User> findUsersByRoleAndOptionalEmail(String email, String role) {
//        return userRepo.findUsersByRoleAndOptionalEmail (role, email);
//    }

    public User updateUserProfile(UserRequest userRequest) {
        User user = userRepo.findById(userRequest.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userRequest.getId()));
        user.setFullName (userRequest.getFullName ());
        user.setPhone (userRequest.getPhone ());
         return  userRepo.save (user);
    }
}
