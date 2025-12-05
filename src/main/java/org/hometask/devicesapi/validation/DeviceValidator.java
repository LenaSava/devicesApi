package org.hometask.devicesapi.validation;

import lombok.extern.slf4j.Slf4j;
import org.hometask.devicesapi.dto.DeviceUpdateCommand;
import org.hometask.devicesapi.exception.DeviceInUseException;
import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceValidator {

    public void validateUpdate(DeviceEntity existingDevice, DeviceUpdateCommand command) {
        log.debug("Validating update for device {} with state {}",
                existingDevice.getId(), existingDevice.getState());

        if (existingDevice.getState() == DeviceState.IN_USE) {
            if (command.getName() != null && !command.getName().equals(existingDevice.getName())) {
                throw new DeviceInUseException(
                        "Cannot update name of a device that is IN_USE"
                );
            }
            if (command.getBrand() != null && !command.getBrand().equals(existingDevice.getBrand())) {
                throw new DeviceInUseException(
                        "Cannot update brand of a device that is IN_USE"
                );
            }
        }
    }

    public void validateDelete(DeviceEntity device) {
        log.debug("Validating delete for device {} with state {}",
                device.getId(), device.getState());

        if (device.getState() == DeviceState.IN_USE) {
            throw new DeviceInUseException(device.getId());
        }
    }
}
