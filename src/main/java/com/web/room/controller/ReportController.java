package com.web.room.controller;

import com.web.room.dto.Request.CreateReportRequest;
import com.web.room.enums.ReportStatus;
import com.web.room.enums.ReportType;
import com.web.room.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // USER → create any type of report
    @PostMapping
    public void createReport(@RequestParam Long reporterId, @RequestBody CreateReportRequest request) {
        reportService.createReport (reporterId, request);
    }

    // ADMIN → get all reports (grouped)
    @GetMapping
    public Map<String, List<?>> getAllReports() {
        return reportService.getAllReports ();
    }

    // ADMIN → update status
    @PatchMapping("/{type}/{id}/status")
    public void updateStatus( @PathVariable ReportType type, @PathVariable Long id, @RequestParam ReportStatus status ) {
        reportService.updateStatus (type, id, status);
    }
}
