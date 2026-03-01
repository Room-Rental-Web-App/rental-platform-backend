package com.web.room.controller;

import com.web.room.model.Room;
import com.web.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class SiteMapController {

    private final RoomRepository roomRepository;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    public ResponseEntity<String> generateSitemap() {

        List<Room> rooms = roomRepository.findByApprovedByAdminTrue();
        List<String> dbCities = roomRepository.findDistinctApprovedCities();

        String baseUrl = "https://roomsdekho.in";
        String today = LocalDate.now().toString();

        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        // ================= STATIC PAGES =================
        String[] staticPages = {
                "/", "/search", "/about", "/contact",
                "/privacy", "/terms", "/faq", "/premium"
        };

        for (String page : staticPages) {
            appendUrl(xml, baseUrl + page, today, "weekly", "0.8");
        }

        // ================= CITY PAGES =================
        for (String city : dbCities) {

            if (city == null || city.isBlank()) continue;

            String encodedCity = URLEncoder.encode(
                    city.toLowerCase().trim(),
                    StandardCharsets.UTF_8
            );

            appendUrl(xml,
                    baseUrl + "/quick-search/" + encodedCity,
                    today,
                    "daily",
                    "0.7");
        }

        // ================= ROOM TYPE PAGES =================
        String[] roomTypes = {
                "single-room", "flat", "1-bhk",
                "2-bhk", "3-bhk", "pg",
                "hostel", "villa", "studio-apartment"
        };

        for (String type : roomTypes) {
            appendUrl(xml,
                    baseUrl + "/quick-search/" + type,
                    today,
                    "weekly",
                    "0.6");
        }

        // ================= ROOM DETAIL PAGES =================
        for (Room room : rooms) {

            String lastModified = today;

            if (room.getUpdatedAt() != null) {
                lastModified = room.getUpdatedAt()
                        .toLocalDate()
                        .toString();
            } else if (room.getCreatedAt() != null) {
                lastModified = room.getCreatedAt()
                        .toLocalDate()
                        .toString();
            }

            appendUrl(xml,
                    baseUrl + "/room/" + room.getId(),
                    lastModified,
                    "daily",
                    "0.9");
        }

        xml.append("</urlset>");

        return ResponseEntity
                .ok()
                .header("Content-Type", "application/xml")
                .body(xml.toString());
    }

    // ================= SAFE HELPER METHOD =================
    private void appendUrl(StringBuilder xml,
                           String url,
                           String lastMod,
                           String changeFreq,
                           String priority) {

        xml.append("<url>")
                .append("<loc>").append(escapeXml(url)).append("</loc>")
                .append("<lastmod>").append(lastMod).append("</lastmod>")
                .append("<changefreq>").append(changeFreq).append("</changefreq>")
                .append("<priority>").append(priority).append("</priority>")
                .append("</url>");
    }

    // ================= XML ESCAPE METHOD =================
    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }


    @GetMapping(value = "/robots.txt", produces = "text/plain")
    public ResponseEntity<String> robots() {
        String robots = """
        User-agent: *
        Allow: /
        Sitemap: https://api.roomsdekho.in/sitemap.xml
        """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(robots);
    }
}