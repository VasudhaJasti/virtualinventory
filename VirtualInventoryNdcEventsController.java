package com.capturerx.cumulus4.virtualinventory.controllers;

import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryEventsViewDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryManualAdjustmentDTO;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcEventDTO;
import com.capturerx.cumulus4.virtualinventory.errors.*;
import com.capturerx.cumulus4.virtualinventory.messages.AcknowledgementMessageDTO;
import com.capturerx.cumulus4.virtualinventory.messages.PurchaseOrderMessage;
import com.capturerx.cumulus4.virtualinventory.messages.SummaryMessage;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdcEvent;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryNdcEventsService;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryNdcDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@Api(value = "/cumulus/v1/events", description = "Operations related to Virtual Inventory Ndc Events")
@RequestMapping(value = "/cumulus/v1/vi-events")
@CrossOrigin(origins = "*")
public class VirtualInventoryNdcEventsController extends BaseController{

    @Autowired
    private VirtualInventoryNdcEventsService virtualInventoryNdcEventsService;

    private static final String HEADER_ID = "id";
    private static final String HEADER_DOES_NOT_EXISTS = "Virtual Inventory Header does not exists";
    private static final String BILL_TO_ID_NOT_FOUND = "bill to id does not exist";
    private static final String NDC = "ndc";
    private static final String NDC_NOT_FOUND = "ndc does not exist";
    private static final String BILL_TO_ID = "billToId";
    private static final String SHIP_TO_ID = "shipToId";
    private static final String SHIP_TO_ID_NOT_FOUND = "ship to id does not exist";

    @PostMapping(value = "/manual", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory Ndc Events", notes = "Create new Virtual Inventory Ndc Events System by NdcId and Ndc", response = VirtualInventoryNdcEventDTO.class)
    public ResponseEntity<VirtualInventoryNdcEventDTO> add(
            @Valid
            @RequestBody
            @ApiParam(required = true, name = "virtualInventoryNdcEventDTO", value = "Create Virtual Inventory Ndc Event")
                    VirtualInventoryEventsViewDTO virtualInventoryNdcEventDTO) {
    VirtualInventoryNdcEventDTO event = virtualInventoryNdcEventsService.add(virtualInventoryNdcEventDTO);
    return generateResourceCreatedResponse(event.getId());
    }

    @PostMapping()
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory Ndc Events", notes = "Create new Virtual Inventory Ndc Events System by NdcId and Ndc", response = VirtualInventoryNdcEventDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> storeMessage(
            @Valid
            @RequestBody
            SummaryMessage summaryMessage){
        FieldExceptionBuilder fieldExceptionBuilder = new FieldExceptionBuilder();
        if(summaryMessage.getShipToId()== null || summaryMessage.getBillToId() == null)
            fieldExceptionBuilder.addFieldError(HEADER_ID,summaryMessage.getBillToId()+ HEADER_DOES_NOT_EXISTS).throwExceptionIfAny();
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEventDTO = virtualInventoryNdcEventsService.postMessage(summaryMessage);
        return generateResourceGetAllResponseOK(virtualInventoryNdcEventDTO);
    }

    @PostMapping(path = "/order")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory Ndc Events for Orders", notes = "Create new vi drug events for orders", response = VirtualInventoryNdcEventDTO[].class)
    public ResponseEntity<List<VirtualInventoryNdcEvent>> storeOrders(
            @Valid
            @RequestBody
                    PurchaseOrderMessage purchaseOrderMessage){
        FieldExceptionBuilder fieldExceptionBuilder = new FieldExceptionBuilder();
        if(purchaseOrderMessage.getBillTo() == null)
            fieldExceptionBuilder.addFieldError(BILL_TO_ID, String.format(BILL_TO_ID_NOT_FOUND, purchaseOrderMessage.getBillTo())).throwExceptionIfAny();
        if(purchaseOrderMessage.getShipTo() == null)
            fieldExceptionBuilder.addFieldError(SHIP_TO_ID, String.format(SHIP_TO_ID_NOT_FOUND, purchaseOrderMessage.getShipTo())).throwExceptionIfAny();
        if(purchaseOrderMessage.getNdc() == null || purchaseOrderMessage.getNdc().isEmpty())
            fieldExceptionBuilder.addFieldError(NDC, String.format(NDC_NOT_FOUND, purchaseOrderMessage.getNdc())).throwExceptionIfAny();
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEvents = virtualInventoryNdcEventsService.saveOrders(purchaseOrderMessage);
        return generateResourceGetAllResponseOK(virtualInventoryNdcEvents);
    }

