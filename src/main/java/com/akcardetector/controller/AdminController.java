package com.akcardetector.controller;

import com.akcardetector.repository.ClaimRepository;
import com.akcardetector.repository.UserRepository;
import com.akcardetector.repository.VehicleRepository;
import com.akcardetector.repository.DamageReportRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ClaimRepository claimRepository;
    private final DamageReportRepository damageReportRepository;

    public AdminController(
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            ClaimRepository claimRepository,
            DamageReportRepository damageReportRepository) {

        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.claimRepository = claimRepository;
        this.damageReportRepository = damageReportRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {

        Map<String, Long> stats = new HashMap<>();

        stats.put("users", userRepository.count());
        stats.put("vehicles", vehicleRepository.count());
        stats.put("reports", damageReportRepository.count());
        stats.put("claims", claimRepository.count());

        return ResponseEntity.ok(stats);
    }
}