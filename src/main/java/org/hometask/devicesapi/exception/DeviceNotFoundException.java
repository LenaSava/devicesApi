package org.hometask.devicesapi.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(Long id) {
        super("Device not found with id: %d".formatted(id));
    }
}
