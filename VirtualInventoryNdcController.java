package com.capturerx.cumulus4.virtualinventory.controllers;

import com.capturerx.cumulus4.virtualinventory.errors.ErrorConstants;
import com.capturerx.cumulus4.virtualinventory.errors.ResourceNotFoundException;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryNdcService;
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

@RestController
@Api(value = "/cumulus/v1/ndc", description = "Operations related to Virtual Inventory Ndc")
@RequestMapping(value="/cumulus/v1/ndc")
@CrossOrigin(origins = "*")
public class VirtualInventoryNdcController extends BaseController{

    @Autowired
    private VirtualInventoryNdcService virtualInventoryNdcService;

    @PostMapping(value = "/{headerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Add Virtual Inventory Ndc", notes = "Create new Virtual Inventory Ndc System", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<VirtualInventoryNdcDTO> add(
            @Valid
            @RequestBody
            @ApiParam(required = true, name = "virtualInventoryNdcDTO", value = "Create Virtual Inventory Ndc")
            final VirtualInventoryNdcDTO virtualInventoryNdcDTO, @PathVariable("headerId")UUID headerId){
        VirtualInventoryNdcDTO virtualInventoryNdcDTO1=virtualInventoryNdcService.add(virtualInventoryNdcDTO,headerId);
        return generateResourceCreatedResponse(virtualInventoryNdcDTO1.toString());
    }

    @GetMapping(value = "/{headerId}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get Virtual Inventory by HeaderID", notes = "Get Virtual Inventory Ndc System based on HeaderId", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcDTO>> getAllByHeaderId(
            @Valid
            @PathVariable("headerId")
            @ApiParam(required = true, name="headerId", value = "Virtual Inventory Header Unique Identifier")
            UUID headerId){
        List<VirtualInventoryNdcDTO> virtualInventoryNdcDTOS = virtualInventoryNdcService.getNdcByHeaderId(headerId);
        if(virtualInventoryNdcDTOS == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_NOT_FOUND_FOR_HEADER, headerId));
        return generateResourceGetAllResponseOK(virtualInventoryNdcDTOS);
    }

    @PutMapping()
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    public ResponseEntity<List<VirtualInventoryNdcDTO>> updatePackageQuantity(
            @Valid
            @RequestBody
            final List<VirtualInventoryNdcDTO> ndcDTO
    ){
       List<VirtualInventoryNdcDTO> virtualInventoryNdcDTOS = virtualInventoryNdcService.updatePackageQuantity(ndcDTO);
       if(virtualInventoryNdcDTOS == null || virtualInventoryNdcDTOS.isEmpty())
           return ResponseEntity.status(HttpStatus.NO_CONTENT).body(virtualInventoryNdcDTOS);
       else
           return ResponseEntity.status(HttpStatus.OK).body(virtualInventoryNdcDTOS);
    }

    @GetMapping()
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get All ndcs", notes = "Get all ndcs", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcDTO>> getAll(){
        List<VirtualInventoryNdcDTO> virtualInventoryNdcs = virtualInventoryNdcService.getAll();
        if(virtualInventoryNdcs == null || virtualInventoryNdcs.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryNdcs);
        else
            return generateResourceGetAllResponseOK(virtualInventoryNdcs);
    }

    @GetMapping(value = "/{headerId}/{id}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get Virtual Inventory by HeaderID and NdcId", notes = "Get Virtual Inventory Ndc System based on headerId and NdcId", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<VirtualInventoryNdcDTO> getAllByHeaderIdAndId(
            @Valid
            @PathVariable("headerId")
            @ApiParam(required = true, name="headerId", value = "Virtual Inventory Header Unique Identifier")
            UUID headerId,
            @Valid
            @PathVariable("id")
            @ApiParam(required = true, name="id", value = "Virtual Inventory Ndc Unique Identifier")
            UUID id){
        VirtualInventoryNdcDTO virtualInventoryNdcDTO = virtualInventoryNdcService.getNdcbyHeaderIdAndId(headerId, id);
        if(virtualInventoryNdcDTO == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_NOT_FOUND,id,headerId));
        return ResponseEntity.status(HttpStatus.OK).body(virtualInventoryNdcDTO);
    }

    @GetMapping(value = "/{headerId}/ndc", params = "description")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get Virtual Inventory by HeaderID, description", notes = "Get Virtual Inventory Ndc System based on HeaderId and Drug Description", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcDTO>> getAllByHeaderIdAndDescription(
            @Valid
            @PathVariable("headerId")
            @ApiParam(required = true, name="headerId", value = "Virtual Inventory Ndc Unique Identifier")
            UUID headerId,
            @Valid
            @RequestParam(value = "description", required = false)
            @ApiParam(required = false, name = "description", value = "Virtual Inventory Ndc description")
            String description){
        List<VirtualInventoryNdcDTO> virtualInventoryNdcDTOS = virtualInventoryNdcService.getByHeaderIdAndDescription(headerId, description);
        if(virtualInventoryNdcDTOS == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_NOT_FOUND_FOR_HEADER, headerId));
        return generateResourceGetAllResponseOK(virtualInventoryNdcDTOS);
    }

    @GetMapping(value = "/{headerId}/ndc", params = "ndc")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    @ApiOperation(value = "Get Virtual Inventory by HeaderID, National Drug code", notes = "Get Virtual Inventory Ndc System based on Headerid and Ndc", response = VirtualInventoryNdcDTO.class)
    public ResponseEntity<List<VirtualInventoryNdcDTO>> getAllByHeaderIdAndNdc(
            @Valid
            @PathVariable(value = "headerId", required = false)
            @ApiParam(required = false, name="headerId", value = "Virtual Inventory Header Unique Identifier")
            UUID headerId,
            @Valid
            @RequestParam(value = "ndc")
            @ApiParam(name = "ndc", value = "Virtual Inventory Ndc National Drug Code")
            String ndc){
        List<VirtualInventoryNdcDTO> virtualInventoryNdcDTOS = virtualInventoryNdcService.getByHeaderIdAndNdc(headerId, ndc);
        if(virtualInventoryNdcDTOS == null) throw new ResourceNotFoundException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_NOT_FOUND_FOR_HEADER, headerId));
        return generateResourceGetAllResponseOK(virtualInventoryNdcDTOS);
    }

    @GetMapping(value = "/packages/{headerId}")
    @PreAuthorize("hasRole(T(com.capturerx.cumulus4.virtualinventory.configuration.RolesList).C4_INTERNAL.toString())")
    public ResponseEntity<List<VirtualInventoryNdcDTO>> getAllByHeaderIdAndPackageQuantity(
            @Valid
            @PathVariable("headerId")
            UUID headerId){
        List<VirtualInventoryNdcDTO> virtualInventoryNdcDTOList = virtualInventoryNdcService.getNdcByHeaderIdAndPackageQuantitygreaterThanZero(headerId);
        if(virtualInventoryNdcDTOList == null || virtualInventoryNdcDTOList.isEmpty())
            return generateResourceGetAllNoContentResponse(virtualInventoryNdcDTOList);
        else
            return generateResourceGetAllResponseOK(virtualInventoryNdcDTOList);
    }
}
