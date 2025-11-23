
package ahb.experience.onboarding.experienceOnboardUser.api;

import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.IDNowDocs.DocumentDetails;
import ahb.experience.onboarding.IDNowDocs.IdentificationProcess;
import ahb.experience.onboarding.IDNowDocs.CustomData;
import ahb.experience.onboarding.IDNowDocs.ContactData;
import ahb.experience.onboarding.IDNowDocs.UserData;
import ahb.experience.onboarding.IDNowDocs.IdentificationDocument;
import ahb.experience.onboarding.IDNowDocs.Attachments;
import ahb.experience.onboarding.StatusValue_Object;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class scanDocument extends BaseApi {

    @Inject
    AuthConfiguration authConfiguration;
    DocumentDetails documentDetails;
    IdentificationProcess identificationProcess;
    CustomData customData;
    ContactData contactData;
    UserData userData;
    IdentificationDocument identificationDocument;
    Attachments attachments;

    private final String UPDATEAPPLICANT_URL = "/v1/idv/events";


    String Result = "SUCCESS_DATA_CHANGED";
    String CompanyId = "alhilalpptest";
    String FileName = "dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777.zip";
    String AgentName = null;
    String IdentificationTime =  "2021-10-26T15:21:31+02:00";
    String Href=  "/api/v1/alhilalpptest/identifications/dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777.zip";
    String Type =  "APP";
    String TransactionNumber =  "dc47e427-e381-3d94-ae46-8664cb0b2d0b";
    String Reason= null;
    String TransactNum_CustomData="dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777";
    String status_Change = "CHANGE";
    String status_New = "NEW";
    String Pdf= "dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777.pdf";
    String IdFrontSide= "dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777_original_idfrontside.jpg";
    String Xml= "dc47e427-e381-3d94-ae46-8664cb0b2d0b---25777.xml";


    public void setUserDocumentData(String strFirstName,String strFullName, String strBirthday, String strGender,String strLastName,String strNationality, String strPersonNum){

        StatusValue_Object firstName = StatusValue_Object.builder().Status(status_Change).Value(strFirstName).build();
        StatusValue_Object fullName = StatusValue_Object.builder().Status(status_New).Value(strFullName).build();
        StatusValue_Object birthday = StatusValue_Object.builder().Status(status_New).Value(strBirthday).build();
        StatusValue_Object lastName = StatusValue_Object.builder().Status(status_New).Value(strLastName).build();
        StatusValue_Object gender = StatusValue_Object.builder().Status(status_New).Value(strGender).build();
        StatusValue_Object nationality = StatusValue_Object.builder().Status(status_New).Value(strNationality).build();
        StatusValue_Object personalNum = StatusValue_Object.builder().Status(status_New).Value(strPersonNum).build();

        userData = UserData.builder().firstName(firstName).fullName(fullName).birthday(birthday).lastName(lastName)
                .gender(gender).nationality(nationality).personalNumber(personalNum).build();
    }

    public void setUserIdentityDocumentData(String strType, String strCountry, String strValidUntil, String strNumber, String strDateIssued){

        StatusValue_Object type = StatusValue_Object.builder().Status(status_Change).Value(strType).build();
        StatusValue_Object country = StatusValue_Object.builder().Status(status_New).Value(strCountry).build();
        StatusValue_Object validUntil = StatusValue_Object.builder().Status(status_New).Value(strValidUntil).build();
        StatusValue_Object number = StatusValue_Object.builder().Status(status_New).Value(strNumber).build();
        StatusValue_Object dateIssued = StatusValue_Object.builder().Status(status_New).Value(strDateIssued).build();

        identificationDocument = IdentificationDocument.builder().type(type).country(country).validUntil(validUntil)
                .number(number).DateIssued(dateIssued).build();
    }

    void setDefaultDocumentValues() {
        customData = CustomData.builder().transactionNumber(TransactNum_CustomData).build();
        attachments = Attachments.builder().pdf(Pdf).idFrontSide(IdFrontSide).xml(Xml).build();
    }

    public void setApplicantID(String strApplicantID){
        //identificationProcess = new IdentificationProcess();
        identificationProcess = IdentificationProcess.builder().id(strApplicantID).result(Result).companyId(CompanyId)
                .fileName(FileName).AgentName(AgentName).identificationTime(IdentificationTime)
                .href(Href).type(Type).transactionNumber(TransactionNumber).Reason(Reason).build();
    }

    public void setContactEmail(String strEmailID){
        contactData = ContactData.builder().Email(strEmailID).build();
    }


    public void scanDoc(String BearerToken, Map<String, String> queryParams) {
        setDefaultDocumentValues();
        documentDetails = DocumentDetails.builder().identificationProcess(identificationProcess).customData(customData)
                .contactData(contactData).userData(userData).identificationDocument(identificationDocument).attachments(attachments).build();

        given()
        .config(config)
        .log().all()
        .header("Authorization", "Bearer " + BearerToken)
        .header("x-api-key", authConfiguration.getExperienceApiKey())
        .contentType(ContentType.JSON)
        .body(documentDetails)
        .when()
        .post(idNowConfiguration.getBasePath()+UPDATEAPPLICANT_URL)
        .then().log().all().statusCode(200).assertThat()
        .extract().body().asString().contains("true");

    }
}
