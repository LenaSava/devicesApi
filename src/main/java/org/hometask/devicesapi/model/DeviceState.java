package org.hometask.devicesapi.model;

import lombok.Getter;

@Getter
public enum DeviceState {
    AVAILABLE("Device is ready to use"),
    IN_USE("Device is currently being used"),
    INACTIVE("Device is not available");

    private final String description;

    DeviceState(String description) {
        this.description = description;
    }

}
