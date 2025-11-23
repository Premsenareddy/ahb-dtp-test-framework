package uk.co.deloitte.banking.banking.account.utils;

import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.banking.account.api.account.model.OBWriteAccountResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.util.TokenUtils;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.cards.api.CardsApiFlows;
import uk.co.deloitte.banking.customer.api.customer.model.*;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBCRSData2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBTaxResidencyCountry2;
import uk.co.deloitte.banking.customer.api.customer.model.crs.OBWriteCRS2;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentDetails1;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBEmploymentStatus;
import uk.co.deloitte.banking.customer.api.customer.model.employment.OBWriteEmploymentDetailsResponse1;
import uk.co.deloitte.banking.customer.api.customer.model.idv.OBWriteIdvDetailsResponse1;
import uk.co.deloitte.banking.customer.api.openbanking.v3.account.OBPartialPostalAddress6;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApiV2;
import uk.co.deloitte.banking.customer.employment.api.EmploymentApiV2;
import uk.co.deloitte.banking.customer.profile.api.CustomerApiV2;
import uk.co.deloitte.banking.customer.relationship.api.RelationshipApi;
import uk.co.deloitte.banking.customer.utils.AlphaTestUserFactory;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;

public class AlphaTestUserBankingCustomerFactory {

    private static AlphaTestUser alphaTestUserStatic;

    @Inject
    private AlphaTestUserFactory alphaTestUserFactory;

    @Inject
    private AccountApi accountApi;

    @Inject
    private AuthenticateApiV2 authenticateApi;

    @Inject
    private CustomerApiV2 customerApiV2;

    @Inject
    private CardsApiFlows cardsApiFlows;

    @Inject
    private EmploymentApiV2 employmentApi;

    @Inject
    private RelationshipApi relationshipApi;

    public AlphaTestUser setUpCustomer(AlphaTestUser alphaTestUser) {
        this.alphaTestUserStatic = alphaTestUserFactory.setupCustomer(alphaTestUser);
        return this.alphaTestUserStatic;
    }

    public void setUpBankingCustomer(AlphaTestUser alphaTestUser) {

        setupIdv(alphaTestUser);

        updateCustomerIdpStateStatus(alphaTestUser); //new line added

        setupEmployment(alphaTestUser);

        setupCrs(alphaTestUser);

        //update customer information
        updateCustomerInformation(alphaTestUser, false);

        //create customer cif
        setupCif(alphaTestUser);

        //update eid status
        updateEidStatus(alphaTestUser);

        //check scope has updated to accounts automatically
        assertAccountScope(alphaTestUser);

        this.alphaTestUserStatic = alphaTestUser;
    }

    public void setUpChildBankingCustomer(AlphaTestUser alphaTestUserChild, AlphaTestUser alphaTestUserParent, String relationshipId) {

        setupIdv(alphaTestUserChild);

        //employment set to OTHER for child
        setupEmploymentChild(alphaTestUserChild);

        //update customer information
        updateChildInformation(alphaTestUserChild, false);

        //create customer cif
        setupCif(alphaTestUserChild);

        //patch kid with banking info
        patchChildWithBankingInfo(alphaTestUserParent, relationshipId);

        //update eid status
        updateEidStatus(alphaTestUserChild);

        //check scope has updated to accounts automatically
        assertAccountScope(alphaTestUserChild);

        this.alphaTestUserStatic = alphaTestUserChild;
    }

