package org.hometask.devicesapi.exception;

public class DeviceInUseException extends RuntimeException {
    public DeviceInUseException(String message) {
        super(message);
    }

    public DeviceInUseException(Long deviceId) {
        super("Device with id %d is currently in use and cannot be deleted".formatted(deviceId));
    }
}
