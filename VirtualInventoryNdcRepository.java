package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualInventoryNdcRepository extends JpaRepository<VirtualInventoryNdc, UUID>{
    List<VirtualInventoryNdc> findAllByVirtualInventoryHeaderId(UUID headerId);
    VirtualInventoryNdc getByVirtualInventoryHeaderIdAndAndId(UUID headerId, UUID id);
    List<VirtualInventoryNdc> getAllByVirtualInventoryHeaderIdAndNdc(UUID headerId, String ndc);
    List<VirtualInventoryNdc> getVirtualInventoryNdcsByVirtualInventoryHeaderIdAndDrugDescription(UUID headerId, String drugDescription);
    VirtualInventoryNdc findById(UUID id);

    VirtualInventoryNdc findByNdcAndVirtualInventoryHeader(String ndc, VirtualInventoryHeader header);

    Boolean existsByNdcAndVirtualInventoryHeader(String ndc, VirtualInventoryHeader header);

    List<VirtualInventoryNdc> findAllByVirtualInventoryHeaderIdAndPackageQuantityGreaterThan(UUID virtualInventoryHeader, int packageQuantity);
}
