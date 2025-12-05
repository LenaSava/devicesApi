package org.hometask.devicesapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.hometask.devicesapi.dto.DeviceCreateRequest;
import org.hometask.devicesapi.dto.DeviceDTO;
import org.hometask.devicesapi.dto.DeviceUpdateRequest;
import org.hometask.devicesapi.exception.ErrorResponse;
import org.hometask.devicesapi.mapper.DeviceMapper;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.service.DeviceService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/device-service/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @PostMapping
    @Operation(summary = "Create a new device")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceCreateRequest request) {
        var createCommand = deviceMapper.toCreateCommand(request);
        var serviceResponse = deviceService.createDevice(createCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceResponse);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update of device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "400", description = "Invalid update")
    })
    public ResponseEntity<DeviceDTO> updateDevice(
            @PathVariable Long id,
            @RequestBody DeviceUpdateRequest request) {
        var updateCommand = deviceMapper.toUpdateCommand(request);
        var response = deviceService.updateDevice(id, updateCommand);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a device by ID", description = "Retrieves a single device by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeviceDTO> getDeviceById(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        DeviceDTO response = deviceService.getDeviceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieves all devices or filters by brand/state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully")
    })
    public ResponseEntity<Page<DeviceDTO>> getDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {

        Page<DeviceDTO> devices;

        if (brand != null) {
            devices = deviceService.getDevicesByBrand(brand, page, size, sort);
        } else if (state != null) {
            devices = deviceService.getDevicesByState(state, page, size, sort);
        } else {
            devices = deviceService.getAllDevices(page, size, sort);
        }
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device", description = "Deletes a device by its ID (cannot delete IN_USE devices)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete IN_USE device",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
