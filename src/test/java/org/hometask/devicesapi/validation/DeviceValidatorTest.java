package org.hometask.devicesapi.validation;

import org.hometask.devicesapi.dto.DeviceUpdateCommand;
import org.hometask.devicesapi.exception.DeviceInUseException;
import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DeviceValidatorTest {

    @InjectMocks
    private DeviceValidator deviceValidator;

    @Test
    void validateUpdate_AvailableDevice_UpdateName_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15 Pro", null, null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_UpdateBrand_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, "Samsung", null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_UpdateState_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, null, DeviceState.IN_USE);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_UpdateAllFields_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("Galaxy S23", "Samsung", DeviceState.INACTIVE);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_UpdateNameAndBrand_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15 Pro Max", "Apple Inc", null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_EmptyCommand_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, null, null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_SameName_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15", null, null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_AvailableDevice_SameBrand_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, "Apple", null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(availableDevice(), command))
                .doesNotThrowAnyException();
    }


    @Test
    void validateUpdate_InUseDevice_UpdateName_ShouldThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15 Pro", null, null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(inUseDevice(), command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_UpdateBrand_ShouldThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, "Samsung", null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(inUseDevice(), command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update brand of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_UpdateNameAndBrand_ShouldThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("Galaxy S23", "Samsung", null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(inUseDevice(), command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessage("Cannot update name of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_SameName_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15", null, null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inUseDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InUseDevice_SameBrand_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, "Apple", null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inUseDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InactiveDevice_UpdateName_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15 Pro", null, null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inactiveDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InactiveDevice_UpdateBrand_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, "Samsung", null);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inactiveDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InactiveDevice_UpdateState_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, null, DeviceState.AVAILABLE);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inactiveDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InactiveDevice_UpdateAllFields_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand("Galaxy S23", "Samsung", DeviceState.IN_USE);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inactiveDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InactiveDevice_UpdateStateToInUse_ShouldNotThrowException() {
        // Given
        DeviceUpdateCommand command = createUpdateCommand(null, null, DeviceState.IN_USE);

        // When & Then
        assertThatCode(() -> deviceValidator.validateUpdate(inactiveDevice(), command))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_InUseDevice_NameWithSpaces_ShouldThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.IN_USE);
        DeviceUpdateCommand command = createUpdateCommand(" iPhone 15 ", null, null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(device, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_DifferentCase_ShouldThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.IN_USE);
        DeviceUpdateCommand command = createUpdateCommand("iphone 15", null, null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(device, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_BrandDifferentCase_ShouldThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.IN_USE);
        DeviceUpdateCommand command = createUpdateCommand(null, "apple", null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(device, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update brand of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_EmptyName_ShouldThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.IN_USE);
        DeviceUpdateCommand command = createUpdateCommand("", null, null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(device, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_EmptyBrand_ShouldThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.IN_USE);
        DeviceUpdateCommand command = createUpdateCommand(null, "", null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(device, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update brand of a device that is IN_USE");
    }

    @Test
    void validateUpdate_InUseDevice_SpecialCharactersInName_ShouldThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.IN_USE);
        DeviceUpdateCommand command = createUpdateCommand("iPhone 15 Pro Max (2024)", null, null);

        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateUpdate(device, command))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name of a device that is IN_USE");
    }

    @Test
    void validateDelete_AvailableDevice_ShouldNotThrowException() {
        // Given
        DeviceEntity device = createDevice(1L, DeviceState.AVAILABLE);

        // When & Then
        assertThatCode(() -> deviceValidator.validateDelete(device))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDelete_InUseDevice_ShouldThrowExceptionWithId() {
        // Given
        Long deviceId = 123L;

        DeviceEntity device = createDevice(deviceId, DeviceState.IN_USE);
        // When & Then
        assertThatThrownBy(() -> deviceValidator.validateDelete(device))
                .isInstanceOf(DeviceInUseException.class);

    }

    private DeviceEntity createDevice(Long id, DeviceState state) {
        DeviceEntity device = new DeviceEntity();
        device.setId(id);
        device.setName("iPhone 15");
        device.setBrand("Apple");
        device.setState(state);
        device.setCreationTime(OffsetDateTime.now());
        return device;
    }

    private DeviceEntity availableDevice() {
        return createDevice(1L, DeviceState.AVAILABLE);
    }

    private DeviceEntity inUseDevice() {
        return createDevice(1L, DeviceState.IN_USE);
    }

    private DeviceUpdateCommand createUpdateCommand(String name, String brand, DeviceState state) {
        return new DeviceUpdateCommand(name, brand, state);
    }

    private DeviceEntity inactiveDevice() {
        return createDevice(1L, DeviceState.INACTIVE);
    }
}