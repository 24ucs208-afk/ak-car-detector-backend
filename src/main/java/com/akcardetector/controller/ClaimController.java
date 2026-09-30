package com.akcardetector.controller;

import com.akcardetector.entity.Claim;
import com.akcardetector.entity.Vehicle;
import com.akcardetector.repository.ClaimRepository;
import com.akcardetector.repository.VehicleRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ClaimController {

    private final ClaimRepository claimRepository;
    private final VehicleRepository vehicleRepository;

    private final String UPLOAD_DIR = "uploads/claims/";

    public ClaimController(
            ClaimRepository claimRepository,
            VehicleRepository vehicleRepository) {

        this.claimRepository = claimRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // ==========================================================
    // CREATE CLAIM
    // ==========================================================

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createClaim(

            @RequestParam Long userId,

            @RequestParam Long vehicleId,

            @RequestParam String insuranceCompany,

            @RequestParam String policyNumber,

            @RequestParam String vehicleNumber,

            @RequestParam String accidentDate,

            @RequestParam Double claimAmount,

            @RequestParam(required = false)
            String damageDescription,

            @RequestPart("damageImage")
            MultipartFile damageImage,

            @RequestPart("vehicleDocument")
            MultipartFile vehicleDocument

    ) {

        try {

            // ==================================================
            // CHECK VEHICLE OWNERSHIP
            // ==================================================

            if (!vehicleRepository.existsByIdAndUserId(
                    vehicleId,
                    userId)) {

                return ResponseEntity
                        .badRequest()
                        .body("This vehicle does not belong to the user");
            }

            // ==================================================
            // GET VEHICLE FROM DATABASE
            // ==================================================

            Vehicle vehicle = vehicleRepository
                    .findById(vehicleId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Vehicle not found"));

            // ==================================================
            // CREATE UPLOAD DIRECTORY
            // ==================================================

            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ==================================================
            // GENERATE CLAIM NUMBER
            // ==================================================

            String claimNumber =
                    "CLM-" + System.currentTimeMillis();

            // ==================================================
            // SAVE DAMAGE IMAGE
            // ==================================================

            String damageImageName =
                    claimNumber +
                    "_damage_" +
                    sanitizeFileName(
                            damageImage.getOriginalFilename()
                    );

            Path damageImagePath =
                    uploadPath.resolve(damageImageName);

            Files.copy(
                    damageImage.getInputStream(),
                    damageImagePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // ==================================================
            // SAVE VEHICLE DOCUMENT
            // ==================================================

            String vehicleDocumentName =
                    claimNumber +
                    "_vehicle_" +
                    sanitizeFileName(
                            vehicleDocument.getOriginalFilename()
                    );

            Path vehicleDocumentPath =
                    uploadPath.resolve(vehicleDocumentName);

            Files.copy(
                    vehicleDocument.getInputStream(),
                    vehicleDocumentPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // ==================================================
            // CREATE CLAIM ENTITY
            // ==================================================

            Claim claim = new Claim();

            claim.setUserId(userId);

            claim.setVehicleId(vehicleId);

            claim.setClaimNumber(claimNumber);

            claim.setInsuranceCompany(
                    insuranceCompany
            );

            claim.setPolicyNumber(
                    policyNumber
            );

            // IMPORTANT:
            // Vehicle number is taken from DATABASE,
            // not from user's typed value.

            claim.setVehicleNumber(
                    vehicle.getVehicleNumber()
            );

            claim.setAccidentDate(
                    LocalDate.parse(accidentDate)
            );

            claim.setClaimAmount(
                    claimAmount
            );

            claim.setDamageDescription(
                    damageDescription
            );

            claim.setDamageImagePath(
                    damageImagePath.toString()
            );

            claim.setVehicleDocumentPath(
                    vehicleDocumentPath.toString()
            );

            // ==================================================
            // SAVE CLAIM TO DATABASE
            // ==================================================

            Claim savedClaim =
                    claimRepository.save(claim);

            // ==================================================
            // RESPONSE
            // ==================================================

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Claim submitted successfully"
            );

            response.put(
                    "claim",
                    savedClaim
            );

            return ResponseEntity
                    .status(201)
                    .body(response);

        } catch (Exception e) {

            Map<String, String> error =
                    new HashMap<>();

            error.put(
                    "message",
                    "Failed to submit claim"
            );

            error.put(
                    "error",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }

    // ==========================================================
    // GET ALL CLAIMS - ADMIN
    // ==========================================================

    @GetMapping
    public ResponseEntity<?> getAllClaims() {

        return ResponseEntity.ok(
                claimRepository.findAll()
        );
    }

    // ==========================================================
    // GET CLAIMS BY USER
    // ==========================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getClaimsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                claimRepository.findByUserId(userId)
        );
    }

    // ==========================================================
    // GET SINGLE CLAIM
    // ==========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getClaim(
            @PathVariable Long id) {

        return claimRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }// ==========================================================
    // FILE NAME SECURITY
    // ==========================================================

    private String sanitizeFileName(
            String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            return "file";
        }

        return Paths
                .get(fileName)
                .getFileName()
                .toString()
                .replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );
    }
}