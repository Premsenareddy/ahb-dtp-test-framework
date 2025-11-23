package uk.co.deloitte.banking.ahb.dtp.test.util;

import java.util.Map;

public class JsonUtils {

    public static String extractValue(Object object, String key) {
        var jsonValue = ((Map) object).get(key);
        return ((Map) jsonValue).get("Value").toString();
    }
}
