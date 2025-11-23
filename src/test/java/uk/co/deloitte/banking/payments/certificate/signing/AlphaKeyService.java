package uk.co.deloitte.banking.payments.certificate.signing;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.testcontainers.shaded.org.bouncycastle.asn1.sec.SECNamedCurves;
import org.testcontainers.shaded.org.bouncycastle.asn1.x9.X9ECParameters;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.testcontainers.shaded.org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.testcontainers.shaded.org.bouncycastle.crypto.params.ECDomainParameters;
import org.testcontainers.shaded.org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.testcontainers.shaded.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.testcontainers.shaded.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.testcontainers.shaded.org.bouncycastle.jce.ECNamedCurveTable;
import org.testcontainers.shaded.org.bouncycastle.jce.ECPointUtil;
import org.testcontainers.shaded.org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testcontainers.shaded.org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.testcontainers.shaded.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

/**
 * Alpha key service. The complexity of key algorithms / specifications should exist in here.
 * <p>
 * NOTE - improvement would be extract interface and have an (1) EC and (2) RSA implementation as soon as RSA
 * requirement is needed. [Bob Marks, 18 Nov 19]
 * <p>
 * FIXME / TODO - These have been copied from `alpha-certificate-service` - need to split into a
 * library to keep DRY / E2E tests robust [Bob Marks].
 */
@Slf4j
@Singleton
public class AlphaKeyService {

    @Inject
    private CertificateApi certificateApi;

    // Public constants

    public static final String ALGORITHM_EC = "EC";
    public static final String CURVE_SPEC_SECP256 = "secp256r1";
    public static final String CURVE_SPEC_PRIME256 = "prime256v1";
    public static final String SIGNATURE_ALGORITHM_SHA256_ECDSA = "SHA256withECDSA";


    public static final ECNamedCurveParameterSpec PRIME256V1_SPEC =
            ECNamedCurveTable.getParameterSpec(CURVE_SPEC_PRIME256);
    public static final ECNamedCurveSpec EC_SPEC_PARAMS = new ECNamedCurveSpec(
            CURVE_SPEC_PRIME256, PRIME256V1_SPEC.getCurve(), PRIME256V1_SPEC.getG(), PRIME256V1_SPEC.getN());

    // Other fields

    public final KeyFactory keyFactoryEc;

    public AlphaKeyService() {
        try {
            Security.addProvider(new BouncyCastleProvider());

            this.keyFactoryEc = KeyFactory.getInstance(ALGORITHM_EC, BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new CertificateException("Error initialising alpha key service", e);
        }
    }

    /**
     * Return an EC based public key from a base64 encoded String.
     *
     * @param publicKeyBase64
     * @return
     */
    public PublicKey getECPublicKey(String publicKeyBase64) {
        try {
            // 1) Convert base64 encoded public key to byte array
            byte[] publicBytes = SecurityUtils.base64Decode(publicKeyBase64);

            // 2) Code below creates the public key from byte array using spec parameters
            ECPoint point = ECPointUtil.decodePoint(EC_SPEC_PARAMS.getCurve(), publicBytes);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, EC_SPEC_PARAMS);
            PublicKey ecPublicKey = keyFactoryEc.generatePublic(pubKeySpec);
            return ecPublicKey;
        } catch (InvalidKeySpecException e) {
            throw new CertificateException("Error creating public key from [ " + publicKeyBase64 + " ]", e);
        }
    }


    /**
     * Return an EC based private Key from a base64 encoded String.
     *
     * @param privateKeyBase64
     * @return
     */
    public PrivateKey getECPrivateKey(String privateKeyBase64) {
        try {
//            log.info("getECPrivateKey::[{}]", privateKeyBase64);
            // 1) Convert base64 encoded public key to byte array
            byte[] privateBytes = SecurityUtils.base64Decode(privateKeyBase64);

            // 2) Code below creates the public key from byte array using spec parameters
            ECPrivateKeySpec priKeySpec = new ECPrivateKeySpec(new BigInteger(1, privateBytes), EC_SPEC_PARAMS);
            PrivateKey ecPrivateKey = keyFactoryEc.generatePrivate(priKeySpec);
            return ecPrivateKey;
        } catch (InvalidKeySpecException e) {
            throw new CertificateException("Error creating public key from [ " + privateKeyBase64 + " ]", e);
        }
    }

    /**
     * Generate an a-symmetric EC key pair (private / public).
     *
     * @return
     */
    public AsymmetricCipherKeyPair generateEcKeyPair() {

        X9ECParameters ecp = SECNamedCurves.getByName(CURVE_SPEC_SECP256);
        ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), ecp.getH(),
                ecp.getSeed());

        // Generate a private key and a public key
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keyGenParams);
        AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    /**
     * Return public key from an a-symmetric EC key pair and encode as Base64.
     *
     * @param keyPair
     * @return
     */
    public String getPrivateKeyAsBase64(AsymmetricCipherKeyPair keyPair) {
        ECPrivateKeyParameters ecPrivateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        byte[] privateKeyBytes = ecPrivateKey.getD().toByteArray();
        return SecurityUtils.base64Encode(privateKeyBytes);
    }

    /**
     * Return private key from an a-symmetric EC key pair and encode as Base64.
     *
     * @param keyPair
     * @return
     */
    public String getPublicKeyAsBase64(AsymmetricCipherKeyPair keyPair) {
        ECPublicKeyParameters ecPublicKey = (ECPublicKeyParameters) keyPair.getPublic();
        byte[] publicKeyBytes = ecPublicKey.getQ().getEncoded(true);
        return SecurityUtils.base64Encode(publicKeyBytes);
    }

    public String generateJwsSignature(AlphaTestUser alphaTestUser, String payload, String privateKeyStr) {
        return generateJwsSignatureForPayload(payload, privateKeyStr);
    }

    public String generateJwsSignature(AlphaTestUser alphaTestUser, String payload) {
        return generateJwsSignature(alphaTestUser, payload, alphaTestUser.getPrivateKeyBase64());
    }

    public String generateJwsSignatureForPayload(String payload, String privateKeyStr) {
        log.debug("generateJwsSignatureForPayload [{}] [{}] ", payload, privateKeyStr);
        PrivateKey privateKey = getECPrivateKey(privateKeyStr);
        byte[] payloadSignature = SecurityUtils.signPayload(SIGNATURE_ALGORITHM_SHA256_ECDSA, privateKey, payload);
        String payloadSignatureBase64 = SecurityUtils.base64Encode(payloadSignature);

        JWSObject jwsObject = new JWSObject(
                new JWSHeader.Builder(new JWSAlgorithm("ES256", Requirement.OPTIONAL)).build(),
                new Payload(jsonObj("signature", payloadSignatureBase64))
        );

        try {
            JWSSigner signer = new ECDSASigner((ECPrivateKey) privateKey);
            jwsObject.sign(signer);
            return jwsObject.serialize();
        }
        catch (Exception e) {
            throw new RuntimeException("Error signing JWS object");
        }

    }

    public static JSONObject jsonObj(String key, String value) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(key, value);
        return jsonObj;
    }

    public String validateCertForPayments(AlphaTestUser alphaTestUser, String payload) {
        AND("I then send a request with a payload certificate can be validated");
        String signedSignature = generateJwsSignature(alphaTestUser, payload);
        THEN("The client submits the payload and receives a 204 response");
        this.certificateApi.validateCertificate(alphaTestUser, payload, signedSignature, 204);
        return signedSignature;
    }


}
