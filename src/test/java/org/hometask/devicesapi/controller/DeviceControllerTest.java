package org.hometask.devicesapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hometask.devicesapi.dto.DeviceCreateRequest;
import org.hometask.devicesapi.dto.DeviceResponse;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void createDevice_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        DeviceCreateRequest request = new DeviceCreateRequest(
                "iPhone 15 Pro",
                "Apple"
        );

        DeviceResponse response = new DeviceResponse(
                "iPhone 15 Pro",
                "Apple",
                DeviceState.AVAILABLE,
                LocalDateTime.now()
        );

        when(deviceService.createDevice(any(DeviceCreateRequest.class)))
                .thenReturn(response);

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

        verify(deviceService, times(1)).createDevice(any(DeviceCreateRequest.class));
    }


}