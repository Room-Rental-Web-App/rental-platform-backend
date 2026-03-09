package com.web.room.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/api/location")
public class ShowLocationSuggestions {
    @Value("${geoapify.api.key}")
    private String apiKey;

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String query) {
        System.out.println (query);
        RestTemplate restTemplate = new RestTemplate();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "https://api.geoapify.com/v1/geocode/autocomplete"
                + "?text=" + encodedQuery
                + "&limit=5"
                + "&filter=countrycode:in"
                + "&apiKey=" + apiKey;

        String result =  restTemplate.getForObject(url,String.class);
        return ResponseEntity.ok (result);
    }
}

