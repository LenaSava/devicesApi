package org.hometask.devicesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hometask.devicesapi.model.DeviceState;

/*
Command to update a new device
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateCommand {


    private String name;

    private String brand;

    private DeviceState state;
}
