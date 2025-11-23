package uk.co.deloitte.banking.cards.api;

import groovy.lang.Singleton;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.limits.WriteCardLimits1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.parameters.WriteCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;

@Singleton
@Slf4j
public class CardsRelationshipApi extends BaseApi {

    @Inject
    CardsConfiguration cardsConfiguration;

    private final String GET_CARDS_RELATIONSHIP = "/internal/v1/relationships/{relationshipId}/cards";
    private final String GET_CVV_RELATIONSHIP = "/internal/v1/relationships/{relationshipId}/cards/{cardId}/cvv";
    private final String RELATIONSHIP_PHYSICAL_CARD = "/internal/v1/relationships/{relationshipId}/cards/{cardId}/physicalcards";
    private final String RELATIONSHIPS_SET_PIN = "/internal/v1/relationships/{relationshipId}/cards/{cardId}/pin";
    private final String RELATIONSHIP_UPDATE_PARAMETERS = "/internal/v1/relationships/{relationshipId}/cards/{cardId}/parameters";
    private final String RELATIONSHIP_GET_PARAMETERS = "/internal/v1/relationships/{relationshipId}/cards/{cardId}/parameters";
    private final String RELATIONSHIP_LIMITS= "/internal/v1/relationships/{relationshipId}/cards/{cardId}/limits";
    private final String RELATIONSHIP_REQUEST = "/internal/v1/relationships/";

