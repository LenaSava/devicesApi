package org.hometask.devicesapi.service;

import org.hometask.devicesapi.dto.DeviceCreateCommand;
import org.hometask.devicesapi.dto.DeviceDTO;
import org.hometask.devicesapi.dto.DeviceUpdateCommand;
import org.hometask.devicesapi.exception.DeviceInUseException;
import org.hometask.devicesapi.exception.DeviceNotFoundException;
import org.hometask.devicesapi.mapper.DeviceMapper;
import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.hometask.devicesapi.repository.DeviceRepository;
import org.hometask.devicesapi.validation.DeviceValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private DeviceValidator deviceValidator;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private DeviceEntity createDeviceEntity(Long id, String name, String brand, DeviceState state) {
        return DeviceEntity.builder()
                .id(id)
                .name(name)
                .brand(brand)
                .state(state)
                .creationTime(OffsetDateTime.now())
                .build();
    }

    private DeviceDTO createDeviceDTO(Long id, String name, String brand, DeviceState state) {
        return new DeviceDTO(id, name, brand, state, OffsetDateTime.now());
    }

    private DeviceCreateCommand createDeviceCreateCommand(String name, String brand, DeviceState state) {
        return new DeviceCreateCommand(name, brand, state, OffsetDateTime.now());
    }

    private DeviceUpdateCommand createDeviceUpdateCommand(String name, String brand, DeviceState state) {
        return new DeviceUpdateCommand(name, brand, state);
    }

    @Test
    void createDevice_WithValidCommand_ShouldReturnDeviceDTO() {
        // Given
        DeviceCreateCommand command = createDeviceCreateCommand("iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceEntity entityToSave = createDeviceEntity(null, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceEntity savedEntity = createDeviceEntity(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceDTO expectedDTO = createDeviceDTO(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE);

        when(deviceMapper.toEntity(command)).thenReturn(entityToSave);
        when(deviceRepository.save(entityToSave)).thenReturn(savedEntity);
        when(deviceMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        DeviceDTO result = deviceService.createDevice(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getBrand()).isEqualTo("Apple");
        assertThat(result.getState()).isEqualTo(DeviceState.AVAILABLE);

        verify(deviceMapper, times(1)).toEntity(command);
        verify(deviceRepository, times(1)).save(entityToSave);
        verify(deviceMapper, times(1)).toDTO(savedEntity);
    }

    @Test
    void createDevice_WithInactiveState_ShouldCreateSuccessfully() {
        // Given
        DeviceCreateCommand command = createDeviceCreateCommand("Pixel 8", "Google", DeviceState.INACTIVE);
        DeviceEntity entityToSave = createDeviceEntity(null, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceEntity savedEntity = createDeviceEntity(1L, "Pixel 8", "Google", DeviceState.INACTIVE);
        DeviceDTO expectedDTO = createDeviceDTO(1L, "Pixel 8", "Google", DeviceState.INACTIVE);

        when(deviceMapper.toEntity(command)).thenReturn(entityToSave);
        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(savedEntity);
        when(deviceMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        DeviceDTO result = deviceService.createDevice(command);

        // Then
        assertThat(result.getState()).isEqualTo(DeviceState.INACTIVE);
        verify(deviceRepository, times(1)).save(any(DeviceEntity.class));
    }

    @Test
    void updateDevice_WithStateUpdate_ShouldUpdateAndReturnDTO() {
        // Given
        Long deviceId = 1L;
        DeviceEntity existingDevice = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceUpdateCommand command = createDeviceUpdateCommand(null, null, DeviceState.IN_USE);
        DeviceEntity updatedEntity = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.IN_USE);
        DeviceDTO expectedDTO = createDeviceDTO(deviceId, "iPhone 15", "Apple", DeviceState.IN_USE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doNothing().when(deviceValidator).validateUpdate(existingDevice, command);
        when(deviceRepository.save(existingDevice)).thenReturn(updatedEntity);
        when(deviceMapper.toDTO(updatedEntity)).thenReturn(expectedDTO);

        // When
        DeviceDTO result = deviceService.updateDevice(deviceId, command);

        // Then
        assertThat(result.getState()).isEqualTo(DeviceState.IN_USE);
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceValidator, times(1)).validateUpdate(existingDevice, command);
        verify(deviceRepository, times(1)).save(existingDevice);
    }

    @Test
    void updateDevice_WithNameUpdate_ShouldUpdateSuccessfully() {
        // Given
        Long deviceId = 1L;
        DeviceEntity existingDevice = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceUpdateCommand command = createDeviceUpdateCommand("iPhone 15 Pro", null, null);
        DeviceEntity updatedEntity = createDeviceEntity(deviceId, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);
        DeviceDTO expectedDTO = createDeviceDTO(deviceId, "iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doNothing().when(deviceValidator).validateUpdate(existingDevice, command);
        when(deviceRepository.save(existingDevice)).thenReturn(updatedEntity);
        when(deviceMapper.toDTO(updatedEntity)).thenReturn(expectedDTO);

        // When
        DeviceDTO result = deviceService.updateDevice(deviceId, command);

        // Then
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(existingDevice.getName()).isEqualTo("iPhone 15 Pro");
        verify(deviceValidator, times(1)).validateUpdate(existingDevice, command);
    }

    @Test
    void updateDevice_WithBrandUpdate_ShouldUpdateSuccessfully() {
        // Given
        Long deviceId = 1L;
        DeviceEntity existingDevice = createDeviceEntity(deviceId, "Galaxy S23", "Samsung", DeviceState.AVAILABLE);
        DeviceUpdateCommand command = createDeviceUpdateCommand(null, "Samsung Electronics", null);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doNothing().when(deviceValidator).validateUpdate(existingDevice, command);
        when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);
        when(deviceMapper.toDTO(existingDevice)).thenReturn(createDeviceDTO(deviceId, "Galaxy S23", "Samsung Electronics", DeviceState.AVAILABLE));

        // When
        deviceService.updateDevice(deviceId, command);

        // Then
        assertThat(existingDevice.getBrand()).isEqualTo("Samsung Electronics");
        verify(deviceValidator, times(1)).validateUpdate(existingDevice, command);
    }

    @Test
    void updateDevice_WithAllFields_ShouldUpdateSuccessfully() {
        // Given
        Long deviceId = 1L;
        DeviceEntity existingDevice = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceUpdateCommand command = createDeviceUpdateCommand("iPhone 15 Pro Max", "Apple Inc", DeviceState.IN_USE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doNothing().when(deviceValidator).validateUpdate(existingDevice, command);
        when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);
        when(deviceMapper.toDTO(existingDevice)).thenReturn(createDeviceDTO(deviceId, "iPhone 15 Pro Max", "Apple Inc", DeviceState.IN_USE));

        // When
        deviceService.updateDevice(deviceId, command);

        // Then
        assertThat(existingDevice.getName()).isEqualTo("iPhone 15 Pro Max");
        assertThat(existingDevice.getBrand()).isEqualTo("Apple Inc");
        assertThat(existingDevice.getState()).isEqualTo(DeviceState.IN_USE);
    }

    @Test
    void updateDevice_WithNonExistentDevice_ShouldThrowException() {
        // Given
        Long deviceId = 999L;
        DeviceUpdateCommand command = createDeviceUpdateCommand(null, null, DeviceState.INACTIVE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, command))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceValidator, never()).validateUpdate(any(), any());
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateDevice_InUseDeviceName_ShouldThrowException() {
        // Given
        Long deviceId = 1L;
        DeviceEntity existingDevice = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.IN_USE);
        DeviceUpdateCommand command = createDeviceUpdateCommand("iPhone 15 Pro", null, null);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doThrow(new DeviceInUseException("Cannot update name of a device that is IN_USE"))
                .when(deviceValidator).validateUpdate(existingDevice, command);

        // When & Then
        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name of a device that is IN_USE");

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceValidator, times(1)).validateUpdate(existingDevice, command);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateDevice_WithNullValues_ShouldNotUpdateFields() {
        // Given
        Long deviceId = 1L;
        DeviceEntity existingDevice = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceUpdateCommand command = createDeviceUpdateCommand(null, null, null);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        doNothing().when(deviceValidator).validateUpdate(existingDevice, command);
        when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);
        when(deviceMapper.toDTO(existingDevice)).thenReturn(createDeviceDTO(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE));

        // When
        deviceService.updateDevice(deviceId, command);

        // Then
        assertThat(existingDevice.getName()).isEqualTo("iPhone 15");
        assertThat(existingDevice.getBrand()).isEqualTo("Apple");
        assertThat(existingDevice.getState()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void getDeviceById_WithExistingId_ShouldReturnDevice() {
        // Given
        Long deviceId = 1L;
        DeviceEntity entity = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);
        DeviceDTO expectedDTO = createDeviceDTO(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(entity));
        when(deviceMapper.toDTO(entity)).thenReturn(expectedDTO);

        // When
        DeviceDTO result = deviceService.getDeviceById(deviceId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(deviceId);
        assertThat(result.getName()).isEqualTo("iPhone 15");
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, times(1)).toDTO(entity);
    }

    @Test
    void getDeviceById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long deviceId = 999L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deviceService.getDeviceById(deviceId))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceMapper, never()).toDTO(any());
    }

    // ==================== GET DEVICES BY BRAND TESTS ====================

    @Test
    void getDevicesByBrand_ShouldReturnPaginatedDevices() {
        // Given
        String brand = "Apple";
        List<DeviceEntity> entities = Arrays.asList(
                createDeviceEntity(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE),
                createDeviceEntity(2L, "iPhone 14", "Apple", DeviceState.IN_USE)
        );
        Page<DeviceEntity> entityPage = new PageImpl<>(entities);

        when(deviceRepository.findByBrand(eq(brand), any(Pageable.class))).thenReturn(entityPage);
        when(deviceMapper.toDTO(any(DeviceEntity.class)))
                .thenReturn(createDeviceDTO(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE))
                .thenReturn(createDeviceDTO(2L, "iPhone 14", "Apple", DeviceState.IN_USE));

        // When
        Page<DeviceDTO> result = deviceService.getDevicesByBrand(brand, 0, 10, null);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getBrand()).isEqualTo("Apple");
        assertThat(result.getContent().get(1).getBrand()).isEqualTo("Apple");
        verify(deviceRepository, times(1)).findByBrand(eq(brand), any(Pageable.class));
    }

    @Test
    void getDevicesByBrand_WithNoDevices_ShouldReturnEmptyPage() {
        // Given
        String brand = "NonExistent";
        Page<DeviceEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(deviceRepository.findByBrand(eq(brand), any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<DeviceDTO> result = deviceService.getDevicesByBrand(brand, 0, 10, null);

        // Then
        assertThat(result.getContent()).isEmpty();
        verify(deviceRepository, times(1)).findByBrand(eq(brand), any(Pageable.class));
    }

    @Test
    void getDevicesByBrand_WithSorting_ShouldApplySorting() {
        // Given
        String brand = "Apple";
        Page<DeviceEntity> entityPage = new PageImpl<>(Collections.emptyList());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(deviceRepository.findByBrand(eq(brand), pageableCaptor.capture())).thenReturn(entityPage);

        // When
        deviceService.getDevicesByBrand(brand, 0, 10, "name,desc");

        // Then
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "name"));
    }

    @Test
    void getDevicesByState_ShouldReturnPaginatedDevices() {
        // Given
        DeviceState state = DeviceState.AVAILABLE;
        List<DeviceEntity> entities = Arrays.asList(
                createDeviceEntity(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE),
                createDeviceEntity(2L, "Galaxy S23", "Samsung", DeviceState.AVAILABLE)
        );
        Page<DeviceEntity> entityPage = new PageImpl<>(entities);

        when(deviceRepository.findByState(eq(state), any(Pageable.class))).thenReturn(entityPage);
        when(deviceMapper.toDTO(any(DeviceEntity.class)))
                .thenReturn(createDeviceDTO(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE))
                .thenReturn(createDeviceDTO(2L, "Galaxy S23", "Samsung", DeviceState.AVAILABLE));

        // When
        Page<DeviceDTO> result = deviceService.getDevicesByState(state, 0, 10, null);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getState()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(result.getContent().get(1).getState()).isEqualTo(DeviceState.AVAILABLE);
        verify(deviceRepository, times(1)).findByState(eq(state), any(Pageable.class));
    }

    @Test
    void getDevicesByState_WithInUseState_ShouldReturnInUseDevices() {
        // Given
        DeviceState state = DeviceState.IN_USE;
        List<DeviceEntity> entities = Collections.singletonList(
                createDeviceEntity(1L, "iPhone 15", "Apple", DeviceState.IN_USE)
        );
        Page<DeviceEntity> entityPage = new PageImpl<>(entities);

        when(deviceRepository.findByState(eq(state), any(Pageable.class))).thenReturn(entityPage);
        when(deviceMapper.toDTO(any(DeviceEntity.class)))
                .thenReturn(createDeviceDTO(1L, "iPhone 15", "Apple", DeviceState.IN_USE));

        // When
        Page<DeviceDTO> result = deviceService.getDevicesByState(state, 0, 10, null);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getState()).isEqualTo(DeviceState.IN_USE);
    }

    @Test
    void getDevicesByState_WithInactiveState_ShouldReturnInactiveDevices() {
        // Given
        DeviceState state = DeviceState.INACTIVE;
        List<DeviceEntity> entities = Collections.singletonList(
                createDeviceEntity(1L, "Pixel 8", "Google", DeviceState.INACTIVE)
        );
        Page<DeviceEntity> entityPage = new PageImpl<>(entities);

        when(deviceRepository.findByState(eq(state), any(Pageable.class))).thenReturn(entityPage);
        when(deviceMapper.toDTO(any(DeviceEntity.class)))
                .thenReturn(createDeviceDTO(1L, "Pixel 8", "Google", DeviceState.INACTIVE));

        // When
        Page<DeviceDTO> result = deviceService.getDevicesByState(state, 0, 10, null);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getState()).isEqualTo(DeviceState.INACTIVE);
    }

    @Test
    void getAllDevices_ShouldReturnPaginatedDevices() {
        // Given
        List<DeviceEntity> entities = Arrays.asList(
                createDeviceEntity(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE),
                createDeviceEntity(2L, "Galaxy S23", "Samsung", DeviceState.IN_USE),
                createDeviceEntity(3L, "Pixel 8", "Google", DeviceState.INACTIVE)
        );
        Page<DeviceEntity> entityPage = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(deviceMapper.toDTO(any(DeviceEntity.class)))
                .thenReturn(createDeviceDTO(1L, "iPhone 15", "Apple", DeviceState.AVAILABLE))
                .thenReturn(createDeviceDTO(2L, "Galaxy S23", "Samsung", DeviceState.IN_USE))
                .thenReturn(createDeviceDTO(3L, "Pixel 8", "Google", DeviceState.INACTIVE));

        // When
        Page<DeviceDTO> result = deviceService.getAllDevices(0, 10, null);

        // Then
        assertThat(result.getContent()).hasSize(3);
        verify(deviceRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void deleteDevice_WithAvailableDevice_ShouldDeleteSuccessfully() {
        // Given
        Long deviceId = 1L;
        DeviceEntity device = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.AVAILABLE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        doNothing().when(deviceValidator).validateDelete(device);
        doNothing().when(deviceRepository).delete(device);

        // When
        deviceService.deleteDevice(deviceId);

        // Then
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceValidator, times(1)).validateDelete(device);
        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    void deleteDevice_WithInactiveDevice_ShouldDeleteSuccessfully() {
        // Given
        Long deviceId = 1L;
        DeviceEntity device = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.INACTIVE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        doNothing().when(deviceValidator).validateDelete(device);
        doNothing().when(deviceRepository).delete(device);

        // When
        deviceService.deleteDevice(deviceId);

        // Then
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceValidator, times(1)).validateDelete(device);
        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    void deleteDevice_WithInUseDevice_ShouldThrowException() {
        // Given
        Long deviceId = 1L;
        DeviceEntity device = createDeviceEntity(deviceId, "iPhone 15", "Apple", DeviceState.IN_USE);

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        doThrow(new DeviceInUseException(deviceId)).when(deviceValidator).validateDelete(device);

        // When & Then
        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
                .isInstanceOf(DeviceInUseException.class);

        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceValidator, times(1)).validateDelete(device);
        verify(deviceRepository, never()).delete(any());
    }
}