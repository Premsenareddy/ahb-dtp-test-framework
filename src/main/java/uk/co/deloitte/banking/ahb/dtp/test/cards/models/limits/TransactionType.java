package uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits;

import java.util.concurrent.ConcurrentHashMap;

public enum TransactionType {
    PURCHASE("purchase", "00"),
    WITHDRAWAL("withdrawal", "01"),
    ECOMMERCE("ecommerce", "90");
    private String label;
    private String transactionTypeCode;

    TransactionType(String label, String transactionTypeCode) {
        this.label = label;
        this.transactionTypeCode = transactionTypeCode;
    }

    private static final ConcurrentHashMap<String, String> tnxsType = new ConcurrentHashMap<>();

    static {
        for (TransactionType item : values()) {
            tnxsType.put(item.label, item.transactionTypeCode);
        }
    }

    public String getLabel() {
        return label;
    }

    public static String getTransactionCodeForType(final String tnxType) {
        return tnxsType.get(tnxType);
    }
}
