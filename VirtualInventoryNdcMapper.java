package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdc;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {VirtualInventoryHeader.class})
public interface VirtualInventoryNdcMapper {

    VirtualInventoryNdcMapper INSTANCE= Mappers.getMapper(VirtualInventoryNdcMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "id"),

            @Mapping(target = "virtualInventoryHeader.id", source = "virtualInventoryHeaderId"),
            @Mapping(source = "ndc", target = "ndc"),
            @Mapping(source = "packageQuantity", target = "packageQuantity"),
            @Mapping(source = "lastCalculatedDate", target = "lastCalculatedDate"),
            @Mapping(source = "packageSize", target = "packageSize"),
            @Mapping(source = "drugDescription", target = "drugDescription"),
            @Mapping(source = "runningTotal", target = "runningTotal"),
            @Mapping(source = "createdDate", target = "createdDate"),
            @Mapping(source = "lastModifiedDate", target = "lastModifiedDate")
    })
    VirtualInventoryNdc toEntity(VirtualInventoryNdcDTO virtualInventoryNdcDTO);

    @Mappings({
            @Mapping(source = "id", target = "id"),

            @Mapping(source = "virtualInventoryHeader.id", target = "virtualInventoryHeaderId"),
            @Mapping(source = "ndc", target = "ndc"),
            @Mapping(source = "packageQuantity", target = "packageQuantity"),
            @Mapping(source = "lastCalculatedDate", target = "lastCalculatedDate"),
            @Mapping(source = "packageSize", target = "packageSize"),
            @Mapping(source = "drugDescription", target = "drugDescription"),
            @Mapping(source = "runningTotal", target = "runningTotal"),
            @Mapping(source = "createdDate", target = "createdDate"),
            @Mapping(source = "lastModifiedDate", target = "lastModifiedDate")
    })
    VirtualInventoryNdcDTO toDto(VirtualInventoryNdc virtualInventoryNdc);
}
