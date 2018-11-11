package com.capturerx.cumulus4.virtualinventory.controllers;

import com.capturerx.cumulus4.virtualinventory.errors.ErrorConstants;
import com.capturerx.cumulus4.virtualinventory.errors.ResourceNotFoundException;
import com.capturerx.cumulus4.virtualinventory.impls.VirtualInventoryServiceImpl;
import com.capturerx.cumulus4.virtualinventory.dtos.VirtualInventoryHeaderDTO;
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

@RestController
@RequestMapping(path="/cumulus/v1/header", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "/cumulus/v1/header", description = "Operations related to Virtual Inventory")
@CrossOrigin(origins = "*")
public class VirtualInventoryController extends BaseController{

    @Autowired
    private VirtualInventoryServiceImpl virtualInventoryService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory", notes = "Create new Virtual Inventory System", response = VirtualInventoryHeaderDTO.class)
    public @ResponseBody
    ResponseEntity<List<VirtualInventoryHeaderDTO>> add(
            @RequestBody
            List<VirtualInventoryHeaderDTO> headerDTOS
    ){
        List<VirtualInventoryHeaderDTO> virtualInventoryHeaderDTOS= virtualInventoryService.add(headerDTOS);
        if(virtualInventoryHeaderDTOS == null || virtualInventoryHeaderDTOS.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(virtualInventoryHeaderDTOS);
        else
            return ResponseEntity.status(HttpStatus.OK).body(virtualInventoryHeaderDTOS);
    }

    @GetMapping()
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get All Virtual Inventory", notes = "Getting All fileds in Virtual Inventory System", response = VirtualInventoryHeaderDTO.class)
    public ResponseEntity<List<VirtualInventoryHeaderDTO>> getAll(){
        List<VirtualInventoryHeaderDTO> virtualInventoryList = virtualInventoryService.getAll();
        if(virtualInventoryList == null || virtualInventoryList.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryList);
        else
            return generateResourceGetAllResponseOK(virtualInventoryList);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Update Virtual Inventory", notes = "Update Data in Virtual Inventory System", response = VirtualInventoryHeaderDTO.class)
    public ResponseEntity<VirtualInventoryHeaderDTO> update(
            @Valid
            @RequestBody
            @ApiParam(required = true, name = "virtualInventoryDTO", value = "Update Virtual Inventory Header")
            final VirtualInventoryHeaderDTO virtualInventoryDTO){
        VirtualInventoryHeaderDTO headerDTO= virtualInventoryService.update(virtualInventoryDTO);
        return ResponseEntity.status(HttpStatus.OK).body(headerDTO);
    }

    @GetMapping(value="/{id}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get particular Virtual Inventory", notes = "Get particular Virtual Inventory by id", response = VirtualInventoryHeaderDTO.class)
    public ResponseEntity<VirtualInventoryHeaderDTO> getById(
            @Valid
            @PathVariable("id")
            @ApiParam(required = true, name="id", value = "Virtual Inventory Header Unique Identifier") UUID id){
        VirtualInventoryHeaderDTO headerDTO = virtualInventoryService.getById(id);
        if(headerDTO == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_HEADER_NOT_FOUND,id));
        return ResponseEntity.status(HttpStatus.OK).body(headerDTO);
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation(value = "Delete Virtual Inventory", notes = "Delete Virtual Inventory by id")
    public ResponseEntity<Void> remove(
            @Valid
            @PathVariable("id")
            @ApiParam(required = true, name="id", value = "Virtual Inventory Header Unique Identifier")
            UUID id) {
        virtualInventoryService.remove(id);
        return generateResourceDeletedResponse();
    }

}