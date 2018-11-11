package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualInventoryRepository extends JpaRepository<VirtualInventoryHeader, UUID> {

    Boolean existsByCoveredEntityBillToIdAndPharmacyShipToId(UUID coveredEntityBillToId, UUID pharmacyShipToId);

    VirtualInventoryHeader findByCoveredEntityBillToIdAndPharmacyShipToId(UUID coveredEntityBillToId, UUID pharmacyShipToId);

    List<VirtualInventoryHeader> findAllByPharmacyShipToId(UUID pharmacyShipToId);

    VirtualInventoryHeader findByCoveredEntityBillToIdAndCentralFillShipToId(UUID coveredEntityBillToId, UUID CentralFillShipToId);

}