package org.hometask.devicesapi.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.hometask.devicesapi.dto.DeviceCreateRequest;
import org.hometask.devicesapi.dto.DeviceResponse;
import org.hometask.devicesapi.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/device-service/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceCreateRequest device) {
        var serviceResponse = deviceService.createDevice(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceResponse);
    }
}
