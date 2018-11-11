package com.capturerx.cumulus4.virtualinventory.services;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventsViewDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryManualAdjustmentDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcEventDTO;
import com.capturerx.cumulus4.virtualinventory.messages.AcknowledgementMessageDTO;
import com.capturerx.cumulus4.virtualinventory.messages.PurchaseOrderMessage;
import com.capturerx.cumulus4.virtualinventory.messages.SummaryMessage;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdcEvent;

import java.util.List;
import java.util.UUID;

public interface VirtualInventoryNdcEventsService {
    VirtualInventoryNdcEventDTO add(VirtualInventoryEventsViewDTO virtualInventoryNdcEventDTO);
    List<VirtualInventoryNdcEventDTO> getByNdcId(UUID ndcId);
    List<VirtualInventoryNdcEventDTO> getByNdcIdAndNdc(UUID ndcId, String ndc);

    List<VirtualInventoryNdcEventDTO> getAll();
    VirtualInventoryNdcEventDTO update(VirtualInventoryNdcEvent virtualInventoryNdcEvent);

    VirtualInventoryNdcEventDTO updateUnitsByNdcId(VirtualInventoryNdcEvent virtualInventoryNdcEvent, UUID ndcID);

    List<VirtualInventoryNdcEventDTO> updateList(List<VirtualInventoryNdcEventDTO> ndcEvents);

    List<VirtualInventoryNdcEventDTO> postMessage(SummaryMessage summaryMessage);

    List<VirtualInventoryNdcEventDTO> saveOrders(PurchaseOrderMessage purchaseOrderMessage);

    Boolean existsByBillToAndShipTo(UUID coveredEntityBillToId, UUID pharmacyShipToId);

    VirtualInventoryNdcEvent getByDispenseIdAndEventStatus(UUID dispenseId, String eventStatus);

    List<VirtualInventoryNdcEvent> getByDispenseId(UUID dispenseId);

    List<VirtualInventoryNdcEventDTO> invoiceLogic(AcknowledgementMessageDTO messageDTO);

    List<VirtualInventoryNdcEvent> getSortedEvents(List<VirtualInventoryNdcEvent> approvalEvents, List<VirtualInventoryNdcEvent> urEvent);

//    List<VirtualInventoryNdcEventDTO> createAcknowledgementEvents(SummaryMessage message);

    List<VirtualInventoryNdcEventDTO> postPreExist(VirtualInventoryManualAdjustmentDTO virtualInventoryManualAdjustmentDTOS);

    List<VirtualInventoryNdcEvent> createCreditMemoEvents(AcknowledgementMessageDTO messageDTO);

    List<VirtualInventoryNdcEventDTO> createAcknowledgementEvents(AcknowledgementMessageDTO messageDTO);

    List<VirtualInventoryNdcEventDTO> getAllByDispenseSetId(UUID dispenseSetId);
}