package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcUiViewDTO;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdcUiView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {VirtualInventoryHeader.class})
public interface VirtualInventoryNdcViewMapper {

    VirtualInventoryNdcViewMapper INSTANCE = Mappers.getMapper(VirtualInventoryNdcViewMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "virtualInventoryNdc.id"),
            @Mapping(source = "virtualInventoryHeaderId", target = "virtualInventoryHeader.id"),
            @Mapping(source = "eventAsOfDate", target = "lastCalculatedDate")
    })
    VirtualInventoryNdcUiView toEntity(VirtualInventoryNdcUiViewDTO virtualInventoryNdcUiViewDTO);

    @Mappings({
            @Mapping(source = "virtualInventoryNdc.id", target = "id"),
            @Mapping(source = "virtualInventoryHeader.id", target = "virtualInventoryHeaderId"),
            @Mapping(source = "lastCalculatedDate", target = "eventAsOfDate")
    })
    VirtualInventoryNdcUiViewDTO toDto(VirtualInventoryNdcUiView virtualInventoryNdcUiView);
}
