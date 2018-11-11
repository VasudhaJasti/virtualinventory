package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryBillToView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VirtualInventoryBillToViewRepository extends JpaRepository<VirtualInventoryBillToView, UUID> {

    @Query("select vibt from VirtualInventoryBillToView vibt order by vibt.billToName asc ")
    List<VirtualInventoryBillToView> findAllOrderByBillToName();

}
