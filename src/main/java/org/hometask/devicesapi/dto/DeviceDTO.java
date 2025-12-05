package org.hometask.devicesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hometask.devicesapi.model.DeviceState;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {

    private Long id;

    private String name;

    private String brand;

    private DeviceState state;

    private OffsetDateTime creationTime;
}
