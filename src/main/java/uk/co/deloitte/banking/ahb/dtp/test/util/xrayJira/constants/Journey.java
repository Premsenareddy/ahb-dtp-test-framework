package uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.constants;

public enum Journey {
    BAC("A. Becoming a Customer"),
    SAVE("B. Save"),
    SPEND_PAY("C. Spend/Pay"),
    ENGAGE("D. Engage"),
    BORROW("E. Borrow"),
    GROW("F. Grow"),
    CORE_BANKING("Core Banking"),
    PLATFORM("Platform"),
    CRM("CRM"),
    OPERATIONS("Operations");

    private String journey;

    private Journey(String journey) {
        this.journey = journey;
    }

    public String getJourney() {
        return this.journey;
    }

}

