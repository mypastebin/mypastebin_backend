package com.hhnatsiuk.mypastebin_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HashGeneratorService {

    @Value("${hashgenerator.url}")
    private String hashGeneratorUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateUniqueHash() {
        return restTemplate.getForObject(hashGeneratorUrl, String.class);
    }
}
