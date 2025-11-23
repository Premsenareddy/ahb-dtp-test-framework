package ahb.experience.onboarding.DocumentGeneration.api;

import ahb.experience.onboarding.DocumentGeneration.DocumentList;
import ahb.experience.onboarding.ExperienceErrValidations;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseStep;

import javax.inject.Inject;

import java.util.Map;

import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.restassured.RestAssured.given;


public class DocumentsAdultApis extends BaseStep<DocumentsAdultApis> {


    @Inject
    AuthConfiguration authConfiguration;

    private final String GETDOCUMENTLIST_URL = "/onboarding/protected/documents";

    public DocumentsAdultApis captureAdultDocList(String BearerToken,Map<String, String> queryParams,int StatusCode) {
        documentList = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +GETDOCUMENTLIST_URL)
                .then().log().all().statusCode(StatusCode).assertThat()
                .extract().body().as(DocumentList.class);

        return this;
    }

    public int openAdultDoc(String BearerToken,Map<String, String> queryParams,String strDocID) {

         return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                 .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +GETDOCUMENTLIST_URL+"/"+strDocID)
                 .getStatusCode();
    }

    public DocumentsAdultApis captureAdultDocListErr(String BearerToken, Map<String, String> queryParams) {

        experienceErrValidations = given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .queryParams(queryParams)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(APPLICATION_JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() +GETDOCUMENTLIST_URL)
                .then().extract().body().as(ExperienceErrValidations.class);

        return this;
    }

    @Override
    protected DocumentsAdultApis getThis() {
        return this;
    }
}