    public CreateCard1Response createVirtualDebitCardForRelationship(final AlphaTestUser alphaTestUser, String relationshipId,
                                                                     final CreateCard1 createCard1) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(createCard1)
                .when()
                .post(cardsConfiguration.getBasePath() + RELATIONSHIP_REQUEST + relationshipId + "/cards")
                .then().log().all().statusCode(201).extract().body().as(CreateCard1Response.class);
    }

    public OBErrorResponse1 createVirtualDebitCardForRelationshipError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                                       final CreateCard1 createCard1, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(createCard1)
                .when()
                .post(cardsConfiguration.getBasePath() + RELATIONSHIP_REQUEST + relationshipId + "/cards")
                .then().log().all().statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 createVirtualDebitCardForRelationshipError(final AlphaTestUser alphaTestUser, String relationshipId,
                                                                       final JSONObject jsonObject, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(jsonObject)
                .when()
                .post(cardsConfiguration.getBasePath() + RELATIONSHIP_REQUEST + relationshipId + "/cards")
                .then().log().all().statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }


    public void activateDebitCardForRelationship(final AlphaTestUser alphaTestUser, final WriteCardActivation1 activateCard1, String relationshipId, String cardId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(activateCard1)
                .when()
                .patch(cardsConfiguration.getBasePath() + RELATIONSHIP_REQUEST + relationshipId + "/cards/" + cardId)
                .then().log().all().statusCode(statusCode);
    }

    public OBErrorResponse1 activateDebitCardForRelationshipError(final AlphaTestUser alphaTestUser, final WriteCardActivation1 activateCard1, String relationshipId, String cardId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(activateCard1)
                .when()
                .patch(cardsConfiguration.getBasePath() + RELATIONSHIP_REQUEST + relationshipId + "/cards/" + cardId)
                .then().log().all().statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }


    public ReadCard1 fetchCardForRelationship(final AlphaTestUser alphaTestUser, final String relationshipId, final String type, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .pathParams("relationshipId", relationshipId)
                .param("type", type)
                .get(cardsConfiguration.getBasePath() + GET_CARDS_RELATIONSHIP)
                .then().log().all()
                .statusCode(statusCode).extract().as(ReadCard1.class);

    }

    public OBErrorResponse1 fetchCardForRelationshipError(final AlphaTestUser alphaTestUser, final String relationshipId, final String type, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .pathParams("relationshipId", relationshipId)
                .param("type", type)
                .get(cardsConfiguration.getBasePath() + GET_CARDS_RELATIONSHIP)
                .then().log().all()
                .statusCode(statusCode).extract().as(OBErrorResponse1.class);

    }

    public void fetchCardForRelationshipErrorVoid(final AlphaTestUser alphaTestUser, final String relationshipId, final String type, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .pathParams("relationshipId", relationshipId)
                .param("type", type)
                .get(cardsConfiguration.getBasePath() + GET_CARDS_RELATIONSHIP)
                .then().log().all()
                .statusCode(statusCode);

    }

    public ReadCardCvv1 fetchCVVForRelationship(AlphaTestUser alphaTestUser, String relationshipId, String cardId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CVV_RELATIONSHIP)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(ReadCardCvv1.class);

    }

    public OBErrorResponse1 fetchCVVForRelationshipError(AlphaTestUser alphaTestUser, String relationshipId, String cardId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CVV_RELATIONSHIP)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);

    }

    public void fetchCVVForRelationshipVoid(AlphaTestUser alphaTestUser, String relationshipId, String cardId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CVV_RELATIONSHIP)
                .then().log().all().statusCode(statusCode);

    }

    public void createPhysicalCardRelationship(AlphaTestUser alphaTestUser, WritePhysicalCard1 writePhysicalCard1, String relationshipId, String cardId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .body(writePhysicalCard1)
                .post(cardsConfiguration.getBasePath() + RELATIONSHIP_PHYSICAL_CARD)
                .then()
                .log().all().statusCode(statusCode);

    }

    public void setDebitCardPinForRelationship(final AlphaTestUser alphaTestUser, final WriteCardPinRequest1 writeCardPinRequest1, String relationshipId, String cardId,
                                               int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .body(writeCardPinRequest1)
                .put(cardsConfiguration.getBasePath() + RELATIONSHIPS_SET_PIN)
                .then().log().all()
                .statusCode(statusCode);

    }

    public void updateCardParametersRelationship(AlphaTestUser alphaTestUser, WriteCardParameters1 writeCardParameters1, String relationshipId, String cardId,
                                                 int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .body(writeCardParameters1)
                .put(cardsConfiguration.getBasePath() + RELATIONSHIP_UPDATE_PARAMETERS)
                .then().log().all()
                .statusCode(statusCode);
    }

    public ReadCardParameters1 getCardParametersRelationship(AlphaTestUser alphaTestUser, String relationshipId, String cardId) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + RELATIONSHIP_GET_PARAMETERS)
                .then().log().all()
                .statusCode(200).assertThat().extract().body().as(ReadCardParameters1.class);
    }

    public ReadCardLimits1 getRelationshipCardsLimits(AlphaTestUser alphaTestUser , String relationshipId, String cardId, String typeOfTransaction, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .queryParam("transactionType", typeOfTransaction)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + RELATIONSHIP_LIMITS)
                .then().log().all()
                .statusCode(statusCode).extract().as(ReadCardLimits1.class);
    }

    public OBErrorResponse1 getRelationshipCardsLimitsError(AlphaTestUser alphaTestUser , String relationshipId, String cardId, String typeOfTransaction, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .queryParam("transactionType", typeOfTransaction)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + RELATIONSHIP_LIMITS)
                .then().log().all()
                .statusCode(statusCode).extract().as(OBErrorResponse1.class);
    }

    public void getRelationshipCardsLimitsErrorVoid(AlphaTestUser alphaTestUser , String relationshipId, String cardId, String typeOfTransaction, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .queryParam("transactionType", typeOfTransaction)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + RELATIONSHIP_LIMITS)
                .then().log().all()
                .statusCode(statusCode);
    }

    public void updateCardLimitsRelationship(final AlphaTestUser alphaTestUser, final WriteCardLimits1 cardLimits, String relationshipId, String cardId,
                                             int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("relationshipId", relationshipId)
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when()
                .body(cardLimits)
                .put(cardsConfiguration.getBasePath() + RELATIONSHIP_LIMITS)
                .then().log().all()
                .statusCode(statusCode);
    }


}
