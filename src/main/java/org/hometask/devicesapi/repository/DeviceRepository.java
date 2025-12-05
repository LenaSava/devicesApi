package org.hometask.devicesapi.repository;

import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    Page<DeviceEntity> findByBrand(String brand, Pageable pageable);
    Page<DeviceEntity> findByState(DeviceState state, Pageable pageable);
}
