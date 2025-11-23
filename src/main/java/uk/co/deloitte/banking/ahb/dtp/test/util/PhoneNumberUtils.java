package uk.co.deloitte.banking.ahb.dtp.test.util;

import com.github.dockerjava.api.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoneNumberUtils {

    //TODO::Make configurable
    public static final String FORBIDDEN_NUMBER = "+971";

    public static String sanitize(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            log.debug("PhoneNumberUtils::sanitize:[{}]", phoneNumber);
            if (phoneNumber.contains(FORBIDDEN_NUMBER)) {
                throw new BadRequestException("Number not allowed");
            }
        }
        return phoneNumber;
    }
}
