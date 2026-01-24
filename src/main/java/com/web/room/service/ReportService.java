package com.web.room.service;

import com.web.room.dto.Request.CreateReportRequest;
import com.web.room.enums.ReportStatus;
import com.web.room.enums.ReportType;
import com.web.room.model.Room;
import com.web.room.model.User;
import com.web.room.model.reports.RoomOwnerReport;
import com.web.room.model.reports.RoomReport;
import com.web.room.model.reports.UserReport;
import com.web.room.repository.RoomRepository;
import com.web.room.repository.UserRepository;
import com.web.room.repository.reportsRepositories.RoomOwnerReportRepository;
import com.web.room.repository.reportsRepositories.RoomReportRepository;
import com.web.room.repository.reportsRepositories.UserReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepo;
    private final RoomRepository roomRepo;

    private final RoomReportRepository roomReportRepo;
    private final RoomOwnerReportRepository ownerReportRepo;
    private final UserReportRepository userReportRepo;

    public void createReport(Long reporterId, CreateReportRequest req) {

        if (req.getReason() == null || req.getReason().length() < 10)
            throw new IllegalArgumentException("Reason too short");

        User reporter = userRepo.findById(reporterId).orElseThrow(() -> new RuntimeException("Reporter not found"));

        switch (req.getReportType()) {

            case ROOM -> {
                Room room = roomRepo.findById(req.getTargetId()).orElseThrow(() -> new RuntimeException("Room not found"));
                RoomReport report = new RoomReport();
                report.setReporter(reporter);
                report.setRoom(room);
                report.setReason(req.getReason());
                roomReportRepo.save(report);
            }

            case ROOM_OWNER -> {

                User owner = userRepo.findById(req.getTargetId()).orElseThrow(() -> new RuntimeException("Owner not found"));
                RoomOwnerReport report = new RoomOwnerReport();
                report.setReporter(reporter);
                report.setReportedOwner(owner);
                report.setReason(req.getReason());
                ownerReportRepo.save(report);
            }

            case USER -> {
                User user = userRepo.findById(req.getTargetId()).orElseThrow(() -> new RuntimeException("User not found"));
                UserReport report = new UserReport();
                report.setReporter(reporter);
                report.setReportedUser(user);
                report.setReason(req.getReason());
                userReportRepo.save(report);
            }
        }
    }

    /* ---------- ADMIN APIs ---------- */

    public Map<String, List<?>> getAllReports() {
        Map<String, List<?>> data = new HashMap<> ();
        data.put("roomReports", roomReportRepo.findAll());
        data.put("roomOwnerReports", ownerReportRepo.findAll());
        data.put("userReports", userReportRepo.findAll());
        return data;
    }

    public void updateStatus(ReportType type, Long id, ReportStatus status) {

        switch (type) {
            case ROOM -> {
                RoomReport r = roomReportRepo.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
                r.setStatus(status);
            }
            case ROOM_OWNER -> {
                RoomOwnerReport r = ownerReportRepo.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
                r.setStatus(status);
            }
            case USER -> {
                UserReport r = userReportRepo.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
                r.setStatus(status);
            }
        }
    }
}
