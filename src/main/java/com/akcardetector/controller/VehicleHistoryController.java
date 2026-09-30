package com.akcardetector.controller;

import com.akcardetector.entity.VehicleHistory;
import com.akcardetector.repository.VehicleHistoryRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-history")
@CrossOrigin(origins = "*")
public class VehicleHistoryController {

    private final VehicleHistoryRepository repository;

    public VehicleHistoryController(VehicleHistoryRepository repository) {
        this.repository = repository;
    }

    // Get all vehicle history
    @GetMapping
    public List<VehicleHistory> getAllHistory() {
        return repository.findAll();
    }

    // Get history for a particular vehicle
    @GetMapping("/vehicle/{vehicleNumber}")
    public List<VehicleHistory> getVehicleHistory(
            @PathVariable String vehicleNumber) {

        return repository.findByVehicleNumber(vehicleNumber);
    }

    // Add new service history
    @PostMapping
    public VehicleHistory addHistory(
            @RequestBody VehicleHistory history) {

        return repository.save(history);
    }

    // Delete history
    @DeleteMapping("/{id}")
    public String deleteHistory(@PathVariable Long id) {

        repository.deleteById(id);

        return "Vehicle history deleted successfully";
    }
}