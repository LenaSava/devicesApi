package org.hometask.devicesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hometask.devicesapi.model.DeviceState;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private String name;

    private String brand;

    private DeviceState state;

    private LocalDateTime creationTime;
}
