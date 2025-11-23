package uk.co.deloitte.banking.journey.scenarios.adult.pdn;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccount1;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.api.relationship.model.OBReadRelationship1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBRelationshipRole;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1;
import uk.co.deloitte.banking.customer.api.relationship.model.OBWriteDependant1Data;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;
import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.PDNTestDataResultDataHolder;
import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.creation.PDNDataAccounts;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.ACCOUNT_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.CUSTOMER_SCOPE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomString;
import static uk.co.deloitte.banking.banking.account.utils.AlphaTestUserBankingCustomerFactory.parseLoginResponse;
import static uk.co.deloitte.banking.journey.scenarios.adult.AdultBankingCustomerScenarioThin.ACCOUNT_TEST;
import static uk.co.deloitte.banking.journey.scenarios.adult.AdultBankingCustomerScenarioThin.SMOKE_TEST;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PDNDataCreatorAccountCreationScenario extends AdultOnBoardingBase {

    public static final String ACCOUNT_CREATION_RESULT_CSV = "account-creation-result.csv";
    @Inject
    private RelationshipApi relationshipApi;

    @Inject
    private AlphaTestUserBankingCustomerFactory alphaTestUserBankingCustomerFactory;

    private final static String TEMPORARY_PASSWORD = "validtestpassword";

    private AlphaTestUser childAlphaTestUser;

    private static List<PDNTestDataResultDataHolder> RESULT = new ArrayList<>();

    @Tag(ACCOUNT_TEST)
    @Tag(SMOKE_TEST)
    @Test
    public void executeDataCreation() throws IOException, URISyntaxException {
        TEST("Execute PDN data creation process");
        List<PDNDataAccounts> data = PDNDataAccounts.getData();
        System.out.println(data);
        AND("Data is successfully gathered. number of main accounts is -> " + data.size());

        for (int i = 0; i < data.size(); i++) {
            System.out.println("Iteration -> " + i);
            PDNDataAccounts dataItem = data.get(i);
            System.out.println("Data -> " + dataItem.toString());

            AND("Perform customer marketplace");
            this.marketplace_customer_setup_success(true);

            AND("Reauthenticate");
            this.reauthenticate(ACCOUNT_SCOPE);

            AND("Generate customer cif");
            String adultCif = this.generate_customer_cif();

            AND("Reauthenticate");
            this.reauthenticate(ACCOUNT_SCOPE);

            AND("Verify EID");
            verifyEid(alphaTestUser);

            AND("Create customer account");
            this.create_account();

            AND("Deposit money to account");
            deposit(alphaTestUser.getAccountNumber(), dataItem.getBalance());

            RESULT.add(new PDNTestDataResultDataHolder(alphaTestUser.getAccountNumber(), adultCif, dataItem.getBalance(), dataItem.getAccountType().name()));

            for (PDNDataAccounts dependant : dataItem.getDependants()) {
                AND("Setup dependant");

                childAlphaTestUser = new AlphaTestUser();

                String childCif = setupDependant(dependant.getAccountType());
                deposit(childAlphaTestUser.getAccountNumber(), dependant.getBalance());
                RESULT.add(new PDNTestDataResultDataHolder(childAlphaTestUser.getAccountNumber(), childCif, dependant.getBalance(), dependant.getAccountType().name()));
            }

            NOTE("Round -" + i);
            NOTE("Dump to console");
            dump();
            NOTE("Dump is done");

        }

        NOTE("DONE");
        NOTE("DUMP");
        dump();
        NOTE("Dump to file");
        FileDumper.dumpToFile(ACCOUNT_CREATION_RESULT_CSV, RESULT);
    }

    private void dump() {
        for (PDNTestDataResultDataHolder testData : RESULT) {
            System.out.println(testData.toString());
        }
    }

    private void deposit(String accountNumber, BigDecimal amount) {
        this.cardProtectedApi.createCardDepositWebhook(accountNumber, amount);
    }

    private String setupDependant(OBExternalAccountType1Code accountType1Code) {
        AND("Create user relationships");
        String depandant = createUserRelationship();

        AND("Create dependant");
        int age = 2;

        if (accountType1Code == OBExternalAccountType1Code.AHB_YOUTH_SAV) {
            age = 15;
        }

        String relationship = createDependantCustomerAndRelationship(age, depandant);

        AND("Send child otp");
        String otp = sendChildOtp(relationship, depandant);

        AND("register child device");
        registerChildDevice(otp, depandant);

        AND("upload certificate");
        certificate_upload_test();

        AND("Switch to child customer scope");
        childCustomerScopeTest();

        AND("Create IDV");
        updateIdv(relationship);

        AND("Create CRS");
        updateCrs();

        AND("Add child customer details");
        childCustomerDetailsTest();

        AND("Setup child cif");
        String childCif = setupCif(relationship);

        AND("Verify child cif");
        verifyEid(childAlphaTestUser);

        AND("Switch to child account scope test");
        accountScope();

        AND("Create child account");
        createDependentAccount(accountType1Code, relationship);

        return childCif;
    }

    private void verifyEid(AlphaTestUser alphaTestUser) {
        OBWriteEIDStatus1 build = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();
        customerApiV2.updateCustomerValidations(alphaTestUser, build);
    }

    private void certificate_upload_test() {
        childAlphaTestUser = alphaTestUserFactory.setupUserCerts(childAlphaTestUser);
    }

    private String sendChildOtp(String relationshipId, String dependantId) {
        otpApi.sentChildOTPCode(alphaTestUser, 204, relationshipId);

        OtpCO otpCO = developmentSimulatorService.retrieveOtpFromDevSimulator(dependantId);
        return otpCO.getPassword();
    }

    private String setupCif(String relationshipId) {
        OBReadCustomer1 obReadCustomer1 = this.relationshipApi.putChildCif(alphaTestUser, relationshipId);
        return obReadCustomer1.getData().getCustomer().get(0).getCif();
    }

    private void registerChildDevice(String otpCode, String dependantId) {
        DependantRegisterDeviceRequestV2 request = DependantRegisterDeviceRequestV2.builder()
                .userId(dependantId)
                .password(TEMPORARY_PASSWORD)
                .otp(otpCode)
                .build();

        UserLoginResponseV2 userLoginResponseV2 = authenticateApi
                .registerDependantUserDevice(childAlphaTestUser, request);

        parseLoginResponse(childAlphaTestUser, userLoginResponseV2);

    }

    private void childCustomerScopeTest() {
        this.authenticateApi.patchUser(childAlphaTestUser,
                UpdateUserRequestV1.builder()
                        .sn("CUSTOMER")
                        .build());


        UserLoginResponseV2 userLoginResponse = authenticateApi.loginUserProtected(childAlphaTestUser,
                UserLoginRequestV2.builder()
                        .userId(childAlphaTestUser.getUserId())
                        .password(childAlphaTestUser.getUserPassword())
                        .build(),
                childAlphaTestUser.getDeviceId(), true);

        Assertions.assertEquals(CUSTOMER_SCOPE, userLoginResponse.getScope());
        parseLoginResponse(childAlphaTestUser, userLoginResponse);
    }


    private void updateIdv(String relationshipId) {
        relationshipApi.createChildIdvDetails(alphaTestUser, relationshipId);
    }

    private void updateCrs() {
        alphaTestUserBankingCustomerFactory.setupEmploymentChild(childAlphaTestUser);
    }

    private void childCustomerDetailsTest() {
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName(generateRandomString(5))
                        .lastName(generateRandomString(10))
                        .preferredName("Test" + generateRandomString(5))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        .cityOfBirth("Dubai")
                        .email(generateRandomEmail())
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .nationality("AE")
                        .address(OBPartialPostalAddress6.builder()
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber("101")
                                .country("AE")
                                .countrySubDivision("Dubai")
                                .postalCode("123456")
                                .build())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();

        this.customerApi.updateCustomer(childAlphaTestUser,
                customer,
                200);
    }

    private void accountScope() {
        alphaTestUserBankingCustomerFactory.assertAccountScope(childAlphaTestUser);
        UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(childAlphaTestUser);
        parseLoginResponse(childAlphaTestUser, loginResponse);
    }

    private String createUserRelationship() {
        UserRelationshipWriteRequest request =
                UserRelationshipWriteRequest.builder().tempPassword(TEMPORARY_PASSWORD).build();
        LoginResponse response = this.authenticateApi.createRelationshipAndUser(alphaTestUser, request);
        return response.getUserId();
    }

    private String createDependantCustomerAndRelationship(int age, String dependantId) {
        OBWriteDependant1 obWriteDependant1 = OBWriteDependant1.builder()
                .data(OBWriteDependant1Data.builder()
                        .id(UUID.fromString(dependantId))
                        .dateOfBirth(LocalDate.now().minusYears(age))
                        .fullName("ete" + generateRandomString(5) + " " + generateRandomString(5))
                        .gender(OBGender.MALE)
                        .language("en")
                        .termsVersion(LocalDate.now())
                        .termsAccepted(Boolean.TRUE)
                        .customerRole(OBRelationshipRole.FATHER)
                        .dependantRole(OBRelationshipRole.SON)
                        .build())
                .build();

        OBReadRelationship1 response = this.relationshipApi.createDependant(alphaTestUser, obWriteDependant1);
        return response.getData().getRelationships().stream()
                .filter(relationship -> relationship.getCustomerId().toString().equals(dependantId))
                .findFirst().get().getConnectionId().toString();
    }

    private void createDependentAccount(OBExternalAccountType1Code accountType1Code, String connectionId) {
        OBWriteAccount1 request = relationshipAccountApi.createChildAccountData(accountType1Code);
        OBWriteAccountResponse1 response = relationshipAccountApi.createDependantCustomerAccount(alphaTestUser, request, connectionId);

        childAlphaTestUser.setAccountNumber(response.getData().getAccountId());
    }

}
