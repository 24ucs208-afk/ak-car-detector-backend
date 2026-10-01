package com.akcardetector.service;

import org.springframework.stereotype.Service;

@Service
public class VehicleRcVerificationService {

    public boolean verifyVehicle(String vehicleNumber) {

        if (vehicleNumber == null ||
                vehicleNumber.trim().isEmpty()) {

            return false;
        }

        String cleanNumber = vehicleNumber
                .replaceAll("[\\s-]", "")
                .toUpperCase();

        // Basic Indian vehicle registration format validation
        return cleanNumber.matches(
                "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{1,4}$"
        );
    }
}