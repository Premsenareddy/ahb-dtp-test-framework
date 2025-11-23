package uk.co.deloitte.banking.customer.fetchEID.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Singleton;
import io.micronaut.http.HttpStatus;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.customer.fetchEID.fetchEIDStatus;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.config.IdNowConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.ApplicantExtractedDTO;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.CreateApplicantRequest;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.DocumentType;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.GetApplicantResponse;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.GetApplicantListResponse;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IDNowValue;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IdentificationProcess;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.IdentificationDocument;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.TokenHolder;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.model.WebhookEvent;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;

@Singleton
@Slf4j

public class FetchEIDAPI extends BaseApi {

    public static final String BEARER = "Bearer ";
    public static final String SCOPE = "Scope";
    public static final String TOKEN_TYPE = "TokenType";
    public static final String ACCESS_TOKEN = "AccessToken";

    public static final String INTERNAL_V2_FETCHEID = "/internal/v2/customers/idvs";
    public static final String DOCUMENT_TYPE = "documentType";



    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private CustomerConfig customerConfig;

    /**
     * @param alphaTestUser
     * @return
     */


    public fetchEIDStatus fetchEID(final AlphaTestUser alphaTestUser) {

        return given().config(config)
                .log().all()
                .header(AUTHORIZATION, "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .queryParam(DOCUMENT_TYPE,"EID")
                .contentType(ContentType.JSON)
                .when()
                .get(customerConfig.getBasePath() + INTERNAL_V2_FETCHEID)
                .then().log().all()
                .statusCode(HttpStatus.OK.getCode())
                .extract().body().as(fetchEIDStatus.class);
    }


}
