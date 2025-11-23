package uk.co.deloitte.banking.customer.crmLeads.api;

import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeads.LeadDataReq;
import uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeads.LeadRequest;
import uk.co.deloitte.banking.ahb.dtp.test.customer.crmLeadsResponse.LeadValueRes;
import uk.co.deloitte.banking.base.BaseApi;
import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;

public class LeadsApiV2 extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    @Inject
    LeadDataReq leadsBody;

    @Inject
    LeadRequest leadRequest;



    private static final String PROTECTED_V2_CUSTOMERS_LEADS = "/protected/v2/customers/";


    public LeadValueRes createCrmLead(String userId) {

        //LeadsBody leadsBody = new LeadsBody();
        leadRequest = leadRequest.builder().productType("LOAN").build();
        leadsBody = leadsBody.builder().leadRequest(leadRequest).build();
        //leadsBody.leadRequest.setProductType("LOAN");

         return given()
                .config(config)
                .log().all()
                .header(HEADER_X_API_KEY, customerConfig.getApiKey())
                .contentType(JSON)
                .body(leadsBody)
                .when()
                .post(customerConfig.getBasePath() + PROTECTED_V2_CUSTOMERS_LEADS + userId + "/leads")
                .then()
                .log().all()
                .statusCode(200).assertThat()
                .extract().body().as(LeadValueRes.class);
            }
        };







