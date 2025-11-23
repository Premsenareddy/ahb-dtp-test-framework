package ahb.experience.borrow.creditCards.api;

import ahb.experience.creditCard.ExpCardTransaction;
import ahb.experience.creditCard.ExperienceGetCreditCard;
import io.restassured.http.ContentType;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CardService extends BaseApi  {

    @Inject
    AuthConfiguration authConfiguration;

    private final String GET_CARDS = "/cards/protected/cards";
    private final String FREEZE_UNFREEZE_CARD = "/cards/protected/freezeUnfreeze";
    private final String GET_CARD_TRANSACTIONS = "/cards/protected/cards/{cardID}/transactions";


    public ExperienceGetCreditCard getCards(String BearerToken, String cardType, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .queryParams("type", cardType)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + GET_CARDS)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(ExperienceGetCreditCard.class);
    }

    public ExpCardTransaction getCardTransaction(String BearerToken,String cardID, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .pathParams("cardID", cardID)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + GET_CARD_TRANSACTIONS)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(ExpCardTransaction.class);
    }

    public ExpCardTransaction getCardTransaction(String BearerToken,String cardID, Map<String, String> queryParams, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + BearerToken)
                .header("x-api-key", authConfiguration.getExperienceApiKey())
                .pathParams("cardID", cardID)
                .queryParams(queryParams)
                .contentType(ContentType.JSON)
                .when()
                .get(authConfiguration.getExperienceBasePath() + GET_CARD_TRANSACTIONS)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(ExpCardTransaction.class);
    }


}
