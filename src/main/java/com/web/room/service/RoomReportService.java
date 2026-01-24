package com.web.room.service;

import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.model.reports.RoomReport;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import com.web.room.repository.reportsRepositories.RoomReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomReportService {

    private final RoomReportRepository repo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    public void reportRoom(Long reporterId, Long roomId, String reason) {

        if (reason == null || reason.length() < 10)
            throw new IllegalArgumentException("Reason too short");

        User reporter = userRepo.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomReport report = new RoomReport();
        report.setReporter(reporter);
        report.setRoom(room);
        report.setReason(reason);

        repo.save(report);
    }
}
