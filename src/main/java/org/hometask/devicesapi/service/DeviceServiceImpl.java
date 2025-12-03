package org.hometask.devicesapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hometask.devicesapi.dto.DeviceCreateRequest;
import org.hometask.devicesapi.dto.DeviceResponse;
import org.hometask.devicesapi.mapper.DeviceMapper;
import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Override
    public DeviceResponse createDevice(DeviceCreateRequest request) {
        log.info("Creating device: {}", request.getName());

        var device = DeviceEntity.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .state(DeviceState.AVAILABLE)
                .creationTime(LocalDateTime.now())
                .build();

        var savedDevice = deviceRepository.save(device);
        log.info("Device created with id: {}", savedDevice.getId());

        return deviceMapper.deviceToDeviseDTO(savedDevice);
    }
}
