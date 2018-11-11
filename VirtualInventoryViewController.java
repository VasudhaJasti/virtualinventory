package com.capturerx.cumulus4.virtualinventory.controllers;


import com.capturerx.cumulus4.virtualinventory.dtos.*;
import com.capturerx.cumulus4.virtualinventory.models.ReplenishmentVirtualInv;
import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventsView;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryViewService;
import io.swagger.annotations.ApiParam;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/cumulus/v1")
@CrossOrigin(origins = "*")
public class VirtualInventoryViewController extends BaseController{

    @Autowired
    private VirtualInventoryViewService virtualInventoryViewService;

    @PostMapping(value = "/getallndcs", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_CANREADVIRTUALINVENTORY.toString())")
    public List<VirtualInventoryNdcUiViewDTO> getAllNdcs(
            @RequestBody
            VirtualInventoryNdcUiViewDTO virtualInventoryNdcUiViewDTO){
        return virtualInventoryViewService.getNdcsByBillToAndShipTo(virtualInventoryNdcUiViewDTO);
    }

    @GetMapping(path = "/getevents")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_CANREADVIRTUALINVENTORY.toString())")
    public List<VirtualInventoryEventsView> getAllEvents(){
        return virtualInventoryViewService.getAllEvents();
    }

    @GetMapping(path = "/getevents/{ndcId}/{eventAsOfDate}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_CANREADVIRTUALINVENTORY.toString())")
    public ResponseEntity<List<VirtualInventoryEventsView>> getEventsbyNdcId(
            @Valid
            @PathVariable("ndcId")
            @ApiParam(required = true, name = "ndcId", value = "events identifier by ndc")
                    UUID ndcId,
            @Valid
            @PathVariable("eventAsOfDate")
                    String eventAsOfDate){
        DateTime dateTime = DateTime.parse(eventAsOfDate, DateTimeFormat.forPattern("yyyy-MM-dd"));
        List<VirtualInventoryEventsViewDTO> virtualInventoryEventsViewDTOS= virtualInventoryViewService.getEventsByNdcIdAndEventAsOfDate(ndcId, dateTime);
        if(virtualInventoryEventsViewDTOS == null || virtualInventoryEventsViewDTOS.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryEventsViewDTOS);
        else
            return generateResourceGetAllResponseOK(virtualInventoryEventsViewDTOS);
    }

    @GetMapping(path = "/billtodetails")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_CANREADVIRTUALINVENTORY.toString())")
    public ResponseEntity<List<VirtualInventoryBillToDTO>> getAllBillTo(){
        List<VirtualInventoryBillToDTO> virtualInventoryBillToDTOS= virtualInventoryViewService.getAllBillTo();
        if(virtualInventoryBillToDTOS == null || virtualInventoryBillToDTOS.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryBillToDTOS);
        else
            return generateResourceGetAllResponseOK(virtualInventoryBillToDTOS);
    }


    @GetMapping(path = "/shiptodetails")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_CANREADVIRTUALINVENTORY.toString())")
    public ResponseEntity<List<VirtualInventoryShipToDTO>> getAllShipTo(){
        List<VirtualInventoryShipToDTO> virtualInventoryShipToDTOS = virtualInventoryViewService.getAllShipTo();
        if(virtualInventoryShipToDTOS == null || virtualInventoryShipToDTOS.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryShipToDTOS);
        else
            return generateResourceGetAllResponseOK(virtualInventoryShipToDTOS);
    }

    @GetMapping(value = "/shiptoid/{shipToId}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    public ResponseEntity<List<VirtualInventoryNdcUiViewDTO>> getAllHeadersByShipToId(
            @Valid
            @PathVariable("shipToId")
                    UUID shipToId){
        List<VirtualInventoryNdcUiViewDTO> virtualInventoryHeaderDTOS = virtualInventoryViewService.getAllByShipToIdAndPackageQuantityGreaterThanZero(shipToId);
        if(virtualInventoryHeaderDTOS == null && virtualInventoryHeaderDTOS.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryHeaderDTOS);
        else
            return generateResourceGetAllResponseOK(virtualInventoryHeaderDTOS);
    }

    @GetMapping(path = "/contracts/headers/{shipToId}")
    public List<ReplenishmentVirtualInv> getContractsAndHeadersByShipToId(
            @Valid
            @PathVariable("shipToId")
                    UUID shipToId){
        return virtualInventoryViewService.getContractsAndHeaders(shipToId);
    }

}
