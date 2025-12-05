package org.hometask.devicesapi.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hometask.devicesapi.dto.*;
import org.hometask.devicesapi.exception.DeviceNotFoundException;
import org.hometask.devicesapi.mapper.DeviceMapper;
import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.repository.DeviceRepository;
import org.hometask.devicesapi.validation.DeviceValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final DeviceValidator deviceValidator;

    @Override
    @Transactional
    public DeviceDTO createDevice(DeviceCreateCommand command) {

        var device = deviceMapper.toEntity(command);
        var savedDevice = deviceRepository.save(device);
        log.info("Device created with id: {}", savedDevice.getId());

        return deviceMapper.toDTO(savedDevice);
    }

    @Override
    @Transactional
    public DeviceDTO updateDevice(Long id, DeviceUpdateCommand command) {
        log.info("Update for device id: {}", id);

        var device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        deviceValidator.validateUpdate(device, command);
        applyUpdates(device, command);

        var updatedDevice = deviceRepository.save(device);
        log.info("Device partially updated: {}", updatedDevice.getId());

        return deviceMapper.toDTO(updatedDevice);
    }

    @Override
    public DeviceDTO getDeviceById(Long id) {
        log.info("Fetching device with id: {}", id);

        DeviceEntity device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        return deviceMapper.toDTO(device);
    }

    @Override
    public Page<DeviceDTO> getDevicesByBrand(String brand, int page, int size, String sortBy) {
        log.info("Fetching devices by brand: {} - page: {}, size: {}", brand, page, size);
        Pageable pageable = createPageable(page, size, sortBy);
        Page<DeviceEntity> devicePage = deviceRepository.findByBrand(brand, pageable);

        return devicePage.map(deviceMapper::toDTO);
    }

    @Override
    public Page<DeviceDTO> getDevicesByState(DeviceState state, int page, int size, String sortBy) {
        log.info("Fetching devices by state: {} - page: {}, size: {}", state, page, size);

        Pageable pageable = createPageable(page, size, sortBy);
        Page<DeviceEntity> devicePage = deviceRepository.findByState(state, pageable);

        return devicePage.map(deviceMapper::toDTO);
    }


    @Transactional
    @Override
    public Page<DeviceDTO> getAllDevices(int page, int size, String sortBy) {
        log.info("Fetching all devices - page: {}, size: {}, sort: {}", page, size, sortBy);
        Pageable pageable = createPageable(page, size, sortBy);
        Page<DeviceEntity> devicePage = deviceRepository.findAll(pageable);

        return devicePage.map(deviceMapper::toDTO);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);
        DeviceEntity device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        deviceValidator.validateDelete(device);
        deviceRepository.delete(device);
    }

    private Pageable createPageable(int page, int size, String sortBy) {

        String defaultField = "id";

        if (sortBy == null || sortBy.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, defaultField));
        }

        String[] params = sortBy.split(",");

        String field = params.length > 0 ? params[0].trim() : defaultField;

        Sort.Direction direction =
                params.length > 1 && params[1].equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    private void applyUpdates(DeviceEntity device, DeviceUpdateCommand command) {
        if (command.getName() != null) {
            device.setName(command.getName());
        }
        if (command.getBrand() != null) {
            device.setBrand(command.getBrand());
        }
        if (command.getState() != null) {
            device.setState(command.getState());
        }
    }
}
