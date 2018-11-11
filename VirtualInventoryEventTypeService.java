package com.capturerx.cumulus4.virtualinventory.services;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventTypeDTO;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventType;
import org.springframework.stereotype.Service;

@Service
public interface VirtualInventoryEventTypeService {

    VirtualInventoryEventTypeDTO add(VirtualInventoryEventTypeDTO virtualInventoryEventTypeDTO);

    VirtualInventoryEventType getVirtualInventoryEventTypeByKey(String key);
}
