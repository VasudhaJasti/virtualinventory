package com.capturerx.cumulus4.virtualinventory.services;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeaderUpdate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface VirtualInventoryHeaderUpdateService {

    VirtualInventoryHeaderUpdate add(VirtualInventoryHeaderUpdate virtualInventoryHeaderUpdate);
    List<VirtualInventoryHeaderUpdate> getAllForUpdate();
    VirtualInventoryHeaderUpdate getByLastUpdatedDate(UUID id);
    List<VirtualInventoryHeaderUpdate> update(List<VirtualInventoryHeaderUpdate> virtualInventoryHeaderUpdates);

}
