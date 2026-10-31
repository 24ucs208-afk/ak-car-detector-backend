package com.akcardetector.controller;

import com.akcardetector.entity.DamageReport;
import com.akcardetector.repository.DamageReportRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/damage")
@CrossOrigin
public class DamageDetectionController {

    private final DamageReportRepository damageReportRepository;

    public DamageDetectionController(
            DamageReportRepository damageReportRepository) {

        this.damageReportRepository = damageReportRepository;
    }

    @PostMapping("/detect")
    public ResponseEntity<String> detectDamage(

            @RequestParam("image") MultipartFile image,

            @RequestParam("userId") Long userId,

            @RequestParam("vehicleId") Long vehicleId) {

        Path tempImage = null;

        try {

            // 1. Check image
            if (image == null || image.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Image is required");
            }

            // 2. Create temporary image
            tempImage = Files.createTempFile(
                    "car_",
                    ".jpg"
            );

            image.transferTo(tempImage.toFile());

            // 3. Python
            String python = "/opt/venv/bin/python";

            String detectScript =
                    "/app/detect.py";

            // 4. Run YOLO
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            python,
                            detectScript,
                            tempImage.toString()
                    );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 5. Read Python output
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream()
                            )
                    );

            StringBuilder output =
                    new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();

            // 6. Delete temporary image
            Files.deleteIfExists(tempImage);
            tempImage = null;

            // 7. YOLO error
            if (exitCode != 0) {

                return ResponseEntity
                        .internalServerError()
                        .body("YOLO error: " + output);
            }

            // 8. Get YOLO JSON
            String jsonOutput = output.toString().trim();

            // 9. Save detection result
            saveDamageReport(
                    jsonOutput,
                    userId,
                    vehicleId
            );

            // 10. Return result to Flutter
            return ResponseEntity.ok(jsonOutput);

        } catch (Exception e) {

            try {

                if (tempImage != null) {
                    Files.deleteIfExists(tempImage);
                }

            } catch (Exception ignored) {
            }

            return ResponseEntity
                    .internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    private void saveDamageReport(
            String jsonOutput,
            Long userId,
            Long vehicleId) {

        try {

            // Example:
            // [{"damage":"dent","confidence":0.87}]

            if (jsonOutput == null ||
                    jsonOutput.isEmpty() ||
                    jsonOutput.equals("[]")) {

                return;
            }

            // Remove [ and ]
            String data = jsonOutput
                    .replace("[", "")
                    .replace("]", "")
                    .trim();

            if (data.isEmpty()) {
                return;
            }

            // Multiple detections
            String[] detections = data.split("\\},\\s*\\{");

            for (String detection : detections) {

                String clean = detection
                        .replace("{", "")
                        .replace("}", "")
                        .trim();

                String damage = null;
                Double confidence = null;

                String[] fields = clean.split(",");

                for (String field : fields) {

                    String[] keyValue =
                            field.split(":", 2);

                    if (keyValue.length != 2) {
                        continue;
                    }

                    String key =
                            keyValue[0]
                                    .replace("\"", "")
                                    .trim();

                    String value =
                            keyValue[1]
                                    .replace("\"", "")
                                    .trim();

                    if (key.equals("damage")) {
                        damage = value;
                    }

                    if (key.equals("confidence")) {

                        try {
                            confidence =
                                    Double.parseDouble(value);
                        } catch (Exception ignored) {
                        }
                    }
                }

                DamageReport report =
                        new DamageReport();

                report.setUserId(userId);
                report.setVehicleId(vehicleId);
                report.setDamage(damage);
                report.setConfidence(confidence);

                damageReportRepository.save(report);
            }

        } catch (Exception ignored) {
            // Detection result should still be returned
            // even if database parsing fails.
        }
    }
}