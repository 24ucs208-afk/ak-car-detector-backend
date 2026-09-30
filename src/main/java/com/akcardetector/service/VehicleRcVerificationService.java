package com.akcardetector.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class VehicleRcVerificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL =
            "https://apisathi.in/gw/v1/vehicle-rc-v1/";

    @Value("${apisathi.api.key}")
    private String apiKey;

    public boolean verifyVehicle(String vehicleNumber) {

        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return false;
        }

        String cleanNumber = vehicleNumber
                .replaceAll("[\\s-]", "")
                .toUpperCase();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("rc_number", cleanNumber);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            API_URL,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            System.out.println(
                    "API SATHI RESPONSE: " + response.getBody()
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody() != null
                        && response.getBody().contains("\"verified\":true");
            }

        } catch (Exception e) {
            System.out.println(
                    "RC VERIFICATION ERROR: " + e.getMessage()
            );
        }

        return false;
    }
}