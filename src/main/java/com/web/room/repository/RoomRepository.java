package com.web.room.repository;

import com.web.room.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // 1. For Tenants: Show only approved rooms
    List<Room> findByApprovedByAdminTrue();

    // 2. For Admin: Show rooms waiting for approval
    List<Room> findByApprovedByAdminFalse();

    // 3. For Owners: See their own rooms
    List<Room> findByOwnerEmail(String email);



    @Query("""
            SELECT r
            FROM Room r
            WHERE (:approved IS NULL OR r.approvedByAdmin = :approved)
            
              AND r.latitude IS NOT NULL
              AND r.longitude IS NOT NULL
            
              /* Structured filters */
              AND (:city IS NULL OR LOWER(r.city) = LOWER(:city))
              AND (:pincode IS NULL OR r.pincode = :pincode)
              AND (:roomType IS NULL OR r.roomType = :roomType)
              AND (:minPrice IS NULL OR r.price >= :minPrice)
              AND (:maxPrice IS NULL OR r.price <= :maxPrice)
            
              /* Keyword search */
              AND (
                  :keyword IS NULL OR
                  LOWER(r.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR r.pincode LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(r.roomType) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                  )
              /* Radius filter */
              AND (
                  :userLat IS NULL OR (
                      6371 * 2 * ASIN(
                          SQRT(
                              POWER(SIN(RADIANS(r.latitude - :userLat) / 2), 2)
                              + COS(RADIANS(:userLat))
                              * COS(RADIANS(r.latitude))
                              * POWER(SIN(RADIANS(r.longitude - :userLng) / 2), 2)
                          )
                      )
                  ) <= :radiusKm
              )
            ORDER BY r.priorityScore DESC,
                     r.createdAt DESC
            """)
    Page<Room> searchAndFilterRooms(
            @Param ("approved") boolean approved,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("pincode") String pincode,
            @Param("roomType") String roomType,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("radiusKm") Double radiusKm,
            Pageable pageable
    );


    long countByOwnerEmail(String ownerEmail);

    @Query("SELECT DISTINCT r.city FROM Room r WHERE r.city IS NOT NULL")
    List<String> findAllCities();

    List<Room> findTop6ByOrderByIdDesc();

    // FIX: available field name updated in JPQL
    @Query("SELECT r FROM Room r WHERE r.contactViewCount >= :limit AND r.available = true")
    List<Room> findHighInterestRooms(@Param("limit") Integer limit);

    long countByOwnerEmailAndSubscriptionIdIsNull(String email);

    long countByOwnerEmailAndSubscriptionId(String email, Long id);

    @Query("SELECT DISTINCT LOWER(r.city) FROM Room r WHERE r.approvedByAdmin = true")
    List<String> findDistinctApprovedCities();
}