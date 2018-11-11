package com.capturerx.cumulus4.virtualinventory.models;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.UUID;

@Data
public class ContractsVirtualInventory {

    private UUID contractId;
    private UUID coveredEntityId;
    private UUID pharmacyId;
    private String contractName;
    private DateTime effectiveDate;
    private DateTime terminationDate;
    private UUID wholesalerAccountId;
    private String billToTypeCode;
    private String shipToTypeCode;
    private UUID billToId;
    private UUID shipToId;
    private UUID wholesalerId;
    private String accountNumber;
    private String masterAccountNumber;
    private String pharmacyName;
    private String coveredEntityBillToName;
    private String centralFillShipToName;

}