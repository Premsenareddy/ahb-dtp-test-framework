package uk.co.deloitte.banking.banking.chequebook.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.restassured.http.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;

import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;


import static io.restassured.RestAssured.given;

public class CustomerScoreApi extends BaseApi {

    @Inject
    CustomerConfig customerConfig;

    private final String CUSTOMER_SCORE_URL = "internal/v1/customer-score";


    public String checkEligible(final String token) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " +token)
                .contentType(ContentType.JSON)
                .body(getRequest())
                .when()
                .post(customerConfig.getEligibilityPath() + CUSTOMER_SCORE_URL)
                .then().log().all().statusCode(200).assertThat()
                .extract().contentType();

    }

    private CustomerScore getRequest() {

        return CustomerScore.builder()
                .CIF("1234567")
                .fullNameEN("vani goyal")
                .dob("20000707")
                .referenceNum("DTP-test124g56")
                .emiratesId("784196800622651")
                .gender("M")
                .primaryMobileNo("0581362265")
                .role("")
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CustomerScore {

        @JsonProperty("Role")
        private String role;
        @JsonProperty("EmiratesId")
        private String emiratesId;
        @JsonProperty("CIF")
        private String CIF;
        @JsonProperty("DOB")
        private String dob ;
        @JsonProperty("Gender")
        private String gender;
        @JsonProperty("PrimaryMobileNo")
        private String primaryMobileNo;
        @JsonProperty("ReferenceNum")
        private String referenceNum;
        @JsonProperty("FullNameEN")
        private String fullNameEN;
    }
}
