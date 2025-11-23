package uk.co.deloitte.banking.payments.certificate.signing;

import javax.inject.Singleton;

/**
 * FIXME / TODO - These have been copied from `alpha-certificate-service` - need to split into a
 * library to keep DRY / E2E tests robust [Bob Marks].
 */
@Singleton

public class CertificateException extends RuntimeException {

    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

}
