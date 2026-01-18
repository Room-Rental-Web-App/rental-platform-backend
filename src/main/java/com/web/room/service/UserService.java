package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    public User getRoomOwner(Long roomId, String userEmail) {
        System.out.println (" roomId: " + roomId + " userEmail: " + userEmail);
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return userRepo.findByEmail(room.getOwnerEmail())
                .orElseThrow(() -> new RuntimeException("Owner not found"));
    }

    public int getRoomCount(String ownerEmail) {
        return roomRepo.countByOwnerEmail(ownerEmail);
    }
}
