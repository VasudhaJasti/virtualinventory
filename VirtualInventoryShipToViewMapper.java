package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryShipToDTO;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryShipToView;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {})
public interface VirtualInventoryShipToViewMapper {

    VirtualInventoryShipToViewMapper INSTANCE = Mappers.getMapper(VirtualInventoryShipToViewMapper.class);

    VirtualInventoryShipToView toEntity(VirtualInventoryShipToDTO virtualInventoryEventTypeDTO);
    VirtualInventoryShipToDTO toDto(VirtualInventoryShipToView virtualInventoryEventType);
}
