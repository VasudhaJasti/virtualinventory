package com.capturerx.cumulus4.virtualinventory.impls;

import com.capturerx.cumulus4.virtualinventory.errors.ErrorConstants;
import com.capturerx.cumulus4.virtualinventory.errors.InternalServerException;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import com.capturerx.cumulus4.virtualinventory.repositories.VirtualInventoryRepository;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryHeaderDTO;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryHeaderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
@Transactional
@Configuration
public class VirtualInventoryServiceImpl {

    Logger logger = Logger.getLogger(VirtualInventoryServiceImpl.class.getName());

    @Autowired
    private final VirtualInventoryRepository virtualInventoryRepository;
    private final VirtualInventoryHeaderMapper virtualInventoryHeaderMapper;

    public VirtualInventoryServiceImpl(VirtualInventoryRepository virtualInventoryRepository, VirtualInventoryHeaderMapper virtualInventoryHeaderMapper){
        this.virtualInventoryRepository = virtualInventoryRepository;
        this.virtualInventoryHeaderMapper = virtualInventoryHeaderMapper;
    }

    public List<VirtualInventoryHeaderDTO> add(List<VirtualInventoryHeaderDTO> headerDTOS){
        List<VirtualInventoryHeader> virtualInventoryHeaders = headerDTOS.stream().map(virtualInventoryHeaderMapper :: toEntity).collect(Collectors.toList());
        List<VirtualInventoryHeader> virtualInventoryHeaderList=virtualInventoryRepository.save(virtualInventoryHeaders);
        if(virtualInventoryHeaderList == null || virtualInventoryHeaderList.isEmpty()) throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_HEADERS_SAVE, virtualInventoryHeaderList));
        return virtualInventoryHeaderList.stream().map(virtualInventoryHeaderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<VirtualInventoryHeaderDTO> getAll(){
        return virtualInventoryRepository.findAll().stream().map(virtualInventoryHeaderMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<VirtualInventoryHeaderDTO> getAllByPharmacyShipToId(UUID pharmacyShipToId){
        return virtualInventoryRepository.findAllByPharmacyShipToId(pharmacyShipToId).stream()
                .map(virtualInventoryHeaderMapper ::toDto).collect(Collectors.toList());
    }

    public VirtualInventoryHeaderDTO update(VirtualInventoryHeaderDTO virtualInventoryHeaderDTO){
        VirtualInventoryHeader virtualInventoryHeader = virtualInventoryRepository.getOne(virtualInventoryHeaderDTO.getId());
        virtualInventoryRepository.flush();
        return virtualInventoryHeaderMapper.toDto(virtualInventoryHeader);
    }

    public VirtualInventoryHeaderDTO getById(UUID id){
        VirtualInventoryHeader virtualInventoryHeader = virtualInventoryRepository.findOne(id);
        if(virtualInventoryHeader == null) return null;
        return virtualInventoryHeaderMapper.toDto(virtualInventoryHeader);
    }

    public void remove(UUID id){
        VirtualInventoryHeader virtualInventoryHeader = virtualInventoryRepository.findOne(id);
        if(virtualInventoryHeader == null) throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_HEADER_NOT_FOUND, virtualInventoryHeader));
        virtualInventoryRepository.delete(virtualInventoryHeader);
    }

}
