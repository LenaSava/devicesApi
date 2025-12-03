package org.hometask.devicesapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Request to create a new device
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;
}
