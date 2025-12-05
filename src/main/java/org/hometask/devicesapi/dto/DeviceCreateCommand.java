package org.hometask.devicesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hometask.devicesapi.model.DeviceState;

import java.time.OffsetDateTime;

/*
Command to create a new device
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCreateCommand {

    private String name;

    private String brand;

    private DeviceState state;

    private OffsetDateTime createdAt;
}
