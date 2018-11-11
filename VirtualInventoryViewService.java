package com.capturerx.cumulus4.virtualinventory.services;


import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryBillToDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventsViewDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcUiViewDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryShipToDTO;
import com.capturerx.cumulus4.virtualinventory.models.ContractsVirtualInventory;
import com.capturerx.cumulus4.virtualinventory.models.ReplenishmentVirtualInv;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventsView;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface VirtualInventoryViewService {

    List<VirtualInventoryNdcUiViewDTO> getAllNdcs(UUID billToId, UUID shipToId);

    List<VirtualInventoryEventsView> getAllEvents();


    List<VirtualInventoryEventsViewDTO> getEventsByNdcIdAndEventAsOfDate(UUID ndcId, DateTime eventAsOfDate);

    List<VirtualInventoryBillToDTO> getAllBillTo();

    List<VirtualInventoryNdcUiViewDTO> getNdcsByBillToAndShipTo(VirtualInventoryNdcUiViewDTO virtualInventoryNdcUiViewDTO);

    List<VirtualInventoryShipToDTO> getAllShipTo();

    List<VirtualInventoryNdcUiViewDTO> getAllByShipToIdAndPackageQuantityGreaterThanZero(UUID shipToId);

    List<ContractsVirtualInventory> getContractsByShipToId(UUID shipToId);

    List<ReplenishmentVirtualInv> getContractsAndHeaders(UUID shipToId);

}