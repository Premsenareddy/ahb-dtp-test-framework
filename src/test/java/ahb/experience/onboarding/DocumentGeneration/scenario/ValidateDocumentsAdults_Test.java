package ahb.experience.onboarding.DocumentGeneration.scenario;

import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.DocumentGeneration.DocumentFile;
import ahb.experience.onboarding.DocumentGeneration.api.DocumentsAdultApis;
import ahb.experience.onboarding.DocumentGeneration.util.DocumentGenSteps;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.childMarketPlace.utils.ChildOnboardingSteps;
import ahb.experience.onboarding.experienceOnboardUser.utils.*;
import ahb.experience.onboarding.response.Child.Child;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static ahb.experience.onboarding.childMarketPlace.data.AdultOnboardingBuilders.bankingUserOnboardingBuilder;
import static ahb.experience.onboarding.childMarketPlace.data.ChildOnboardingBuilders.childUserOnboardingData;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;

@MicronautTest
@Slf4j
@Singleton
public class ValidateDocumentsAdults_Test {

    @Inject
    ExperienceDebitCardSetup experienceDebitCardSetup;
    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;
    @Inject
    ExperienceMarketPlaceUser experienceMarketPlaceUser;
    @Inject
    ExperienceBankAccountUser experienceBankAccountUser;
    @Inject
    DocumentsAdultApis adultDocuments;
    @Inject
    ChildOnboardingSteps childOnboardingSteps;
    @Inject
    DocumentGenSteps documentGenSteps;

    private ExperienceTestUser experienceTestUser;
    String strErrMsg = "Invalid Customer EID Status for this operation.";

    public void setupTestUser() {
        experienceTestUser = new ExperienceTestUser();
    }

    /**
     * This test is to validate documents for adult when EID status is VALID
     */
    @Order(1)
    @Test
    public void docGenerationAdult_EIDVALID() {
        TEST("AHBDB-22482: This test is to validate documents for adult when EID status is VALID");
        setupTestUser();

        String strAdultDocTypes="AC_OPEN_CURRENT.pdf,AC_OPEN_SAVINGS.pdf,CRS.pdf,IBAN_CURRENT.pdf,IBAN_SAVINGS.pdf,W8.pdf";

        GIVEN("User have created a valid test user with EID Status as VALID");
        ExperienceLoginResponse loginResponse = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);

