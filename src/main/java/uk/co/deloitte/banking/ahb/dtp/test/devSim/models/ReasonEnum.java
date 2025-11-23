package uk.co.deloitte.banking.ahb.dtp.test.devSim.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ReasonEnum {

    E_NAME_CHECKER_HIT("E-name checker hit"),
    VERIFY_RELATIONSHIP("Verify Relationship"),
    IDV_REVIEW_NEEDED("IDV Review Needed"),
    IDV_DOCUMENT_ERROR("IDV document error"),
    EID_CANNOT_BE_VERIFIED("EID could not be verified");

    private final String value;

    public String getValue() {
        return this.value;
    }
}
