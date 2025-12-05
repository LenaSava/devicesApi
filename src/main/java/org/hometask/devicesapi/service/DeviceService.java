package org.hometask.devicesapi.service;

import org.hometask.devicesapi.dto.*;
import org.hometask.devicesapi.model.DeviceState;
import org.springframework.data.domain.Page;

public interface DeviceService {

    DeviceDTO createDevice(DeviceCreateCommand request);
    DeviceDTO updateDevice(Long id, DeviceUpdateCommand request);
    DeviceDTO getDeviceById(Long id);
    Page<DeviceDTO> getDevicesByBrand(String brand, int page, int size, String sortBy);
    Page<DeviceDTO> getDevicesByState(DeviceState state, int page, int size, String sortBy);
    Page<DeviceDTO> getAllDevices(int page, int size, String sortBy);
    void deleteDevice(Long id);
}
