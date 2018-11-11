package com.capturerx.cumulus4.virtualinventory.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * This class acts as base class for all controllers
 * @param <T>
 */
public abstract class BaseController <T> {

    /**
     * This method generates the required response for add operations
     * @param id
     * @return
     */

    protected ResponseEntity generateResourceCreatedResponse(UUID id){
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(id).toUri();

        return ResponseEntity.created(location).build();
    }

    protected ResponseEntity generateResourceCreatedResponse(String hash){
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{hash}")
                .buildAndExpand(hash).toUri();

        return ResponseEntity.created(location).build();
    }

    protected ResponseEntity generateResourceCreatedResponse(Boolean isSuccess){
        return(new ResponseEntity(isSuccess.toString(), HttpStatus.OK));
    }

    protected ResponseEntity<List<T>> generateResourceGetAllNoContentResponse(List<T> entities){
        return new ResponseEntity<List<T>>(entities, HttpStatus.NO_CONTENT);
    }

    protected ResponseEntity<List<T>> generateResourceGetAllResponseOK(List<T> entities){
        return new ResponseEntity<List<T>>(entities, HttpStatus.OK);
    }

    /**
     * This method generates the required response for delete operations
     * @return
     */
    protected ResponseEntity generateResourceDeletedResponse(){
        return ResponseEntity.noContent().build();
    }

}

