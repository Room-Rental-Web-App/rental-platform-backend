package com.web.room.repository;

import com.web.room.model.ShareRoom;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShareRoomRepository extends JpaRepository<ShareRoom, Long> {
    List<ShareRoom> findByOwnerId(Long ownerId);

    List<ShareRoom> findByUserId(Long userId);
}
