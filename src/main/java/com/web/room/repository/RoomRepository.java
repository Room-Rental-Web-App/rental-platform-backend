package com.web.room.repository;

import com.web.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find rooms by specific pincode for location-based search
    List<Room> findByPincode(String pincode);

    // Fetch all rooms added by a specific owner
    List<Room> findByOwnerEmail(String email);

    // Fetch only rooms that are verified and approved by Admin
    List<Room> findByIsApprovedByAdminTrue();
}