package uk.co.deloitte.banking.journey.scenarios.adult.pdn.model;

import java.math.BigDecimal;

public abstract class PDNData {
    protected static final String DIVIDER = ",";

     protected static String cleanItem(String item) {
        return item.replaceAll(" ", "");
    }

    protected static BigDecimal mapBalance(String balance) {
        String cleanItem = cleanItem(balance);
        return new BigDecimal(cleanItem);
    }
}
