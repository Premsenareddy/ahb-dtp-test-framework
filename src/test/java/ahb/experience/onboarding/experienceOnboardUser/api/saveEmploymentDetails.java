package ahb.experience.onboarding.experienceOnboardUser.api;

import ahb.experience.onboarding.ExperienceEmploymentDetails;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class saveEmploymentDetails extends BaseApi {


    @Inject
    AuthConfiguration authConfiguration;
    ExperienceEmploymentDetails experienceEmploymentDetails;

    private final String SAVEEMPLOYMENTDETAILS_URL = "/onboarding/protected/employment";


    public void saveEmpDetails(String BearerToken, String strEmpStatus, String strCompanyName, String strEmpCode, float fMontlyIncome, String IncomeSrc, String strBusinessCode, String strDesgnLapseCode, String ProfCode, String OtherSrcIncome, Map<String, String> queryParams, int StatucCode) {
        experienceEmploymentDetails = ExperienceEmploymentDetails.builder().employmentStatus(strEmpStatus).companyName(strCompanyName).
                employerCode(strEmpCode).monthlyIncome(fMontlyIncome).incomeSource(IncomeSrc).businessCode(strBusinessCode)
                .designationLapsCode(strDesgnLapseCode).professionCode(ProfCode)
                .otherSourceOfIncome(OtherSrcIncome)
                .build();
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .contentType(ContentType.JSON)
                .queryParams(queryParams)
                .body(experienceEmploymentDetails)
                .when()
                .post(authConfiguration.getExperienceBasePath() +SAVEEMPLOYMENTDETAILS_URL)
                .then().log().all().statusCode(StatucCode).assertThat();
    }
}
