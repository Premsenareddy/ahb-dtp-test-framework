package uk.co.deloitte.banking.ahb.dtp.test.devSim.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProcessOriginEnum {

    NAME_SCREENING("Name screening"),
    BIRTH_CERTIFICATE_AND_RELATIONSHIP_VERIFICATION("Birth Certificate and Relationship Verification"),
    IDV("IDV"),
    EID_VERIFICATION("EID Verification");

    private final String value;

    public String getValue() {
        return this.value;
    }
}

