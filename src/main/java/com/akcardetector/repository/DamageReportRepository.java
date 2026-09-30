package com.akcardetector.repository;

import com.akcardetector.entity.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageReportRepository
        extends JpaRepository<DamageReport, Long> {

    List<DamageReport> findByUserId(Long userId);

    List<DamageReport> findByVehicleId(Long vehicleId);
}