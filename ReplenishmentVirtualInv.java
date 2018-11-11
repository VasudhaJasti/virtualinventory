package com.capturerx.cumulus4.virtualinventory.models;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.UUID;

@Data
public class ReplenishmentVirtualInv {

    private UUID contractId;
    private UUID headerId;
    private DateTime effectiveDate;
    private DateTime terminationDate;
    private String billToTypeCode;
    private UUID billToId;
    private String shipToTypeCode;
    private UUID shipToId;
    private String billToName;
    private String shipToName;
    private String ndc;
    private int packageQuantity;
    private int packageSize;
    private String drugDescription;

}