    public void updateCustomerIdpStateStatus(AlphaTestUser alphaTestUser) {
        OBWritePartialCustomer1 eidStatus = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder().customerState(OBCustomerStateV1.IDV_COMPLETED).build()).build();
        this.customerApiV2.updateCustomer(alphaTestUser, eidStatus , 200);
    }

    public void setupCrs(AlphaTestUser alphaTestUser) {
        var crsResponse1 =
                this.customerApiV2.addCRSDetails(alphaTestUser,
                        OBWriteCRS2.builder()
                                .data(OBCRSData2.builder()
                                        .taxResidencyCountry(List.of(OBTaxResidencyCountry2.builder()
                                                .country("AE")
                                                .missingTinReason("Country doesn't issue TINs to its " +
                                                        "residents")
                                                .build()))
                                        .uaeResidencyByInvestmentScheme(true)
                                        .otherResidencyJurisdictions(true)
                                        .personalIncomeTaxJurisdictions(List.of("AE", "CN"))
                                        .agreedCertification(true)
                                        .build())
                                .build());
    }

    public OBWriteEmploymentDetailsResponse1 setupEmployment(AlphaTestUser alphaTestUser) {
        OBEmploymentDetails1 employmentDetails1 = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.EMPLOYED)
                .companyName(generateEnglishRandomString(10))
                .monthlyIncome("AED " + generateRandomNumeric(4))
                .incomeSource(generateEnglishRandomString(10))
                .businessCode(generateRandomBusinessCode())
                .designationLAPSCode(generateRandomLAPSCode())
                .professionCode(generateRandomProfessionCode())
                .build();

        OBWriteEmploymentDetailsResponse1 customerEmploymentDetails = this.employmentApi.createEmploymentDetails(alphaTestUser, employmentDetails1);


        assertNotNull(customerEmploymentDetails);
        return customerEmploymentDetails;
    }

    public OBWriteEmploymentDetailsResponse1 setupEmploymentChild(AlphaTestUser alphaTestUser) {
        OBEmploymentDetails1 employment = OBEmploymentDetails1.builder()
                .employmentStatus(OBEmploymentStatus.OTHER)
                .companyName(generateEnglishRandomString(10))
                .monthlyIncome("AED " + generateRandomNumeric(4))
                .incomeSource(generateEnglishRandomString(10))
                .businessCode(generateRandomBusinessCode())
                .designationLAPSCode(generateRandomLAPSCode())
                .professionCode(generateRandomProfessionCode())
                .build();

        OBWriteEmploymentDetailsResponse1 customerEmploymentDetails = this.employmentApi.createEmploymentDetails(alphaTestUser, employment);

        assertNotNull(customerEmploymentDetails);
        return customerEmploymentDetails;
    }

    public OBWriteIdvDetailsResponse1 setupIdv(AlphaTestUser alphaTestUser) {
        // complete IDV
        return customerApiV2.createCustomerIdvDetails(alphaTestUser);
    }

    public void setUpAccount(AlphaTestUser alphaTestUser) {
        setUpBankingCustomer(alphaTestUser);
        createValidAccounts();
    }

    public OBWriteCustomerResponse1 updateCustomerInformation(AlphaTestUser alphaTestUser, boolean includeMobileEmail) {

        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName(generateRandomString(5))
                        .lastName(generateRandomString(10))
                        .preferredName("Test" + generateRandomString(5))
                        .dateOfBirth(LocalDate.now().minusYears(30))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language(generateRandomLanguage())
                        .mobileNumber(includeMobileEmail ? alphaTestUser.getUserTelephone() : null)
                        .email(includeMobileEmail ? alphaTestUser.getUserEmail() : null)
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .gender(alphaTestUser.getGender())
                        .nationality("AE") //TODO:: Fails when not set
                        .address(OBPartialPostalAddress6.builder()
                                .subDepartment("Al Bahr Towers")
                                .streetName("Sheikh Zayed")
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber(generateRandomBuildingNumber())
                                .country("AE")
                                .countrySubDivision(generateRandomCountrySubDivision())
                                .townName(generateRandomTownName())
                                .build())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();
        return this.customerApiV2.updateCustomer(alphaTestUser, customer, 200);

    }

    public OBWriteCustomerResponse1 updateChildInformation(AlphaTestUser alphaTestUserChild, boolean includeMobileEmail) {
        //TODO:: NOT ALL THIS INFO SHOULD BE SENT
        //update customer information
        OBWritePartialCustomer1 customer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .firstName(generateRandomString(5))
                        .lastName(generateRandomString(10))
                        .preferredName("Test" + generateRandomString(5))
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        //defect https://ahbdigitalbank.atlassian.net/browse/AHBDB-10977
                        .mobileNumber(alphaTestUserChild.getUserTelephone())
                        .email(alphaTestUserChild.getUserEmail())
                        .termsAccepted(Boolean.TRUE)
                        .termsVersion(LocalDate.now())
                        .gender(alphaTestUserChild.getGender())
                        .nationality("AE") //TODO:: Fails when not set
                        .address(OBPartialPostalAddress6.builder()
                                .addressLine(List.of(generateRandomString(10),
                                        generateRandomString(5)))
                                .buildingNumber("101")
                                .country("AE")
                                .countrySubDivision("Dubai")
                                .postalCode("123456") //TODO::Remove
                                .build())
                        .customerState(OBCustomerStateV1.IDV_COMPLETED)
                        .build())
                .build();

        return this.customerApiV2.updateCustomer(alphaTestUserChild,
                customer,
                200);

    }

    public void updateEidStatus(AlphaTestUser alphaTestUser) {
        OBWriteEIDStatus1 eidStatus = OBWriteEIDStatus1.builder()
                .data(OBWriteEIDStatus1Data.builder()
                        .status(OBEIDStatus.VALID)
                        .build())
                .build();
        this.customerApiV2.updateCustomerValidations(alphaTestUser, eidStatus);
    }

