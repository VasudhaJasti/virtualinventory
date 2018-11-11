package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventType;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdc;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdcEvent;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {VirtualInventoryNdc.class, VirtualInventoryEventType.class})
public interface VirtualInventoryNdcEventsMapper {

    VirtualInventoryNdcEventsMapper INSTANCE= Mappers.getMapper(VirtualInventoryNdcEventsMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(target = "virtualInventoryNdc.id", source = "virtualInventoryNdcId"),
            @Mapping(target = "virtualInventoryEventType.id", source = "virtualInventoryEventsTypeId"),
            @Mapping(target = "virtualInventoryEventType.eventStatus", source = "virtualInventoryEventsTypeEventStatus"),
            @Mapping(source = "dispenseId", target = "dispenseId"),
            @Mapping(source = "ndc", target = "ndc"),
            @Mapping(source = "orderId", target = "orderId"),
            @Mapping(source = "quantity", target = "quantity"),
            @Mapping(source = "eventDate", target = "eventDate"),
            @Mapping(source = "orderCount", target = "orderCount"),
            @Mapping(source = "orderFlag", target = "orderFlag"),
            @Mapping(source = "orderNumber", target = "orderNumber"),
            @Mapping(source = "virtualInventoryUnits", target = "virtualInventoryUnits"),
            @Mapping(source = "reason", target = "reason"),
            @Mapping(source = "createdDate", target = "createdDate"),
            @Mapping(source = "createdBy", target = "createdBy"),
            @Mapping(source = "lastModifiedDate", target="lastModifiedDate"),
            @Mapping(source = "contractId", target = "contractId"),
            @Mapping(source = "payorType", target = "payorType"),
            @Mapping(source = "approvedDate", target = "approvedDate")

    })
    VirtualInventoryNdcEvent toEntity(VirtualInventoryNdcEventDTO virtualInventoryNdcEventDTO);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "virtualInventoryNdc.id", target = "virtualInventoryNdcId"),
            @Mapping(source = "virtualInventoryEventType.id", target = "virtualInventoryEventsTypeId"),
            @Mapping(source = "virtualInventoryEventType.eventStatus", target = "virtualInventoryEventsTypeEventStatus"),
            @Mapping(source = "dispenseId", target = "dispenseId"),
            @Mapping(source = "orderId", target = "orderId"),
            @Mapping(source = "quantity", target = "quantity"),
            @Mapping(source = "eventDate", target = "eventDate"),
            @Mapping(source = "orderCount", target = "orderCount"),
            @Mapping(source = "orderFlag", target = "orderFlag"),
            @Mapping(source = "orderNumber", target = "orderNumber"),
            @Mapping(source = "virtualInventoryUnits", target = "virtualInventoryUnits"),
            @Mapping(source = "reason", target = "reason"),
            @Mapping(source = "createdDate", target = "createdDate"),
            @Mapping(source = "createdBy", target = "createdBy"),
            @Mapping(source = "lastModifiedDate", target="lastModifiedDate"),
            @Mapping(source = "contractId", target = "contractId"),
            @Mapping(source = "payorType", target = "payorType"),
            @Mapping(source = "approvedDate", target = "approvedDate")


    })
    VirtualInventoryNdcEventDTO toDto(VirtualInventoryNdcEvent virtualInventoryNdcEvent);
}