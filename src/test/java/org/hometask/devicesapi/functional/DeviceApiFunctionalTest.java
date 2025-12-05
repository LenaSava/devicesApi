package org.hometask.devicesapi.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hometask.devicesapi.dto.DeviceCreateRequest;
import org.hometask.devicesapi.dto.DeviceUpdateRequest;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.repository.DeviceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DeviceApiFunctionalTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("devices_db_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @AfterEach
    void cleanup() {
        deviceRepository.deleteAll();
    }

    @Test
    void createDevice_WithValidData_ShouldReturn201AndDeviceDTO() throws Exception {
        // Given
        DeviceCreateRequest request = new DeviceCreateRequest("iPhone 15 Pro", "Apple");

        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.brand", is("Apple")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")))
                .andExpect(jsonPath("$.creationTime", notNullValue()));
    }

    @Test
    void createDevice_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - missing name
        String requestBody = "{\"brand\": \"Apple\"}";

        // When & Then
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDeviceById_WhenDeviceExists_ShouldReturn200AndDevice() throws Exception {
        // Given - Create a device first
        DeviceCreateRequest createRequest = new DeviceCreateRequest("iPhone 15", "Apple");
        MvcResult createResult = mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long deviceId = objectMapper.readTree(responseBody).get("id").asLong();

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.intValue())))
                .andExpect(jsonPath("$.name", is("iPhone 15")))
                .andExpect(jsonPath("$.brand", is("Apple")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")));
    }

    @Test
    void getDeviceById_WhenDeviceDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        Long nonExistentId = 99999L;

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDevices_WithNoDevices_ShouldReturnEmptyPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void getAllDevices_WithMultipleDevices_ShouldReturnPaginatedList() throws Exception {
        // Given - Create multiple devices
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("iPhone 15", "Apple"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Galaxy S23", "Samsung"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Pixel 8", "Google"))))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("iPhone 15", "Galaxy S23", "Pixel 8")));
    }

    @Test
    void getDevicesByBrand_WithMatchingBrand_ShouldReturnFilteredDevices() throws Exception {
        // Given
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("iPhone 15", "Apple"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("iPhone 14", "Apple"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Galaxy S23", "Samsung"))))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("brand", "Apple")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].brand", everyItem(is("Apple"))));
    }

    @Test
    void getDevicesByState_WithAvailableState_ShouldReturnAvailableDevices() throws Exception {
        // Given - All newly created devices are AVAILABLE by default
        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Device 1", "Brand 1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Device 2", "Brand 2"))))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/device-service/v1/devices")
                        .param("state", "AVAILABLE")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].state", everyItem(is("AVAILABLE"))));
    }

    @Test
    void updateDevice_StateOnly_ShouldUpdateSuccessfully() throws Exception {
        // Given - Create device
        MvcResult createResult = mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("iPhone 15", "Apple"))))
                .andReturn();

        Long deviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeviceUpdateRequest(null, null, DeviceState.IN_USE))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.intValue())))
                .andExpect(jsonPath("$.state", is("IN_USE")))
                .andExpect(jsonPath("$.name", is("iPhone 15")))
                .andExpect(jsonPath("$.brand", is("Apple")));
    }

    @Test
    void updateDevice_NameWhenInUse_ShouldReturn400() throws Exception {
        // Given - Create device and set to IN_USE
        MvcResult createResult = mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("iPhone 15", "Apple"))))
                .andReturn();

        Long deviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeviceUpdateRequest(null, null, DeviceState.IN_USE))))
                .andExpect(status().isOk());

        // When & Then - Try to update name
        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeviceUpdateRequest("iPhone 15 Pro", null, null))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDevice_NonExistentDevice_ShouldReturn404() throws Exception {
        // Given
        Long nonExistentId = 99999L;

        // When & Then
        mockMvc.perform(patch("/device-service/v1/devices/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeviceUpdateRequest(null, null, DeviceState.INACTIVE))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDevice_AvailableDevice_ShouldReturn204() throws Exception {
        // Given - Create device (AVAILABLE by default)
        MvcResult createResult = mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Device", "Brand"))))
                .andReturn();

        Long deviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDevice_InactiveDevice_ShouldReturn204() throws Exception {
        // Given - Create device and set to INACTIVE
        MvcResult createResult = mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Device", "Brand"))))
                .andReturn();

        Long deviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeviceUpdateRequest(null, null, DeviceState.INACTIVE))))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDevice_InUseDevice_ShouldReturn400() throws Exception {
        // Given - Create device and set to IN_USE
        MvcResult createResult = mockMvc.perform(post("/device-service/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceCreateRequest("Device", "Brand"))))
                .andReturn();

        Long deviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/device-service/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeviceUpdateRequest(null, null, DeviceState.IN_USE))))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(delete("/device-service/v1/devices/{id}", deviceId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Verify device still exists
        mockMvc.perform(get("/device-service/v1/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("IN_USE")));
    }
}
