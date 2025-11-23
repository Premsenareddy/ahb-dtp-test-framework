package uk.co.deloitte.banking.ahb.dtp.test.cards.models.create;

import java.util.concurrent.ConcurrentHashMap;

public enum AccountType {
    SAVINGS("10", "savings"),
    CURRENT("20", "current");
    private String esbValue;
    private String dtpValue;

    private static final ConcurrentHashMap<String, String> accountTypeMap = new ConcurrentHashMap<>();

    static {
        for (AccountType item : values()) {
            accountTypeMap.put(item.dtpValue, item.esbValue);
        }
    }

    AccountType(String esbValue, String dtpValue) {
        this.esbValue = esbValue;
        this.dtpValue = dtpValue;
    }

    public String getESbValueForDtp(final String dtpValue) {
        return accountTypeMap.get(dtpValue);
    }

    public String getEsbValue() {
        return esbValue;
    }

    public String getDtpValue() {
        return dtpValue;
    }
}
