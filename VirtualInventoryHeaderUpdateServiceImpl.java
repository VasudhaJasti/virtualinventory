package com.capturerx.cumulus4.virtualinventory.impls;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeaderUpdate;
import com.capturerx.cumulus4.virtualinventory.repositories.VirtualInventoryHeaderUpdateRepository;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryHeaderUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VirtualInventoryHeaderUpdateServiceImpl implements VirtualInventoryHeaderUpdateService {

    @Autowired
    private final VirtualInventoryHeaderUpdateRepository virtualInventoryHeaderUpdateRepository;

    public VirtualInventoryHeaderUpdateServiceImpl(VirtualInventoryHeaderUpdateRepository virtualInventoryHeaderUpdateRepository) {
        this.virtualInventoryHeaderUpdateRepository = virtualInventoryHeaderUpdateRepository;
    }

    @Override
    public VirtualInventoryHeaderUpdate add(VirtualInventoryHeaderUpdate virtualInventoryHeaderUpdate) {
        virtualInventoryHeaderUpdateRepository.save(virtualInventoryHeaderUpdate);
        return virtualInventoryHeaderUpdate;
    }

    @Override
    public List<VirtualInventoryHeaderUpdate> getAllForUpdate() {
        return virtualInventoryHeaderUpdateRepository.findAll();
    }

    @Override
    public VirtualInventoryHeaderUpdate getByLastUpdatedDate(UUID id) {
        return virtualInventoryHeaderUpdateRepository.findOne(id);
    }

    @Override
    public List<VirtualInventoryHeaderUpdate> update(List<VirtualInventoryHeaderUpdate> virtualInventoryHeaderUpdates) {
        List<VirtualInventoryHeaderUpdate> headerUpdates = virtualInventoryHeaderUpdateRepository.save(virtualInventoryHeaderUpdates);
        return headerUpdates;
    }

}
