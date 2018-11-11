package com.capturerx.cumulus4.virtualinventory.impls;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventType;
import com.capturerx.cumulus4.virtualinventory.repositories.VirtualInventoryEventTypeRepository;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryEventTypeService;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventTypeDTO;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryEventTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VirtualInventoryEventTypeImpl implements VirtualInventoryEventTypeService {

    @Autowired
    private final VirtualInventoryEventTypeRepository virtualInventoryEventTypeRepository;

    private final VirtualInventoryEventTypeMapper virtualInventoryEventTypeMapper;

    HashMap<String, VirtualInventoryEventType> allEventTypes = new HashMap();

    public VirtualInventoryEventTypeImpl(VirtualInventoryEventTypeRepository virtualInventoryEventTypeRepository, VirtualInventoryEventTypeMapper virtualInventoryEventTypeMapper){
        this.virtualInventoryEventTypeRepository = virtualInventoryEventTypeRepository;
        this.virtualInventoryEventTypeMapper = virtualInventoryEventTypeMapper;
    }

    public VirtualInventoryEventTypeDTO add(VirtualInventoryEventTypeDTO virtualInventoryEventTypeDTO){
        VirtualInventoryEventType virtualInventoryEventType1 = virtualInventoryEventTypeMapper.toEntity(virtualInventoryEventTypeDTO);
        virtualInventoryEventTypeRepository.save(virtualInventoryEventType1);
        return virtualInventoryEventTypeMapper.toDto(virtualInventoryEventType1);
    }

    public HashMap<String, VirtualInventoryEventType> getAll(){
        List<VirtualInventoryEventType> virtualInventoryEventTypes = virtualInventoryEventTypeRepository.findAll();
        for(VirtualInventoryEventType virtualInventoryEventType: virtualInventoryEventTypes){
            allEventTypes.put(virtualInventoryEventType.getEventStatus(), virtualInventoryEventType);
        }
        return allEventTypes;
    }

    public VirtualInventoryEventType getVirtualInventoryEventTypeByKey(String key){
        HashMap<String, VirtualInventoryEventType> hashMap = getAll();
        return hashMap.get(key);
    }
}
