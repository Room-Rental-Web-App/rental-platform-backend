package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<User> getAllOwners() {
        // Sirf ROLE_OWNER wale users ko return karega
        return userRepository.findAll().stream()
                .filter(user -> "ROLE_OWNER".equals(user.getRole()))
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}