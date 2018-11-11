package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventType;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {})
public interface VirtualInventoryEventTypeMapper {

    VirtualInventoryEventTypeMapper INSTANCE = Mappers.getMapper(VirtualInventoryEventTypeMapper.class);

    VirtualInventoryEventType toEntity(VirtualInventoryEventTypeDTO virtualInventoryEventTypeDTO);
    VirtualInventoryEventTypeDTO toDto(VirtualInventoryEventType virtualInventoryEventType);
}
