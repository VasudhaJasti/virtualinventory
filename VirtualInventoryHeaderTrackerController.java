package com.capturerx.cumulus4.virtualinventory.controllers;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryHeaderUpdate;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryHeaderUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/cumulus/v1/vi/tracker", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class VirtualInventoryHeaderTrackerController extends BaseController{

    @Autowired
    private VirtualInventoryHeaderUpdateService virtualInventoryHeaderUpdateService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<VirtualInventoryHeaderUpdate> addHeaderTracker(
            @RequestBody VirtualInventoryHeaderUpdate virtualInventoryHeaderUpdate
    ){
        VirtualInventoryHeaderUpdate headerUpdate = virtualInventoryHeaderUpdateService.add(virtualInventoryHeaderUpdate);
        if(headerUpdate == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(headerUpdate);
        else
            return generateResourceCreatedResponse(headerUpdate.getId());
    }


    @GetMapping
    public ResponseEntity<List<VirtualInventoryHeaderUpdate>> getAll(){
        List<VirtualInventoryHeaderUpdate> virtualInventoryHeaderUpdates = virtualInventoryHeaderUpdateService.getAllForUpdate();
        if(virtualInventoryHeaderUpdates == null || virtualInventoryHeaderUpdates.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryHeaderUpdates);
        else
            return generateResourceGetAllResponseOK(virtualInventoryHeaderUpdates);
    }

    @PutMapping
    public ResponseEntity<List<VirtualInventoryHeaderUpdate>> updateTracker(
            @RequestBody List<VirtualInventoryHeaderUpdate> virtualInventoryHeaderUpdate
    ){
        List<VirtualInventoryHeaderUpdate> headerUpdate = virtualInventoryHeaderUpdateService.update(virtualInventoryHeaderUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(headerUpdate);
    }
}
