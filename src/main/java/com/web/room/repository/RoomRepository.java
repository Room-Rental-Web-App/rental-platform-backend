package com.web.room.repository;

import com.web.room.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // Admin ke liye: Sirf un emails ki list nikalna jo owners hain
    @Query("SELECT DISTINCT r.ownerEmail FROM Room r")
    List<String> findDistinctOwnerEmails();

    @Query("""
        SELECT r FROM Room r
        WHERE r.isApprovedByAdmin = true
          AND r.isAvailable = true
          AND (:city IS NULL OR r.city = :city)
          AND (:pincode IS NULL OR r.pincode = :pincode)
          AND (:roomType IS NULL OR r.roomType = :roomType)
          AND (:minPrice IS NULL OR r.price >= :minPrice)
          AND (:maxPrice IS NULL OR r.price <= :maxPrice)
    """)
    Page<Room> filterRooms(String city,
                           String pincode,
                           String roomType,
                           Double minPrice,
                           Double maxPrice,
                           Pageable pageable);

}