    @PostMapping(path ="/preexist")
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> storePreExist(
            @Valid
            @RequestBody
            VirtualInventoryManualAdjustmentDTO virtualInventoryManualAdjustmentDTO){
        FieldExceptionBuilder fieldExceptionBuilder = new FieldExceptionBuilder();
        if(virtualInventoryManualAdjustmentDTO.getBillToId() == null)
            fieldExceptionBuilder.addFieldError(BILL_TO_ID, String.format(BILL_TO_ID_NOT_FOUND, virtualInventoryManualAdjustmentDTO.getBillToId())).throwExceptionIfAny();
        if(virtualInventoryManualAdjustmentDTO.getShipToId() == null)
            fieldExceptionBuilder.addFieldError(SHIP_TO_ID, String.format(SHIP_TO_ID_NOT_FOUND, virtualInventoryManualAdjustmentDTO.getShipToId())).throwExceptionIfAny();
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEventDTOS1 = virtualInventoryNdcEventsService.postPreExist(virtualInventoryManualAdjustmentDTO);
        return ResponseEntity.status(HttpStatus.OK).body(virtualInventoryNdcEventDTOS1);

    }
    @PostMapping(path = "/invoice")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory Ndc Events for Orders", notes = "Create new vi drug events for orders", response = VirtualInventoryNdcEventDTO[].class)
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> storeInvoice(
            @Valid
            @RequestBody
                    AcknowledgementMessageDTO messageDTO){
        FieldExceptionBuilder fieldExceptionBuilder = new FieldExceptionBuilder();
        if(messageDTO.getBillToId() == null)
            fieldExceptionBuilder.addFieldError(BILL_TO_ID, String.format(BILL_TO_ID_NOT_FOUND, messageDTO.getBillToId())).throwExceptionIfAny();
        if(messageDTO.getShipToId() == null)
            fieldExceptionBuilder.addFieldError(SHIP_TO_ID, String.format(SHIP_TO_ID_NOT_FOUND, messageDTO.getShipToId())).throwExceptionIfAny();
        if(messageDTO.getNdc() == null || messageDTO.getNdc().isEmpty())
            fieldExceptionBuilder.addFieldError(NDC, String.format(NDC_NOT_FOUND, messageDTO.getNdc())).throwExceptionIfAny();
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEvents = virtualInventoryNdcEventsService.invoiceLogic(messageDTO);
        return generateResourceGetAllResponseOK(virtualInventoryNdcEvents);
    }

    @PostMapping(path = "/acknowledgment")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory Ndc Events for Orders", notes = "Create new vi drug events for orders", response = VirtualInventoryNdcEventDTO[].class)
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> storeAcknowledgment(
            @Valid
            @RequestBody
                    AcknowledgementMessageDTO messageDTO){
        FieldExceptionBuilder fieldExceptionBuilder = new FieldExceptionBuilder();
        if(messageDTO.getBillToId() == null)
            fieldExceptionBuilder.addFieldError(BILL_TO_ID, String.format(BILL_TO_ID_NOT_FOUND, messageDTO.getBillToId())).throwExceptionIfAny();
        if(messageDTO.getShipToId() == null)
            fieldExceptionBuilder.addFieldError(SHIP_TO_ID, String.format(SHIP_TO_ID_NOT_FOUND, messageDTO.getShipToId())).throwExceptionIfAny();
        if(messageDTO.getNdc() == null || messageDTO.getNdc().isEmpty())
            fieldExceptionBuilder.addFieldError(NDC, String.format(NDC_NOT_FOUND, messageDTO.getNdc())).throwExceptionIfAny();
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEvents = virtualInventoryNdcEventsService.createAcknowledgementEvents(messageDTO);
        return generateResourceGetAllResponseOK(virtualInventoryNdcEvents);
    }

    @GetMapping()
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get all Virtual Inventories Events", notes = "Get all virtual inventory events")
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> getAll() {
        List<VirtualInventoryNdcEventDTO> ndcEvents = virtualInventoryNdcEventsService.getAll();
        if(ndcEvents == null || ndcEvents.isEmpty())
            return generateResourceGetAllNoContentResponse(ndcEvents);
        else
            return generateResourceGetAllResponseOK(ndcEvents);
    }

