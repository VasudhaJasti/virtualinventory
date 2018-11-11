package com.capturerx.cumulus4.virtualinventory.models;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.UUID;

@Data
public class WholesalerAccountDTO {
    private UUID id;
    private String billToTypeCode;
    private String billToTypeName;
    private UUID billToId;
    private String billToName;
    private String shipToTypeCode;
    private String shipToTypeName;
    private UUID shipToId;
    private String shipToName;
    private UUID wholesalerId;
    private String wholesalerName;
    private String accountNumber;
    private String masterAccountNumber;
    private String accountTypeCode;
    private String accountTypeName;
    private DateTime effectiveDate;
    private DateTime terminationDate;

}
