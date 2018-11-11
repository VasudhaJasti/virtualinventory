package com.capturerx.cumulus4.virtualinventory.services;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface VirtualInventoryNdcService {

    VirtualInventoryNdcDTO add(VirtualInventoryNdcDTO virtualInventoryNdcDTO, UUID headerId);
    List<VirtualInventoryNdcDTO> getAll();
    List<VirtualInventoryNdcDTO> getNdcByHeaderId(UUID headerId);
    VirtualInventoryNdcDTO getNdcbyHeaderIdAndId(UUID headerId, UUID id);
    List<VirtualInventoryNdcDTO> getByHeaderIdAndNdc(UUID id,String ndc);
    List<VirtualInventoryNdcDTO> getByHeaderIdAndDescription(UUID id,String drugDescription);

    List<VirtualInventoryNdcDTO> updatePackageQuantity(List<VirtualInventoryNdcDTO> virtualInventoryNdc);

    List<VirtualInventoryNdcDTO> getNdcByHeaderIdAndPackageQuantitygreaterThanZero(UUID headerId);

}
