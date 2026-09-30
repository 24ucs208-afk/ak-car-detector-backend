package com.akcardetector.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_history")
public class VehicleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleNumber;

    private LocalDate serviceDate;

    private String service;

    private String mechanic;

    private double cost;

    public VehicleHistory() {
    }

    public VehicleHistory(String vehicleNumber,
                          LocalDate serviceDate,
                          String service,
                          String mechanic,
                          double cost) {
        this.vehicleNumber = vehicleNumber;
        this.serviceDate = serviceDate;
        this.service = service;
        this.mechanic = mechanic;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMechanic() {
        return mechanic;
    }

    public void setMechanic(String mechanic) {
        this.mechanic = mechanic;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}