        WHEN("User has captured a document list");
        adultDocuments = adultDocuments.captureAdultDocList(loginResponse.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""),200);

        THEN("6 Documents should be genrated for Adult");
        Assert.assertTrue(adultDocuments.documentList.getData().getDocumentFiles().size()==6);

        THEN("Verify the type of Documents generated");
        ArrayList<DocumentFile> docList = adultDocuments.documentList.getData().getDocumentFiles();
        for(DocumentFile doc: docList){
            Assert.assertTrue(doc.getName()+" is NOT found",strAdultDocTypes.toLowerCase().contains(doc.getName().toLowerCase()));
        }

        THEN("Verify user can access each type of Doc");
        ArrayList<DocumentFile> docList_new = adultDocuments.documentList.getData().getDocumentFiles();
        for(DocumentFile doc: docList_new){
            Assert.assertTrue(doc.getName()+"Document is not accessible",adultDocuments.openAdultDoc(loginResponse.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""),doc.getId())==200);
        }
        DONE();
    }

    /**
     * This test is to validate documents for Banking adult when EID status is PENDING
     */
    @Order(2)
    @Test
    public void docGenerationAdult_EIDPENDING() {
        TEST("AHBDB-22482: This test is to validate documents for Banking adult when EID status is PENDING");
        setupTestUser();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "ADULT");

        GIVEN("User have created a valid test user with EID Status as PENDING");
        experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        ExperienceLoginResponse loginResponse = experienceBankAccountUser
                .bankAdultUserOnboard(bankingUserOnboardingBuilder.get(), experienceTestUser, queryParams);

        WHEN("User do not have documents linked");
        adultDocuments = adultDocuments.captureAdultDocListErr(loginResponse.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""));

        THEN("User should get error message on the basis of EID status");
        Assert.assertTrue("Invalid Message:'"+adultDocuments.experienceErrValidations.getMessage()+"'",adultDocuments.experienceErrValidations.getMessage().equalsIgnoreCase(strErrMsg));

        DONE();
    }

    /**
     * This test is to validate documents for Banking adult when EID status is INVALID
     */
    @Order(3)
    @Test
    public void docGenerationAdult_EIDINVALID() {
        TEST("AHBDB-22482: This test is to validate documents for Banking adult when EID status is INVALID");
        setupTestUser();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "ADULT");

        GIVEN("User have created a valid test user with EID Status as INVALID");
        experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        ExperienceLoginResponse loginResponse = experienceBankAccountUser
                .bankAdultUserOnboard(bankingUserOnboardingBuilder.get(), experienceTestUser, queryParams);
        experienceDebitCardSetup.setUpDebitCard(queryParams,"Failure","fail");

        WHEN("User do not have documents linked");
        adultDocuments = adultDocuments.captureAdultDocListErr(loginResponse.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""));

        THEN("User should get error message on the basis of EID status");
        Assert.assertTrue("Invalid Message:'"+adultDocuments.experienceErrValidations.getMessage()+"'",adultDocuments.experienceErrValidations.getMessage().equalsIgnoreCase(strErrMsg));

        DONE();
    }

    /**
     * This test is to validate documents for MarketPlace adult
     */
    @Order(4)
    @Test
    public void docGenerationMarketPlaceAdult_EIDPENDING() {
        TEST("AHBDB-22482: This test is to validate documents for MarketPlace adult");
        setupTestUser();
        GIVEN("User have created a marketplace user with EID Status as PENDING");
        ExperienceLoginResponse loginResponse =experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);

        WHEN("User do not have documents linked");
        adultDocuments = adultDocuments.captureAdultDocListErr(loginResponse.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""));

        THEN("User should get error message on the basis of EID status");
        Assert.assertTrue("Invalid Message:'"+adultDocuments.experienceErrValidations.getMessage()+"'",adultDocuments.experienceErrValidations.getMessage().equalsIgnoreCase(strErrMsg));

        DONE();
    }

    /**
     * This test is to validate user1 can not access document of user2
     */
    @Order(5)
    @Test
    public void docGenerationAdult_AccessRestriction(){
        TEST("AHBDB-22482: This test is to validate user1 can not access document of user2");
        setupTestUser();

        GIVEN("User have created two adult bank account for user1 and user2 with EID Status as VALID");
        ExperienceLoginResponse loginResponse_user1 = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);

        WHEN("User1 document list is captured");
        DocumentsAdultApis docListObj_user1= adultDocuments.captureAdultDocList(loginResponse_user1.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""),200);
        ArrayList<DocumentFile> docList_user1 = docListObj_user1.documentList.getData().getDocumentFiles();

        setupTestUser();
        ExperienceLoginResponse loginResponse_user2 = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);
        WHEN("User2 document list is captured");
        DocumentsAdultApis docListObj_user2 = adultDocuments.captureAdultDocList(loginResponse_user2.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""),200);

        THEN("Verify user2 can not access Doc of user1");

        for(DocumentFile doc: docList_user1){
            int actStatusCode = adultDocuments.openAdultDoc(loginResponse_user2.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""),doc.getId());
            Assert.assertTrue("User 2 can access doc of user 1, Status code is: "+actStatusCode,actStatusCode==401 || actStatusCode==404);
        }
        THEN("Verify user1 can not access Doc of user2");
        ArrayList<DocumentFile> docList_user2 = docListObj_user2.documentList.getData().getDocumentFiles();
        for(DocumentFile doc: docList_user2){
            int actStatusCode = adultDocuments.openAdultDoc(loginResponse_user1.getToken().getAccessToken(),documentGenSteps.setQueryParam("adult",""),doc.getId());
            Assert.assertTrue("User 2 can access doc of user 1, Status code is: "+actStatusCode,actStatusCode==401 || actStatusCode==404);
        }
        DONE();
    }

    /**
     * This test is to validate parent can access it's child's documents only
     */
    @Order(6)
    @ParameterizedTest
    @MethodSource("childOnboardingPayloadProvider")
    public void docGenerationAdult_AccessBankChildDocs(final BankingUserOnboardingDto bankingUserOnboardingDto){
        TEST("AHBDB-22482: This test is to validate parent can access it's Bank child's documents only");
        setupTestUser();
        GIVEN("User is a parent with 2 children");
        ExperienceLoginResponse loginResponse_Parent1 = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);

        Child child_1 =childOnboardingSteps
                .childMarketCreateAndLogin(loginResponse_Parent1.getToken().accessToken, bankingUserOnboardingDto);
        childOnboardingSteps
                .childBankOnboarding(loginResponse_Parent1.getToken().accessToken, bankingUserOnboardingDto);
        childOnboardingSteps
                .childDebitCardSetup();

        DocumentsAdultApis DocListObj_Child1 = adultDocuments.captureAdultDocList(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_1.getRelationshipId()),200);

        THEN("Verify parent can access Doc of child1");
        ArrayList<DocumentFile> docList_Child1 = DocListObj_Child1.documentList.getData().getDocumentFiles();
        for(DocumentFile doc: docList_Child1){
            //Pass relationshipID1
            int actStatusCode = adultDocuments.openAdultDoc(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_1.getRelationshipId()),doc.getId());
            Assert.assertTrue("Parent can not access doc of child 1, Status code is: "+actStatusCode,actStatusCode==200);
        }

        bankingUserOnboardingDto.childDetailsReqBody.setEmail(generateRandomEmail());

        Child child_2 =childOnboardingSteps
                .childMarketCreateAndLogin(loginResponse_Parent1.getToken().accessToken, bankingUserOnboardingDto);
        childOnboardingSteps
                .childBankOnboarding(loginResponse_Parent1.getToken().accessToken, bankingUserOnboardingDto);
        childOnboardingSteps
                .childDebitCardSetup();

        DocumentsAdultApis DocListObj_Child2 = adultDocuments.captureAdultDocList(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_2.getRelationshipId()),200);

        THEN("Verify parent can access Doc of child2");
        ArrayList<DocumentFile> docList_Child2 = DocListObj_Child2.documentList.getData().getDocumentFiles();
        for(DocumentFile doc: docList_Child2){
            //Pass relationshipID2
            int actStatusCode = adultDocuments.openAdultDoc(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_2.getRelationshipId()),doc.getId());
            Assert.assertTrue("Parent can not access doc of child 1, Status code is: "+actStatusCode,actStatusCode==200);
        }

        WHEN("Another Parent(Paren2) is created with single child");
        setupTestUser();
        ExperienceLoginResponse loginResponse_Parent2 = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);
        bankingUserOnboardingDto.childDetailsReqBody.setEmail(generateRandomEmail());
        Child child_3 =childOnboardingSteps
                .childMarketCreateAndLogin(loginResponse_Parent2.getToken().accessToken, bankingUserOnboardingDto);
        childOnboardingSteps
                .childBankOnboarding(loginResponse_Parent2.getToken().accessToken, bankingUserOnboardingDto);
        childOnboardingSteps
                .childDebitCardSetup();

        DocumentsAdultApis DocListObj_Child3 = adultDocuments.captureAdultDocList(loginResponse_Parent2.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_3.getRelationshipId()),200);

        THEN("Verify Parent1 can not access document list of Child of Parent2");
        String errCode = adultDocuments.captureAdultDocListErr(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_3.getRelationshipId())).experienceErrValidations.getCode();
        Assert.assertTrue("error code was "+errCode ,errCode.equalsIgnoreCase("login_failed"));

        THEN("Verify Parent1 can not open/access document of Child of Parent2");
        ArrayList<DocumentFile> docList_Child3 = DocListObj_Child3.documentList.getData().getDocumentFiles();
        for(DocumentFile doc: docList_Child3){
            int actStatusCode = adultDocuments.openAdultDoc(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child_3.getRelationshipId()),doc.getId());
            Assert.assertTrue("Parent can not access doc of child 1, Status code is: "+actStatusCode,actStatusCode==401);
        }
        DONE();
    }

    /**
     * This test is to Validate parent can not have documents when child is marketplace user
     */
    @Order(7)
    @ParameterizedTest
    @MethodSource("childOnboardingPayloadProvider")
    public void docGenerationAdult_AccessMarketPlaceChildDocs(final BankingUserOnboardingDto bankingUserOnboardingDto){
        TEST("AHBDB-22482: This test is to Validate parent can not have documents when child is marketplace user");
        setupTestUser();
        GIVEN("User has created a parent with child as marketplace user only");
        ExperienceLoginResponse loginResponse_Parent1 = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);

        Child child = childOnboardingSteps
                .childMarketCreateAndLogin(loginResponse_Parent1.getToken().accessToken, bankingUserOnboardingDto);

        adultDocuments = adultDocuments.captureAdultDocListErr(loginResponse_Parent1.getToken().getAccessToken(),documentGenSteps.setQueryParam("kid",child.getRelationshipId()));
        THEN("Parent should get error for accessing documents for marketplace user");
        Assert.assertTrue("Invalid Message:'"+adultDocuments.experienceErrValidations.getMessage()+"'",adultDocuments.experienceErrValidations.getMessage().equalsIgnoreCase(strErrMsg));

        DONE();
    }

    private static Stream<Arguments> childOnboardingPayloadProvider() {
        return Stream.of(
                Arguments.of(childUserOnboardingData.get())
        );
    }
}
