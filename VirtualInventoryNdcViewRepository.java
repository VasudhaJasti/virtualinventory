package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdcUiView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualInventoryNdcViewRepository extends JpaRepository<VirtualInventoryNdcUiView,String>{

    List<VirtualInventoryNdcUiView> findAllByBillToIdAndShipToId(UUID billToId, UUID shipToId);

    List<VirtualInventoryNdcUiView> findAllByBillToIdAndShipToIdAndNdcIn(UUID billToId, UUID shipToId, List<String> ndcList);


    VirtualInventoryNdcUiView findByNdcAndBillToIdAndShipToId(String ndc, UUID billToId, UUID shipToId);

    List<VirtualInventoryNdcUiView> findAllByShipToIdAndPackageQuantityGreaterThan(UUID shipToId, int packageQuantity);
}
