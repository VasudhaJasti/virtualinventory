package com.capturerx.cumulus4.virtualinventory.models;

import lombok.Data;

@Data
public class MedispanDrug {

    private String ndc;
    private String drugName;
    private String roadDescription;
    private String strength;
    private String strengthUnitOfMeasure;
    private String tcgpiDrugFullGPIDesc;
    private String packageSize;
    private String dosageForm;


}
