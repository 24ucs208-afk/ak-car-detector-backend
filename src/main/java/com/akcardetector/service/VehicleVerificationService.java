package com.akcardetector.service;

import org.springframework.stereotype.Service;

@Service
public class VehicleVerificationService {

    public boolean verifyVehicleNumber(String vehicleNumber) {

        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return false;
        }

        String cleanNumber = vehicleNumber
                .replaceAll("[\\s-]", "")
                .toUpperCase();

        // Indian vehicle registration number format
        String pattern = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{1,4}$";

        return cleanNumber.matches(pattern);
    }
}


