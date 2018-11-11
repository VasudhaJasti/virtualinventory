package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryHeaderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {})
public interface VirtualInventoryHeaderMapper {

    VirtualInventoryHeaderMapper INSTANCE = Mappers.getMapper(VirtualInventoryHeaderMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "coveredEntityBillToId", target = "coveredEntityBillToId"),
            @Mapping(source = "pharmacyBillToId", target = "pharmacyBillToId"),
            @Mapping(source = "pharmacyShipToId", target = "pharmacyShipToId"),
            @Mapping(source = "centralFillShipToId", target = "centralFillShipToId"),
            @Mapping(source = "centralFillBillToId", target = "centralFillBillToId"),
            @Mapping(source = "createdDate", target = "createdDate"),
            @Mapping(source = "createdBy", target = "createdBy"),
            @Mapping(source = "lastModifiedDate", target="lastModifiedDate"),
            @Mapping(source = "billToTypeCode", target = "billToTypeCode"),
            @Mapping(source = "billToName", target = "billToName"),
            @Mapping(source = "shipToTypeCode", target = "shipToTypeCode"),
            @Mapping(source = "shipToName", target = "shipToName")
    })
    VirtualInventoryHeader toEntity(VirtualInventoryHeaderDTO virtualInventoryHeaderDTO);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "coveredEntityBillToId", target = "coveredEntityBillToId"),
            @Mapping(source = "pharmacyBillToId", target = "pharmacyBillToId"),
            @Mapping(source = "pharmacyShipToId", target = "pharmacyShipToId"),
            @Mapping(source = "centralFillShipToId", target = "centralFillShipToId"),
            @Mapping(source = "centralFillBillToId", target = "centralFillBillToId"),
            @Mapping(source = "createdDate", target = "createdDate"),
            @Mapping(source = "createdBy", target = "createdBy"),
            @Mapping(source = "lastModifiedDate", target="lastModifiedDate"),
            @Mapping(source = "billToTypeCode", target = "billToTypeCode"),
            @Mapping(source = "billToName", target = "billToName"),
            @Mapping(source = "shipToTypeCode", target = "shipToTypeCode"),
            @Mapping(source = "shipToName", target = "shipToName")
    })
    VirtualInventoryHeaderDTO toDto(VirtualInventoryHeader virtualInventoryHeader);
}
