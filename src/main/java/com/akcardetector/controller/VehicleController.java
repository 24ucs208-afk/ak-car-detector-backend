package com.akcardetector.controller;

import com.akcardetector.entity.Vehicle;
import com.akcardetector.repository.VehicleRepository;
import com.akcardetector.service.VehicleRcVerificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleRcVerificationService rcVerificationService;

    public VehicleController(
            VehicleRepository vehicleRepository,
            VehicleRcVerificationService rcVerificationService) {

        this.vehicleRepository = vehicleRepository;
        this.rcVerificationService = rcVerificationService;
    }

    // Register Vehicle
    @PostMapping
    public ResponseEntity<?> saveVehicle(@RequestBody Vehicle vehicle) {

        if (vehicle.getVehicleNumber() == null ||
                vehicle.getVehicleNumber().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Vehicle number is required");
        }

        String vehicleNumber = vehicle.getVehicleNumber()
                .replaceAll("[\\s-]", "")
                .toUpperCase();

        vehicle.setVehicleNumber(vehicleNumber);

        // Check duplicate vehicle
        Optional<Vehicle> existingVehicle =
                vehicleRepository.findByVehicleNumber(vehicleNumber);

        if (existingVehicle.isPresent()) {

            return ResponseEntity.badRequest()
                    .body("Vehicle number already registered");
        }

        // RC Verification
        boolean verified =
                rcVerificationService.verifyVehicle(vehicleNumber);

        if (!verified) {

            return ResponseEntity.badRequest()
                    .body("Vehicle registration could not be verified");
        }

        // Vehicle verified
        vehicle.setVerificationStatus("VERIFIED");
        vehicle.setVerifiedAt(LocalDateTime.now());

        Vehicle savedVehicle =
                vehicleRepository.save(vehicle);

        return ResponseEntity.ok(savedVehicle);
    }

    @GetMapping
    public List<Vehicle> getVehicles() {
        return vehicleRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Vehicle> getUserVehicles(
            @PathVariable Long userId) {

        return vehicleRepository.findByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteVehicle(@PathVariable Long id) {
        vehicleRepository.deleteById(id);
    }

    @PutMapping("/{id}/verify")
    public Vehicle verifyVehicle(@PathVariable Long id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        vehicle.setVerificationStatus("VERIFIED");
        vehicle.setVerifiedAt(LocalDateTime.now());

        return vehicleRepository.save(vehicle);
    }
}