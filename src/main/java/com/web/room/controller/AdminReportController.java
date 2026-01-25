package com.web.room.controller;

import com.web.room.dto.AdminReportDTO;
import com.web.room.enums.ReportStatus;
import com.web.room.enums.ReportType;
import com.web.room.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public List<AdminReportDTO> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportStatus status
    ) {
        return adminReportService.getReports(type, status);
    }

    @PatchMapping("/{type}/{id}/status")
    public void updateStatus(
            @PathVariable ReportType type,
            @PathVariable Long id,
            @RequestParam ReportStatus status
    ) {
        adminReportService.updateReportStatus(type, id, status);
    }
}
