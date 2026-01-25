package com.web.room.service;

import com.web.room.dto.AdminReportDTO;
import com.web.room.enums.ReportStatus;
import com.web.room.enums.ReportType;
import com.web.room.model.reports.RoomOwnerReport;
import com.web.room.model.reports.RoomReport;
import com.web.room.model.reports.UserReport;
import com.web.room.repository.reportsRepositories.RoomOwnerReportRepository;
import com.web.room.repository.reportsRepositories.RoomReportRepository;
import com.web.room.repository.reportsRepositories.UserReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReportService {

    private final RoomReportRepository roomReportRepo;
    private final RoomOwnerReportRepository roomOwnerReportRepo;
    private final UserReportRepository userReportRepo;

    public List<AdminReportDTO> getReports(ReportType type, ReportStatus status) {
        List<AdminReportDTO> result = new ArrayList<> ();
        if (type == ReportType.ROOM) {
            result.addAll (roomReportRepo.findByStatus (status).stream ().map (this::mapRoom).toList ());
        } else if (type == ReportType.ROOM_OWNER) {
            result.addAll (roomOwnerReportRepo.findByStatus (status).stream ().map (this::mapOwner).toList ());
        } else if (type == ReportType.USER) {
            result.addAll (userReportRepo.findByStatus (status).stream ().map (this::mapUser).toList ());
        }else {
            result.addAll (roomReportRepo.findAll ().stream ().map (this::mapRoom).toList ());
            result.addAll (roomOwnerReportRepo.findAll ().stream ().map (this::mapOwner).toList ());
            result.addAll (userReportRepo.findAll ().stream ().map (this::mapUser).toList ());
        }
        return result;
    }

    /* ---------- mappers ---------- */

    private AdminReportDTO mapRoom(RoomReport r) {
        return new AdminReportDTO (
                r.getId (),
                ReportType.ROOM,
                r.getReporter ().getEmail (),
                "Room: " + r.getRoom ().getTitle (),
                r.getReason (),
                r.getStatus (),
                r.getCreatedAt ()
        );
    }

    private AdminReportDTO mapOwner(RoomOwnerReport r) {
        return new AdminReportDTO (
                r.getId (),
                ReportType.ROOM_OWNER,
                r.getReporter ().getEmail (),
                "Owner: " + r.getReportedOwner ().getEmail (),
                r.getReason (),
                r.getStatus (),
                r.getCreatedAt ()
        );
    }

    private AdminReportDTO mapUser(UserReport r) {
        return new AdminReportDTO (
                r.getId (),
                ReportType.USER,
                r.getReporter ().getEmail (),
                "User: " + r.getReportedUser ().getEmail (),
                r.getReason (),
                r.getStatus (),
                r.getCreatedAt ()
        );
    }

    public void updateReportStatus(ReportType type, Long reportId, ReportStatus status) {
        switch (type) {
            case ROOM -> {
                RoomReport r = roomReportRepo.findById (reportId).orElseThrow (() -> new RuntimeException ("Report not found"));
                r.setStatus (status);
            }
            case ROOM_OWNER -> {
                RoomOwnerReport r = roomOwnerReportRepo.findById (reportId).orElseThrow (() -> new RuntimeException ("Report not found"));
                r.setStatus (status);
            }
            case USER -> {
                UserReport r = userReportRepo.findById (reportId).orElseThrow (() -> new RuntimeException ("Report not found"));
                r.setStatus (status);
            }
        }
    }
}