    @PutMapping()
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    public ResponseEntity<VirtualInventoryNdcEventDTO> updateEvent(
            @Valid
            @RequestBody VirtualInventoryNdcEvent virtualInventoryNdcEvent){
        VirtualInventoryNdcEventDTO ndcEventsDTO = virtualInventoryNdcEventsService.update(virtualInventoryNdcEvent);
        return ResponseEntity.status(HttpStatus.OK).body(ndcEventsDTO);
    }

    @GetMapping(value = "/{ndcId}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get Virtual Inventory by NdcId", notes = "Get Virtual Inventory Ndc System based on NdcId", response = VirtualInventoryNdcEventDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> getEventByNdcId(
            @Valid
            @PathVariable("ndcId")
            @ApiParam(required = true, name="ndcId", value = "Virtual Inventory Ndc Unique Identifier")
            UUID ndcId){
        List<VirtualInventoryNdcEventDTO> eventsDTOS= virtualInventoryNdcEventsService.getByNdcId(ndcId);
        if(eventsDTOS == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_FOUND_FOR_NDC, ndcId));
        return generateResourceGetAllResponseOK(eventsDTOS);
    }

    @PutMapping(path = "/{ndcId}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Update virtual Inventory units", notes = "update virtual inventory units for events of each ndc", response = VirtualInventoryNdcEventDTO.class)
    public ResponseEntity<VirtualInventoryNdcEventDTO> updateEventsbyNdcId(
            @Valid
            @RequestBody
            @ApiParam(required = true, name = "virtualInventoryNdcEvent", value = "Create Virtual Inventory Ndc Event")
                    VirtualInventoryNdcEvent virtualInventoryNdcEvent,
            @Valid
            @PathVariable("ndcId")
            @ApiParam(required = true, name="ndcId", value = "Virtual Inventory Ndc Unique Identifier")
                    UUID ndcId){
        VirtualInventoryNdcEventDTO ndcEvents = virtualInventoryNdcEventsService.updateUnitsByNdcId(virtualInventoryNdcEvent, ndcId);
        return ResponseEntity.status(HttpStatus.OK).body(ndcEvents);
    }

    @GetMapping(value = "/{ndcId}/{ndc}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get Virtual Inventory by NdcId and Ndc", notes = "Get Virtual Inventory Ndc System based on NdcId and National Drug Code", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> getEventsByNdcIdAndNdc(
            @Valid
            @PathVariable("ndcId")
            @ApiParam(required = true, name="ndcId", value = "Virtual Inventory Ndc Unique Identifier")
            UUID ndcId,
            @Valid
            @PathVariable("ndc")
            @ApiParam(required = true, name = "ndc", value = "Virtual Inventory Ndc National Drug Code")
            String ndc) {
        List<VirtualInventoryNdcEventDTO> ndcEventsDTOS = virtualInventoryNdcEventsService.getByNdcIdAndNdc(ndcId, ndc);
        if(ndcEventsDTOS == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_FOUND_FOR_NDC, ndcId));
        return generateResourceGetAllResponseOK(ndcEventsDTOS);
    }

    @PutMapping(value = "/ndc")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> updateEventsForNdc(
            @RequestBody
            List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEvents){
        List<VirtualInventoryNdcEventDTO> ndcEventsDTOS = virtualInventoryNdcEventsService.updateList(virtualInventoryNdcEvents);
        if(ndcEventsDTOS == null || ndcEventsDTOS.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ndcEventsDTOS);
        else
            return ResponseEntity.status(HttpStatus.OK).body(ndcEventsDTOS);
    }

    @GetMapping(value = "/dispenses/{dispenseSetId}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    public ResponseEntity<List<VirtualInventoryNdcEventDTO>> getAllEventsByDispenseSetId(
            @Valid
            @PathVariable("dispenseSetId")
            UUID dispenseSetId){
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEventDTOS = virtualInventoryNdcEventsService.getAllByDispenseSetId(dispenseSetId);
        if(virtualInventoryNdcEventDTOS == null || virtualInventoryNdcEventDTOS.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryNdcEventDTOS);
        else
            return generateResourceGetAllResponseOK(virtualInventoryNdcEventDTOS);
    }
}
