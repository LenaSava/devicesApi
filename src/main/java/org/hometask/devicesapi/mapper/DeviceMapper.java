package org.hometask.devicesapi.mapper;

import org.hometask.devicesapi.dto.*;
import org.hometask.devicesapi.model.DeviceEntity;
import org.hometask.devicesapi.model.DeviceState;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceEntity toEntity(DeviceCreateCommand command);
    DeviceDTO toDTO(DeviceEntity device);
    DeviceCreateCommand toCreateCommand(DeviceCreateRequest request);
    DeviceUpdateCommand toUpdateCommand(DeviceUpdateRequest request);

    @AfterMapping
    default void addExtraFields(DeviceCreateRequest request, @MappingTarget DeviceCreateCommand command) {
        command.setState(DeviceState.AVAILABLE);
        command.setCreatedAt(OffsetDateTime.now());

    }
}
