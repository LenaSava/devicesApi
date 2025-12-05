package org.hometask.devicesapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hometask.devicesapi.dto.*;
import org.hometask.devicesapi.exception.DeviceInUseException;
import org.hometask.devicesapi.exception.DeviceNotFoundException;
import org.hometask.devicesapi.mapper.DeviceMapper;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private DeviceService deviceService;
    @MockBean
    private DeviceMapper deviceMapper;

    @Test
    void createDevice_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        DeviceCreateRequest request = new DeviceCreateRequest(
                "iPhone 15 Pro",
                "Apple"
        );

        DeviceCreateCommand command = new DeviceCreateCommand(
                "iPhone 15 Pro",
                "Apple",
                DeviceState.AVAILABLE,
                OffsetDateTime.now()
        );

        DeviceDTO response = new DeviceDTO(
                1L,
                "iPhone 15 Pro",
                "Apple",
                DeviceState.AVAILABLE,
                OffsetDateTime.now()
        );

        when(deviceService.createDevice(any(DeviceCreateCommand.class)))
                .thenReturn(response);
        when(deviceMapper.toCreateCommand(any(DeviceCreateRequest.class)))
                .thenReturn(command);

        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.brand", is("Apple")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")))
                .andExpect(jsonPath("$.creationTime", notNullValue()));

        verify(deviceService, times(1)).createDevice(any(DeviceCreateCommand.class));
    }

    @Test
    void createDevice_WithNullName_ShouldReturnBadRequest() throws Exception {
        // Given
        String requestBody = "{\"name\": null, \"brand\": \"Apple\"}";
        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(deviceService, never()).createDevice(any());
    }

    @Test
    void createDevice_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(deviceService, never()).createDevice(any());
    }

    @Test
    void getDeviceById_WhenDeviceExists_ShouldReturnDevice() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceDTO response = createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);

        when(deviceService.getDeviceById(deviceId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.brand", is("Apple")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")));

        verify(deviceService, times(1)).getDeviceById(deviceId);
    }

    @Test
    void getDeviceById_WhenDeviceDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Long deviceId = 999L;
        when(deviceService.getDeviceById(deviceId))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).getDeviceById(deviceId);
    }

    @Test
    void getDeviceById_WithInUseState_ShouldReturnDevice() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceDTO response = createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.IN_USE);

        when(deviceService.getDeviceById(deviceId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("IN_USE")));

        verify(deviceService, times(1)).getDeviceById(deviceId);
    }

    @Test
    void getDeviceById_WithInactiveState_ShouldReturnDevice() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceDTO response = createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.INACTIVE);

        when(deviceService.getDeviceById(deviceId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("INACTIVE")));

        verify(deviceService, times(1)).getDeviceById(deviceId);
    }

    @Test
    void getDevices_ShouldReturnPaginatedList() throws Exception {
        // Given
        List<DeviceDTO> deviceList = Arrays.asList(
                createDeviceDTO(1L, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE),
                createDeviceDTO(2L, "Galaxy S23", "Samsung", DeviceState.IN_USE),
                createDeviceDTO(3L, "Pixel 8", "Google", DeviceState.INACTIVE)
        );
        Page<DeviceDTO> page = new PageImpl<>(deviceList);

        when(deviceService.getAllDevices(0, 10, null)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.content[1].name", is("Galaxy S23")))
                .andExpect(jsonPath("$.content[2].name", is("Pixel 8")));

        verify(deviceService, times(1)).getAllDevices(0, 10, null);
    }

    @Test
    void getDevices_WhenNoDevices_ShouldReturnEmptyList() throws Exception {
        // Given
        Page<DeviceDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(deviceService.getAllDevices(0, 10, null)).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(deviceService, times(1)).getAllDevices(0, 10, null);
    }

    @Test
    void getDevices_FilterByBrand_ShouldReturnFilteredList() throws Exception {
        // Given
        List<DeviceDTO> deviceList = Arrays.asList(
                createDeviceDTO(1L, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE),
                createDeviceDTO(2L, "iPhone 14", "Apple", DeviceState.IN_USE)
        );
        Page<DeviceDTO> page = new PageImpl<>(deviceList);

        when(deviceService.getDevicesByBrand("Apple", 0, 10, null)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("brand", "Apple"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].brand", is("Apple")))
                .andExpect(jsonPath("$.content[1].brand", is("Apple")));

        verify(deviceService, times(1)).getDevicesByBrand("Apple", 0, 10, null);
    }

    @Test
    void getDevices_FilterByState_ShouldReturnFilteredList() throws Exception {
        // Given
        List<DeviceDTO> deviceList = Arrays.asList(
                createDeviceDTO(1L, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE),
                createDeviceDTO(2L, "Galaxy S23", "Samsung", DeviceState.AVAILABLE)
        );
        Page<DeviceDTO> page = new PageImpl<>(deviceList);

        when(deviceService.getDevicesByState(DeviceState.AVAILABLE, 0, 10, null)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("state", "AVAILABLE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].state", is("AVAILABLE")))
                .andExpect(jsonPath("$.content[1].state", is("AVAILABLE")));

        verify(deviceService, times(1)).getDevicesByState(DeviceState.AVAILABLE, 0, 10, null);
    }

    @Test
    void updateDevice_NameAndBrand_ShouldReturnUpdatedDevice() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceUpdateRequest request = createUpdateRequest("iPhone 15 Pro Max", "Apple", null);
        DeviceUpdateCommand command = new DeviceUpdateCommand("iPhone 15 Pro Max", "Apple", null);
        DeviceDTO response = createDeviceDTO(deviceId, "iPhone 15 Pro Max", "Apple", DeviceState.AVAILABLE);

        when(deviceMapper.toUpdateCommand(any(DeviceUpdateRequest.class))).thenReturn(command);
        when(deviceService.updateDevice(eq(deviceId), any(DeviceUpdateCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro Max")))
                .andExpect(jsonPath("$.brand", is("Apple")));

        verify(deviceService, times(1)).updateDevice(eq(deviceId), any(DeviceUpdateCommand.class));
    }

    @Test
    void updateDevice_NonExistentDevice_ShouldReturnNotFound() throws Exception {
        // Given
        Long deviceId = 999L;
        DeviceUpdateRequest request = createUpdateRequest(null, null, DeviceState.INACTIVE);
        DeviceUpdateCommand command = new DeviceUpdateCommand(null, null, DeviceState.INACTIVE);

        when(deviceMapper.toUpdateCommand(any(DeviceUpdateRequest.class))).thenReturn(command);
        when(deviceService.updateDevice(eq(deviceId), any(DeviceUpdateCommand.class)))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).updateDevice(eq(deviceId), any(DeviceUpdateCommand.class));
    }

    @Test
    @DisplayName("Should update all fields when device is AVAILABLE")
    void updateDevice_AllFields_ShouldReturnUpdatedDevice() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceUpdateRequest request = createUpdateRequest("Updated Name", "Updated Brand", DeviceState.INACTIVE);
        DeviceUpdateCommand command = new DeviceUpdateCommand("Updated Name", "Updated Brand", DeviceState.INACTIVE);
        DeviceDTO response = createDeviceDTO(deviceId, "Updated Name", "Updated Brand", DeviceState.INACTIVE);

        when(deviceMapper.toUpdateCommand(any(DeviceUpdateRequest.class))).thenReturn(command);
        when(deviceService.updateDevice(eq(deviceId), any(DeviceUpdateCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.brand", is("Updated Brand")))
                .andExpect(jsonPath("$.state", is("INACTIVE")));

        verify(deviceService, times(1)).updateDevice(eq(deviceId), any(DeviceUpdateCommand.class));
    }

    @Test
    @DisplayName("Should handle empty update request")
    void updateDevice_EmptyRequest_ShouldReturnOk() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceUpdateRequest request = createUpdateRequest(null, null, null);
        DeviceUpdateCommand command = new DeviceUpdateCommand(null, null, null);
        DeviceDTO response = createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);

        when(deviceMapper.toUpdateCommand(any(DeviceUpdateRequest.class))).thenReturn(command);
        when(deviceService.updateDevice(eq(deviceId), any(DeviceUpdateCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(deviceService, times(1)).updateDevice(eq(deviceId), any(DeviceUpdateCommand.class));
    }

    @Test
    @DisplayName("Should transition device from AVAILABLE to IN_USE")
    void updateDevice_AvailableToInUse_ShouldReturnUpdatedDevice() throws Exception {
        // Given
        Long deviceId = 1L;
        DeviceUpdateRequest request = createUpdateRequest(null, null, DeviceState.IN_USE);
        DeviceUpdateCommand command = new DeviceUpdateCommand(null, null, DeviceState.IN_USE);
        DeviceDTO response = createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.IN_USE);

        when(deviceMapper.toUpdateCommand(any(DeviceUpdateRequest.class))).thenReturn(command);
        when(deviceService.updateDevice(eq(deviceId), any(DeviceUpdateCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("IN_USE")));

        verify(deviceService, times(1)).updateDevice(eq(deviceId), any(DeviceUpdateCommand.class));
    }

    @Test
    void updateDevice_InUseToAvailable_ShouldReturnUpdatedDevice() throws Exception {
        Long deviceId = 1L;

        DeviceUpdateRequest request =
                new DeviceUpdateRequest(null, null, DeviceState.AVAILABLE);

        DeviceUpdateCommand command =
                new DeviceUpdateCommand(null, null, DeviceState.AVAILABLE);

        DeviceDTO response =
                createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);

        when(deviceMapper.toUpdateCommand(any())).thenReturn(command);
        when(deviceService.updateDevice(eq(deviceId), any())).thenReturn(response);

        mockMvc.perform(
                        patch("/device-service/v1/devices/{id}", deviceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("AVAILABLE"));

        verify(deviceService).updateDevice(eq(deviceId), any());
    }

    @Test
    @DisplayName("Should delete AVAILABLE device successfully")
    void deleteDevice_AvailableDevice_ShouldReturnNoContent() throws Exception {
        // Given
        Long deviceId = 1L;
        doNothing().when(deviceService).deleteDevice(deviceId);

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDevice(deviceId);
    }

    @Test
    @DisplayName("Should delete INACTIVE device successfully")
    void deleteDevice_InactiveDevice_ShouldReturnNoContent() throws Exception {
        // Given
        Long deviceId = 1L;
        doNothing().when(deviceService).deleteDevice(deviceId);

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDevice(deviceId);
    }

    @Test
    @DisplayName("Should return 400 when trying to delete IN_USE device")
    void deleteDevice_InUseDevice_ShouldReturnBadRequest() throws Exception {
        // Given
        Long deviceId = 1L;
        doThrow(new DeviceInUseException("Cannot delete device in use"))
                .when(deviceService).deleteDevice(deviceId);

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(deviceService, times(1)).deleteDevice(deviceId);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent device")
    void deleteDevice_NonExistentDevice_ShouldReturnNotFound() throws Exception {
        // Given
        Long deviceId = 999L;
        doThrow(new DeviceNotFoundException(deviceId))
                .when(deviceService).deleteDevice(deviceId);

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).deleteDevice(deviceId);
    }

    @Test
    @DisplayName("Should handle deletion with path variable containing special characters")
    void deleteDevice_WithSpecialPathVariable_ShouldHandleCorrectly() throws Exception {
        // Given
        Long deviceId = 123L;
        doNothing().when(deviceService).deleteDevice(deviceId);

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDevice(deviceId);
    }

    @Test
    void getDevices_WithInvalidState_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("state", "INVALID_STATE"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(deviceService, never()).getDevicesByState(any(), anyInt(), anyInt(), any());
    }

    @Test
    void getDevices_WithNegativePageNumber_ShouldAcceptRequest() throws Exception {
        // Given
        Page<DeviceDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(deviceService.getAllDevices(-1, 10, null)).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("page", "-1"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(deviceService, times(1)).getAllDevices(-1, 10, null);
    }

    @Test
    void getDevices_WithLargePageSize_ShouldAcceptRequest() throws Exception {
        // Given
        Page<DeviceDTO> page = new PageImpl<>(Collections.emptyList());
        when(deviceService.getAllDevices(0, 1000, null)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("size", "1000"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(deviceService, times(1)).getAllDevices(0, 1000, null);
    }

    @Test
    void getDevices_WithMultipleParams_ShouldPrioritizeBrand() throws Exception {
        // Given - When both brand and state are provided, brand takes precedence
        List<DeviceDTO> deviceList = Arrays.asList(
                createDeviceDTO(1L, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE)
        );
        Page<DeviceDTO> page = new PageImpl<>(deviceList);

        when(deviceService.getDevicesByBrand("Apple", 0, 10, "name,asc")).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("brand", "Apple")
                        .param("state", "AVAILABLE")
                        .param("sort", "name,asc"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(deviceService, times(1)).getDevicesByBrand("Apple", 0, 10, "name,asc");
        verify(deviceService, never()).getDevicesByState(any(), anyInt(), anyInt(), any());
    }

    @Test
    void getDeviceById_WithInvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", "invalid"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(deviceService, never()).getDeviceById(any());
    }

    @Test
    void createDevice_WithoutContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // Given
        DeviceCreateRequest request = createDeviceRequest("iPhone 15 Pro", "Apple");

        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());

        verify(deviceService, never()).createDevice(any());
    }

    @Test
    void createDevice_WithVeryLongName_ShouldProcess() throws Exception {
        // Given
        String longName = "A".repeat(500);
        DeviceCreateRequest request = createDeviceRequest(longName, "Apple");
        DeviceCreateCommand command = new DeviceCreateCommand(
                longName,
                "Apple",
                DeviceState.AVAILABLE,
                OffsetDateTime.now()
        );
        DeviceDTO response = createDeviceDTO(1L, longName, "Apple", DeviceState.AVAILABLE);

        when(deviceMapper.toCreateCommand(any(DeviceCreateRequest.class))).thenReturn(command);
        when(deviceService.createDevice(any(DeviceCreateCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(deviceService, times(1)).createDevice(any(DeviceCreateCommand.class));
    }

    @Test
    void getDevices_WithSpecialCharactersInBrand_ShouldReturnResults() throws Exception {
        // Given
        String brandWithSpecialChars = "LG&Samsung";
        Page<DeviceDTO> page = new PageImpl<>(Collections.emptyList());
        when(deviceService.getDevicesByBrand(brandWithSpecialChars, 0, 10, null)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("brand", brandWithSpecialChars))
                .andDo(print())
                .andExpect(status().isOk());

        verify(deviceService, times(1)).getDevicesByBrand(brandWithSpecialChars, 0, 10, null);
    }

    private DeviceCreateRequest createDeviceRequest(String name, String brand) {
        return new DeviceCreateRequest(name, brand);
    }

    private DeviceDTO createDeviceDTO(Long id, String name, String brand, DeviceState state) {
        return new DeviceDTO(id, name, brand, state, OffsetDateTime.now());
    }

    private DeviceUpdateRequest createUpdateRequest(String name, String brand, DeviceState state) {
        return new DeviceUpdateRequest(name, brand, state);
    }
}
