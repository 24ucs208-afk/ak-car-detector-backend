package com.akcardetector.controller;

import com.akcardetector.entity.DamageReport;
import com.akcardetector.repository.DamageReportRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/damage/reports")
@CrossOrigin
public class DamageReportController {

    private final DamageReportRepository damageReportRepository;

    public DamageReportController(
            DamageReportRepository damageReportRepository) {
        this.damageReportRepository = damageReportRepository;
    }

    // =========================
    // GET ALL DAMAGE REPORTS
    // =========================

    @GetMapping
    public List<DamageReport> getAllReports() {
        return damageReportRepository.findAll();
    }

    // =========================
    // GET REPORTS BY USER
    // =========================

    @GetMapping("/user/{userId}")
    public List<DamageReport> getUserReports(
            @PathVariable Long userId) {

        return damageReportRepository.findByUserId(userId);
    }

    // =========================
    // GET REPORTS BY VEHICLE
    // =========================

    @GetMapping("/vehicle/{vehicleId}")
    public List<DamageReport> getVehicleReports(
            @PathVariable Long vehicleId) {

        return damageReportRepository.findByVehicleId(vehicleId);
    }

    // =========================
    // SAVE DAMAGE REPORT
    // =========================

    @PostMapping
    public ResponseEntity<DamageReport> saveReport(
            @RequestBody DamageReport report) {

        DamageReport savedReport =
                damageReportRepository.save(report);

        return ResponseEntity.ok(savedReport);
    }

    // =========================
    // DELETE DAMAGE REPORT
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(
            @PathVariable Long id) {

        if (!damageReportRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        damageReportRepository.deleteById(id);

        return ResponseEntity.ok(
                "Damage report deleted successfully"
        );
    }
}