package com.capturerx.cumulus4.virtualinventory.impls;

import com.capturerx.cumulus4.virtualinventory.errors.ErrorConstants;
import com.capturerx.cumulus4.virtualinventory.errors.InternalServerException;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdc;
import com.capturerx.cumulus4.virtualinventory.repositories.VirtualInventoryNdcRepository;
import com.capturerx.cumulus4.virtualinventory.repositories.VirtualInventoryRepository;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryNdcService;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcDTO;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryNdcMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class VirtualInventoryNdcServiceImpl implements VirtualInventoryNdcService {

    private final static Logger logger = Logger.getLogger(VirtualInventoryNdcServiceImpl.class.getName());

    @Autowired
    private final VirtualInventoryNdcRepository virtualInventoryNdcRepository;
    private final VirtualInventoryNdcMapper virtualInventoryNdcMapper;

    private final VirtualInventoryRepository virtualInventoryRepository;

    public VirtualInventoryNdcServiceImpl(VirtualInventoryNdcRepository virtualInventoryNdcRepository, VirtualInventoryNdcMapper virtualInventoryNdcMapper, VirtualInventoryRepository virtualInventoryRepository){
        this.virtualInventoryNdcRepository = virtualInventoryNdcRepository;
        this.virtualInventoryNdcMapper = virtualInventoryNdcMapper;

        this.virtualInventoryRepository=virtualInventoryRepository;
    }

    public VirtualInventoryNdcDTO add(VirtualInventoryNdcDTO virtualInventoryNdcDTO, UUID virtualInventoryHeaderId){
        VirtualInventoryNdc virtualInventoryNdc=virtualInventoryNdcMapper.toEntity(virtualInventoryNdcDTO);

        VirtualInventoryHeader virtualInventoryHeader=virtualInventoryRepository.findOne(virtualInventoryHeaderId);
        logger.info(""+virtualInventoryHeader.getId());
        virtualInventoryNdc.setVirtualInventoryHeader(virtualInventoryHeader);

        //send a request to redis database to get drugDescription,package size and ndc data
        virtualInventoryNdc.setNdc(virtualInventoryNdcDTO.getNdc());
        virtualInventoryNdc.setDrugDescription(virtualInventoryNdcDTO.getDrugDescription());
        virtualInventoryNdc.setPackageSize(virtualInventoryNdcDTO.getPackageSize());

        virtualInventoryNdc.setPackageQuantity(virtualInventoryNdcDTO.getPackageQuantity());
        virtualInventoryNdc.setLastCalculatedDate(DateTime.now(DateTimeZone.UTC));
        virtualInventoryNdc.setCreatedDate(DateTime.now(DateTimeZone.UTC));
        virtualInventoryNdc.setCreatedBy(virtualInventoryNdcDTO.getCreatedBy());
        virtualInventoryNdc = virtualInventoryNdcRepository.save(virtualInventoryNdc);
        return virtualInventoryNdcMapper.toDto(virtualInventoryNdc);
    }

    public List<VirtualInventoryNdcDTO> getAll(){
        return virtualInventoryNdcRepository.findAll().stream().map(virtualInventoryNdcMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<VirtualInventoryNdcDTO> getNdcByHeaderId(UUID virtualInventoryHeaderId){

        return virtualInventoryNdcRepository.findAllByVirtualInventoryHeaderId(virtualInventoryHeaderId).stream()
                .map(virtualInventoryNdcMapper::toDto).collect(Collectors.toCollection(LinkedList::new));

    }

    public VirtualInventoryNdcDTO getNdcbyHeaderIdAndId(UUID virtualInventoryHeaderId, UUID id){
        VirtualInventoryNdc virtualInventoryNdc = virtualInventoryNdcRepository.getByVirtualInventoryHeaderIdAndAndId(virtualInventoryHeaderId, id);
        return virtualInventoryNdcMapper.toDto(virtualInventoryNdc); }

    public List<VirtualInventoryNdcDTO> getByHeaderIdAndNdc(UUID headerId, String ndc){
        return virtualInventoryNdcRepository.getAllByVirtualInventoryHeaderIdAndNdc(headerId, ndc).stream()
                .map(virtualInventoryNdcMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public List<VirtualInventoryNdcDTO> getByHeaderIdAndDescription(UUID headerId, String drugDescription){
        return virtualInventoryNdcRepository.getVirtualInventoryNdcsByVirtualInventoryHeaderIdAndDrugDescription(headerId, drugDescription).stream()
                .map(virtualInventoryNdcMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public List<VirtualInventoryNdcDTO> updatePackageQuantity(List<VirtualInventoryNdcDTO> virtualInventoryNdcDTO){
        List<VirtualInventoryNdc> ndcs = virtualInventoryNdcDTO.stream().map(virtualInventoryNdcMapper :: toEntity).collect(Collectors.toList());
        List<VirtualInventoryNdc> virtualInventoryNdcs = virtualInventoryNdcRepository.save(ndcs);
        if(virtualInventoryNdcs == null || virtualInventoryNdcs.isEmpty()) throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDCS_NOT_FOUND, virtualInventoryNdcs));
        return virtualInventoryNdcs.stream().map(virtualInventoryNdcMapper :: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryNdcDTO> getNdcByHeaderIdAndPackageQuantitygreaterThanZero(UUID headerId) {
        List<VirtualInventoryNdc> virtualInventoryNdcs = virtualInventoryNdcRepository.findAllByVirtualInventoryHeaderIdAndPackageQuantityGreaterThan(headerId,0);
        if(virtualInventoryNdcs == null) return null;
        return virtualInventoryNdcs.stream().map(virtualInventoryNdcMapper :: toDto).collect(Collectors.toList());
    }
}
