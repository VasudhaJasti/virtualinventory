package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventsViewDTO;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventsView;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {VirtualInventoryHeader.class})
public interface VirtualInventoryEventsViewMapper {

    VirtualInventoryEventsViewMapper INSTANCE= Mappers.getMapper(VirtualInventoryEventsViewMapper.class);


    VirtualInventoryEventsView toEntity(VirtualInventoryEventsViewDTO virtualInventoryEventsViewDTO);


    VirtualInventoryEventsViewDTO toDto(VirtualInventoryEventsView virtualInventoryEventsView);
}
