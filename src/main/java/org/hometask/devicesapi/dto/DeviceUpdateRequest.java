package org.hometask.devicesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hometask.devicesapi.model.DeviceState;

/*
Request to update a device (partial update supported)
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateRequest {

    private String name;

    private String brand;

    private DeviceState state;
}
