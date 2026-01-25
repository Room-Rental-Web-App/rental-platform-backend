package com.web.room.dto;

import com.web.room.enums.ReportStatus;
import com.web.room.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminReportDTO {

    private Long reportId;
    private ReportType reportType;

    private String reporterEmail;
    private String targetInfo;   // room title / user email / owner email

    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
