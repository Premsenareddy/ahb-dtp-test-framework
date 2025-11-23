package uk.co.deloitte.banking.ahb.dtp.test.util;

import lombok.extern.slf4j.Slf4j;

import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomLong;

@Slf4j
public class RequestUtils {

    public static Long generateCorrelationId() {
        final Long traceId = generateRandomLong();
        log.warn("TRACE::[{}]", traceId);
        return traceId;
    }

    public static Long generateIdempotentId() {
        final Long idempotencyId = generateRandomLong();
        log.warn("IDEMPOTENCY_ID::[{}]", idempotencyId);
        return idempotencyId;
    }

}
