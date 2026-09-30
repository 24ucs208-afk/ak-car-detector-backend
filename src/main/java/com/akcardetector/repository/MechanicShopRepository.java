package com.akcardetector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.akcardetector.entity.MechanicShop;

public interface MechanicShopRepository
        extends JpaRepository<MechanicShop, Long> {
}
