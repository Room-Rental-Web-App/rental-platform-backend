package com.web.room.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/location")
public class ShowLocationSuggestions {

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String query) {
        System.out.println (query);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "RoomsDekho");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://nominatim.openstreetmap.org/search?format=json&q="
                + UriUtils.encode(query, StandardCharsets.UTF_8)
                + "&limit=10";

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}