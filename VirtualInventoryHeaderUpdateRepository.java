package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeaderUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VirtualInventoryHeaderUpdateRepository extends JpaRepository<VirtualInventoryHeaderUpdate, UUID> {
}
