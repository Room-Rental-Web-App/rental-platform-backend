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

    // 1. For Tenants: Show only approved rooms
    List<Room> findByIsApprovedByAdminTrue();

    // 2. For Admin: Show rooms waiting for approval
    List<Room> findByIsApprovedByAdminFalse();

    // 3. For Owners: See their own rooms (Approved + Pending)
    List<Room> findByOwnerEmail(String email);

    // Existing methods...
    List<Room> findByPincode(String pincode);

    @Query("SELECT DISTINCT r.ownerEmail FROM Room r")
    List<String> findDistinctOwnerEmails();

    @Query("""
                SELECT r FROM Room r
                WHERE r.isApprovedByAdmin = true
                  AND r.isAvailable = true
                  AND (:city IS NULL OR LOWER(r.city) = LOWER(:city))
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

    @Query("""
            SELECT r FROM Room r WHERE r.isApprovedByAdmin = true AND r.isAvailable = true AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL AND (:city IS NULL OR LOWER(r.city) = LOWER(:city)) AND (:pincode IS NULL OR r.pincode = :pincode) AND (:roomType IS NULL OR r.roomType = :roomType) AND (:minPrice IS NULL OR r.price >= :minPrice) AND (:maxPrice IS NULL OR r.price <= :maxPrice) AND ( :userLat IS NULL OR (6371 * 2 * ASIN(SQRT( POWER(SIN(RADIANS(r.latitude - :userLat) / 2), 2) + COS(RADIANS(:userLat)) * COS(RADIANS(r.latitude)) * POWER(SIN(RADIANS(r.longitude - :userLng) / 2), 2) ))) <= :radiusKm ) ORDER BY r.priorityScore DESC, r.createdAt DESC """)
    Page<Room> filterRoomsWithRadius(String city, String pincode, String roomType, Double minPrice, Double maxPrice, Double userLat, Double userLng, Double radiusKm, Pageable pageable);

    int countByOwnerEmail(String ownerEmail);

}