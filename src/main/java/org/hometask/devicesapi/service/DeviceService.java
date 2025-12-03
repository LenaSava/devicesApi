package org.hometask.devicesapi.service;

import org.hometask.devicesapi.dto.DeviceCreateRequest;
import org.hometask.devicesapi.dto.DeviceResponse;

public interface DeviceService {

    DeviceResponse createDevice(DeviceCreateRequest request);
}
