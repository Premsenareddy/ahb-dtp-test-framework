package uk.co.deloitte.banking.ahb.dtp.test.devSim.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CaseTypeEnum {

    COMPLAINT("Complaint"),
    GENERAL("General"),
    EXCEPTION("Exception"),
    OPERATION("Operation");

    private final String value;

    public String getValue() {
        return this.value;
    }
}