//    public void updatePPStatus(AlphaTestUser alphaTestUser) {
//         eidStatus = OBWriteEIDStatus1.builder()
//                .data(OBWriteEIDStatus1Data.builder()
//                        .status(OBEIDStatus.VALID)
//                        .build())
//                .build();
//        this.customerApiV2.updateCustomerValidations(alphaTestUser, eidStatus);
//    }


    public void assertAccountScope(AlphaTestUser alphaTestUser) {
        await().atMost(10, SECONDS).with()
                .pollInterval(1, SECONDS)
                .pollDelay(2, SECONDS)
                .untilAsserted(() ->
                {
                    UserLoginResponseV2 loginResponse = this.authenticateApi.loginUser(alphaTestUser);
                    if (loginResponse != null && loginResponse.getAccessToken() != null) {
                        assertTrue(loginResponse.getScope().toLowerCase().contains("accounts"));
                    } else {
                        fail();
                    }
                    parseLoginResponse(alphaTestUser, loginResponse);
                });
    }

    public OBCustomer1 setupCif(AlphaTestUser alphaTestUser) {
        final OBCustomer1 customerUpdatedWithCif =
                this.customerApiV2.putCustomerCif(alphaTestUser)
                        .getData().getCustomer().get(0);

        assertNotNull(customerUpdatedWithCif);

        return customerUpdatedWithCif;
    }

    public void createValidAccounts() {
        OBWriteAccountResponse1 savings = accountApi.createCustomerSavingsAccount(alphaTestUserStatic);
        Assertions.assertNotNull(savings);
        alphaTestUserStatic.setAccountNumber(savings.getData().getAccountId());
    }


    public static AlphaTestUser parseLoginResponse(AlphaTestUser atu, UserLoginResponseV2 loginResponse) {
        return TokenUtils.parseLoginResponse(atu, loginResponse);
    }

    public static AlphaTestUser parseLoginResponse(AlphaTestUser alphaTestUser, LoginResponseV1 loginResponse) {
        return TokenUtils.parseLoginResponse(alphaTestUser, loginResponse);
    }

    public void patchChildWithBankingInfo(AlphaTestUser alphaTestUser, String relationshipId) {
        OBWritePartialCustomer1 patchCustomer = OBWritePartialCustomer1.builder()
                .data(OBWritePartialCustomer1Data.builder()
                        .preferredName(alphaTestUser.getName())
                        .dateOfBirth(alphaTestUser.getDateOfBirth())
                        .emailState(OBWriteEmailState1.NOT_VERIFIED)
                        .language("en")
                        .gender(alphaTestUser.getGender())
                        .build())
                .build();

        final OBWriteCustomerResponse1 patchResponse = this.customerApiV2.patchChildSuccess(alphaTestUser,
                patchCustomer,relationshipId);

        assertNotNull(patchResponse);

    }
}
