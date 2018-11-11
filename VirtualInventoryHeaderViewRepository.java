package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeaderView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualInventoryHeaderViewRepository extends JpaRepository<VirtualInventoryHeaderView, UUID>{

    List<VirtualInventoryHeaderView> findAllByShipToId(UUID shipToId);

}
