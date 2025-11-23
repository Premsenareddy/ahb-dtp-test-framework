package uk.co.deloitte.banking.payments.certificate.signing;

import javax.inject.Singleton;
import java.security.*;
import java.util.Base64;

/**
 * Collection of useful static security utilities.
 * <p>
 * NOTE - these should not be tied to security implementations but use interfaces only e.g. `PublicKey` etc.
 * <p>
 * library to keep DRY / E2E tests robust [Bob Marks].
 */
@Singleton
public class SecurityUtils {

    /**
     * Decode a Base64 encoded String.
     *
     * @param input
     * @return
     */
    public static byte[] base64Decode(String input) {
        return Base64.getDecoder().decode(input);
    }

    /**
     * Decode a Base64 encoded String.
     *
     * @param input
     * @return
     */
    public static String base64Encode(byte[] input) {
        return new String(Base64.getEncoder().encode(input));
    }


    /**
     * Sign a payload.
     *
     * @param signingAlgorithm
     * @param privateKey
     * @param payload
     * @return
     */
    public static byte[] signPayload(String signingAlgorithm, PrivateKey privateKey, String payload) {
        try {
            Signature signature = Signature.getInstance(signingAlgorithm);
            signature.initSign(privateKey);
            signature.update(payload.getBytes());

            return signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new CertificateException("Error validating payload", e);
        }
    }

}
