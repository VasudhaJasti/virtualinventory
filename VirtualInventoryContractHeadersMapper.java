package com.capturerx.cumulus4.virtualinventory.services.mappers;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryContractHeadersDTO;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryContractHeaders;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {VirtualInventoryHeader.class})
public interface VirtualInventoryContractHeadersMapper {

    VirtualInventoryContractHeadersMapper INSTANCE = Mappers.getMapper(VirtualInventoryContractHeadersMapper.class);

    @Mappings({

            @Mapping(target = "virtualInventoryHeader.id", source = "virtualInventoryHeaderId"),

    })
    VirtualInventoryContractHeaders toEntity(VirtualInventoryContractHeadersDTO virtualInventoryContractHeadersDTO);

    @Mappings({

            @Mapping(source = "virtualInventoryHeader.id", target = "virtualInventoryHeaderId"),

    })
    VirtualInventoryContractHeadersDTO toDto(VirtualInventoryContractHeaders virtualInventoryContractHeaders);
}
