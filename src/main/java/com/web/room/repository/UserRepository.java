package com.web.room.repository;

import com.web.room.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(String status);
    @Query("""
             SELECT u  From User u 
               WHERE u.role = :role
               AND (u.status = :pending)
               AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
            """)
    List<User> findByRoleAndStatus(String email, String role, String pending);
    @Query("""
    SELECT u
    FROM User u
    WHERE u.role = :role
      AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
""")
    List<User> findUsersByRoleAndOptionalEmail(
            @Param("role") String role,
            @Param("email") String email
    );


    @Query("""
SELECT u
FROM User u
WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
""")
    List<User> findByEmailOrAll(@Param("email") String email);

}