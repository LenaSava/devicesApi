package org.hometask.devicesapi.mapper;

import org.hometask.devicesapi.dto.DeviceResponse;
import org.hometask.devicesapi.model.DeviceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceResponse deviceToDeviseDTO(DeviceEntity device);
}
