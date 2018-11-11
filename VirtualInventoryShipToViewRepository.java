package com.capturerx.cumulus4.virtualinventory.repositories;


import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryShipToView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VirtualInventoryShipToViewRepository extends JpaRepository<VirtualInventoryShipToView, UUID> {

    @Query("select vist from VirtualInventoryShipToView vist order by vist.shipToName asc ")
    List<VirtualInventoryShipToView> findAllOrderByShipToName();
}
