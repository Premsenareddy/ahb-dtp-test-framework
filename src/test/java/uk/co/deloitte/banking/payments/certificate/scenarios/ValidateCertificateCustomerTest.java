package uk.co.deloitte.banking.payments.certificate.scenarios;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.shaded.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.environment.EnvUtils;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteCustomer1Data;
import uk.co.deloitte.banking.customer.api.customer.model.OBWriteEmailState1;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;
import uk.co.deloitte.banking.payments.certificate.api.CertificateApi;
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService;

import javax.inject.Inject;
import java.time.LocalDate;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.AND;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.GIVEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.THEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.WHEN;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateEnglishRandomString;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ValidateCertificateCustomerTest {

    @Inject
    private CertificateApi certificateApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    private AlphaTestUser alphaTestUser;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AlphaKeyService alphaKeyService;

    @Inject
    private EnvUtils envUtils;

    @Inject
    private CustomerApi customerApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private void createUserAndGenerateCert() {
        if (this.alphaTestUser == null) {
            this.alphaTestUser = new AlphaTestUser();
            LoginResponseV1 loginResponse = authenticateApi.registerUserAndDevice(alphaTestUser);
            parseLoginResponse(alphaTestUser, loginResponse);
            generateValidKeyPairAndUploadCert();
        }
    }

    private void setupTestUser() {

        this.alphaTestUser = new AlphaTestUser();
        this.alphaTestUser = alphaTestUserFactory.setupCustomer(alphaTestUser);
        this.alphaTestUserBankingCustomerFactory.setUpAccount(this.alphaTestUser);
    }

    private void generateValidKeyPairAndUploadCert() {
        AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificate(alphaTestUser);
    }

    @BeforeEach
    void setUpTest() {
        alphaTestUser = alphaTestUserFactory.refreshAccessToken(alphaTestUser);
    }

    @Test
    @Order(1)
    public void upload_and_validate_certificate_creating_customer() {
        TEST("AHBDB-2357 certificate service can upload and validate certificates");
        GIVEN("I have a valid alpha test user set up");
        createUserAndGenerateCert();

        WHEN("I create a user I can generate a public/private key and upload the public key into certificates service");

        THEN("The client uploads public key and receives a 200 response");

        WHEN("I generate the a payload");
        OBWriteCustomer1 obWriteCustomer = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(generateEnglishRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(21))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .build())
                .build();


        AND("I then send a request with a payload certificate can be validated");
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, obWriteCustomer.toString());

        THEN("The client submits the payload and receives a 204 response");
        this.certificateApi.validateCertificate(alphaTestUser, obWriteCustomer.toString(), signedSignature, 204);

    }

    @Test
    @Order(2)
    public void negative_upload_and_validate_certificate_creating_customer_different_payload() {

        TEST("AHBDB-2357 certificate service can upload and validate certificates");
        GIVEN("I have a valid alpha test user set up");
        createUserAndGenerateCert();

        WHEN("I create a user I can generate a public/private key and upload the public key into certificates service");

        THEN("The client uploads public key and receives a 200 response");

        WHEN("I generate the first payload");
        OBWriteCustomer1 obWriteCustomer1 = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(generateEnglishRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(21))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .build())
                .build();


        AND("I then send a request with a payload certificate can be validated");
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, obWriteCustomer1.toString());

        THEN("The client submits the payload and receives a 204 response");
        this.certificateApi.validateCertificate(alphaTestUser, obWriteCustomer1.toString(), signedSignature, 204);

        WHEN("I generate a new different payload");
        OBWriteCustomer1 obWriteCustomer2 = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName("DifferentName")
                        .dateOfBirth(LocalDate.now().minusYears(21))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(generateRandomEmail())
                        .mobileNumber(generateRandomMobile())
                        .language("en")
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .build())
                .build();

        THEN("The client submits the second payload using the same signed cert and receives a 400 response");
        this.certificateApi.validateCertificate(alphaTestUser, obWriteCustomer2.toString(), signedSignature, 400);

    }

    @Test
    @Order(3)
    public void negative_upload_and_validate_certificate_creating_customer() {

        TEST("AHBDB-2357 certificate service can upload and validate certificates");
        GIVEN("I have a valid alpha test user set up");
        createUserAndGenerateCert();

        WHEN("I create a user I can generate a public/private key and upload the public key into certificates service");

        THEN("The client uploads public key and receives a 200 response");

        WHEN("I generate the payload");
        OBWriteCustomer1 obWriteCustomer = OBWriteCustomer1.builder()
                .data(OBWriteCustomer1Data.builder()
                        .preferredName(generateEnglishRandomString(10))
                        .dateOfBirth(LocalDate.now().minusYears(21))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .email(alphaTestUser.getUserEmail())
                        .mobileNumber(alphaTestUser.getUserTelephone())
                        .language("en")
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .build())
                .build();

        AND("I generate another key pair that does not match the uploaded pair");
        AsymmetricCipherKeyPair keyPair2 = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair2));

        WHEN("I then generate the payload certificate can be validated");
        String signedSignature = alphaKeyService.generateJwsSignature(alphaTestUser, obWriteCustomer.toString());

        THEN("The client submits the payload and an incorrect signature response is 400");
        this.certificateApi.validateCertificate(alphaTestUser, obWriteCustomer.toString(), signedSignature, 400);

    }


    @Test
    @Order(4)
    public void upload_and_validate_certificate_fails_with_wrong_scope() {
        TEST("AHBDB-13036 certificate upload has limited scope");
        GIVEN("I have a valid alpha test user set up with account scope");
        setupTestUser();
        AsymmetricCipherKeyPair keyPair = alphaKeyService.generateEcKeyPair();
        alphaTestUser.setPrivateKeyBase64(alphaKeyService.getPrivateKeyAsBase64(keyPair));
        alphaTestUser.setPublicKeyBase64(alphaKeyService.getPublicKeyAsBase64(keyPair));
        this.certificateApi.uploadCertificateForbbidenError(alphaTestUser);
    }

}
