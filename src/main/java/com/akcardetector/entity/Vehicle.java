package com.akcardetector.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String vehicleNumber;

    private String brand;

    private String model;

    private Integer manufacturingYear;

    private String fuelType;

    // Vehicle verification
    @Column(name = "verification_status")
    private String verificationStatus = "PENDING";

    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    // Default constructor
    public Vehicle() {
    }

    // Get ID
    public Long getId() {
        return id;
    }

    // Get User ID
    public Long getUserId() {
        return userId;
    }

    // Set User ID
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Get Vehicle Number
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    // Set Vehicle Number
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    // Get Brand
    public String getBrand() {
        return brand;
    }

    // Set Brand
    public void setBrand(String brand) {
        this.brand = brand;
    }

    // Get Model
    public String getModel() {
        return model;
    }

    // Set Model
    public void setModel(String model) {
        this.model = model;
    }

    // Get Manufacturing Year
    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    // Set Manufacturing Year
    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    // Get Fuel Type
    public String getFuelType() {
        return fuelType;
    }

    // Set Fuel Type
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    // Get Verification Status
    public String getVerificationStatus() {
        return verificationStatus;
    }

    // Set Verification Status
    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    // Get Verified Time
    public java.time.LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    // Set Verified Time
    public void setVerifiedAt(java.time.LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}