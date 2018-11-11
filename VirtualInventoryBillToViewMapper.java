package com.capturerx.cumulus4.virtualinventory.services.mappers;


import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryBillToDTO;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryBillToView;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {})
public interface VirtualInventoryBillToViewMapper {

    VirtualInventoryBillToViewMapper INSTANCE = Mappers.getMapper(VirtualInventoryBillToViewMapper.class);

    VirtualInventoryBillToView toEntity(VirtualInventoryBillToDTO virtualInventoryEventTypeDTO);
    VirtualInventoryBillToDTO toDto(VirtualInventoryBillToView virtualInventoryEventType);
}
