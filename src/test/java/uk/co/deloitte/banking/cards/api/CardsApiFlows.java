package uk.co.deloitte.banking.cards.api;

import groovy.lang.Singleton;
import io.micronaut.http.HttpStatus;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.account.api.card.model.ReadCard2;
import uk.co.deloitte.banking.account.api.card.model.WriteCardAccount1;
import uk.co.deloitte.banking.account.api.card.model.activation.WriteCardActivation1;
import uk.co.deloitte.banking.account.api.card.model.parameters.ReadCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.parameters.WriteCardParameters1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCard1;
import uk.co.deloitte.banking.account.api.card.model.physicalcards.WritePhysicalCardResponse1;
import uk.co.deloitte.banking.account.api.card.model.pinvalidation.CardPinValidation1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.configuration.CardsConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.ReadCreditCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.Transactions.CardTransaction;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.activateCard.ActivateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.AccountType;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.CreateCard1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.create.response.CreateCard1Response;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.cvv.ReadCardCvv1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.dailymonthlylimits.WriteDailyMonthlyLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.fetchDigitalCards.FetchDigitalCards1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.fetchDigitalCards.InvalidLegacyCif;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.filters.UpdateCardParameters1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.limits.ReadCardLimits1;
import uk.co.deloitte.banking.ahb.dtp.test.cards.models.setPin.WriteCardPinRequest1;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.ahb.dtp.test.util.RequestUtils;
import uk.co.deloitte.banking.base.BaseApi;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_FAPI_INTERACTION_ID;


//TODO:: Break into smaller api classes based on the sub-domain
@Singleton
@Slf4j
public class CardsApiFlows extends BaseApi {

    @Inject
    CardsConfiguration cardsConfiguration;


    private final String CARDS_FETCH = "/internal/v1/cards?type=debit";
    private final String CARDS_FETCH_V2 = "/internal/v2/cards?type=debit";
    private final String CREDIT_CARD_FETCH_V3 = "/internal/v3/cards?type=credit";
    private final String CREDIT_CARD_SUMMARY_FETCH_V4 = "/internal/v4/cards?type=credit";
    private final String LEGACY_CREDIT_CARD_FETCH_V4 = "/internal/v4/cards?type=credit&legacyCif=";
    private final String CREDIT_CARD_FETCH_V4 = "/internal/v4/cards";
    private final String CARDS_FETCH_NO_TYPE = "/internal/v1/cards?type=";
    private final String CREATE_VIRTUAL_DEBIT_CARD = "/internal/v1/cards";

    private final String SET_CARD_PIN = "/internal/v1/cards/pin";
    private final String ACTIVATE_CARDS = "/internal/v1/cards/activate";
    private final String UPDATE_DEFAULT_CARD = "/internal/v1/cards/{cardId}/accounts";
    private final String CARD_LIMITS = "/internal/v1/cards/limits";
    private final String CREATE_PHYSICAL_CARD = "/internal/v1/cards/{cardId}/physicalcards";
    private final String ACTIVATE_CARDS_PROTECTED = "/protected/v1/cifs/{cif}/cards/{cardId}";
    private final String BLOCK_CARDS_PROTECTED = "/protected/v1/cifs/{cif}/cards/{cardId}/parameters";
    private final String GET_CARDS_PROTECTED = "/protected/v1/cifs/{cif}/cards?type=";
    private final String GET_CARDS_PARAMETERS = "/internal/v1/cards/{cardId}/parameters";
    private final String GET_CARDS_PARAMETERS_V2 = "/internal/v2/cards/{cardId}/parameters";
    private final String PUT_CARD_PARAMETERS_V2 = "/internal/v2/cards/parameters";
    private final String GET_CARDS_CVV_V2 = "/internal/v2/cards/{cardId}/cvv";
    private final String GET_CARD_TRANSACTION = "/internal/v1/cards/{cardId}/transactions";
    private final String GET_CARD_TRANSACTION_V2 = "/internal/v2/cards/{cardId}/transactions";
    private final String GET_CARDS_DETAILS_V1 = "/internal/v1/cards/details";

    public final static String ACCOUNT_TYPE = AccountType.SAVINGS.getDtpValue();

    public ReadCard1 fetchCardsForUser(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CARDS_FETCH)
                .then().log().all().statusCode(200)
                .extract().body().as(ReadCard1.class);

    }


    public FetchDigitalCards1 fetchdigitalCardsForUser(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CREDIT_CARD_SUMMARY_FETCH_V4)
                .then().log().all().statusCode(200)
                .extract().body().as(FetchDigitalCards1.class);

    }


    public FetchDigitalCards1 fetchLegacyCardsForUser(final AlphaTestUser alphaTestUser, String legacyCif) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + LEGACY_CREDIT_CARD_FETCH_V4+legacyCif)
                .then().log().all().statusCode(200)
                .extract().body().as(FetchDigitalCards1.class);

    }

    public InvalidLegacyCif fetchLegacyCardsForInvalidCif(final AlphaTestUser alphaTestUser, String legacyCif) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + LEGACY_CREDIT_CARD_FETCH_V4+legacyCif)
                .then().log().all().statusCode(404)
                .extract().body().as(InvalidLegacyCif.class);

    }

    public CardTransaction fetchTransactionForCard(final AlphaTestUser alphaTestUser, String cardId) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .pathParams("cardId", cardId)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARD_TRANSACTION)
                .then().log().all().statusCode(200)
                .extract().body().as( CardTransaction.class);

    }

    public CardTransaction fetchTransactionForCardV2(final AlphaTestUser alphaTestUser, String cardId) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .pathParams("cardId", cardId)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARD_TRANSACTION_V2)
                .then().log().all().statusCode(200)
                .extract().body().as( CardTransaction.class);

    }

    public CardTransaction fetchTransactionForCardWithFilter(final AlphaTestUser alphaTestUser, String cardId, String fromDateTime, int maxAmount, int minAmount, String toDateTime, String  creditDebitIndicator, String transactionBillingStatus) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .param("fromDateTime" , fromDateTime)
                .param("maxAmount" , maxAmount)
                .param("minAmount" , minAmount)
                .param("toDateTime" , toDateTime)
                .param("creditDebitIndicator" , creditDebitIndicator)
                .param("transactionBillingStatus" , transactionBillingStatus)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .pathParams("cardId", cardId)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARD_TRANSACTION)
                .then().log().all().statusCode(200)
                .extract().body().as( CardTransaction.class);

    }

    public CardTransaction fetchTransactionForCardWithFilterV2(final AlphaTestUser alphaTestUser, String cardId, String fromDateTime, int maxAmount, int minAmount, String toDateTime, String  creditDebitIndicator, String transactionBillingStatus) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .param("fromDateTime" , fromDateTime)
                .param("maxAmount" , maxAmount)
                .param("minAmount" , minAmount)
                .param("toDateTime" , toDateTime)
                .param("creditDebitIndicator" , creditDebitIndicator)
                .param("transactionBillingStatus" , transactionBillingStatus)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .pathParams("cardId", cardId)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARD_TRANSACTION_V2)
                .then().log().all().statusCode(200)
                .extract().body().as( CardTransaction.class);

    }
    public CardTransaction fetchTransactionForCardWithFilter(final AlphaTestUser alphaTestUser, String cardId, String fromDateTime, String toDateTime, String  creditDebitIndicator) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .param("fromDateTime" , fromDateTime)
                .param("toDateTime" , toDateTime)
                .param("creditDebitIndicator" , creditDebitIndicator)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .pathParams("cardId", cardId)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARD_TRANSACTION)
                .then().log().all().statusCode(200)
                .extract().body().as( CardTransaction.class);

    }

    public OBErrorResponse1 fetchTransactionForCardError(final AlphaTestUser alphaTestUser, String cardId, int statusCode) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .pathParams("cardId", cardId)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARD_TRANSACTION)
                .then().log().all().statusCode(statusCode)
                .extract().body().as( OBErrorResponse1.class);

    }

    public ReadCreditCard1 fetchCreditCardsForUser(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CREDIT_CARD_FETCH_V3)
                .then().log().all().statusCode(200)
                .extract().body().as(ReadCreditCard1.class);

    }

    public void putCardControl(final AlphaTestUser alphaTestUser, HashMap<String, Object> request, String prams) {
        given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .param("type", prams)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(cardsConfiguration.getBasePath() + PUT_CARD_PARAMETERS_V2)
                .then().log().all().statusCode(200);

    }

    public <T> T fetchCreditCardsForUserV4(final AlphaTestUser alphaTestUser, Map<String, String> queryParams, final Class <T> classType, HttpStatus status) {
        return (T) given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .queryParams(queryParams)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CREDIT_CARD_FETCH_V4)
                .then().log().all().statusCode(status.getCode())
                .extract().body().as(classType);

    }
    public OBErrorResponse1 fetchCreditCardError(final AlphaTestUser alphaTestUser, int statusCode) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CREDIT_CARD_FETCH_V3)
                .then().log().all().statusCode(statusCode)
                .extract().body().as( OBErrorResponse1.class);

    }

    public ReadCard2 fetchCardsForUserV2(final AlphaTestUser alphaTestUser) {
        return given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CARDS_FETCH_V2)
                .then().log().all().statusCode(200)
                .extract().body().as(ReadCard2.class);

    }

    public OBErrorResponse1 getCardsError(final AlphaTestUser alphaTestUser, final String cardType,
                                          final int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CARDS_FETCH_NO_TYPE + cardType)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public void getCardsErrorVoid(final AlphaTestUser alphaTestUser, final String cardType, final int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + CARDS_FETCH_NO_TYPE + cardType)
                .then().log().all().statusCode(statusCode);
    }

    public ReadCard1 fetchCardsForUserProtected(final String cif, final String cardType) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .pathParams("cif", cif)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PROTECTED + cardType)
                .then().log().all().statusCode(200)
                .extract().body().as(ReadCard1.class);

    }

    public OBErrorResponse1 getCardsErrorProtected(final String cif, final String cardType,
                                                   final int statusCode) {
        return given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .log().all()
                .pathParams("cif", cif)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PROTECTED + cardType)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public void getCardsErrorNoApiKey(final String cif, final String cardType, final int statusCode) {
        given()
                .config(config)
                .log().all()
                .pathParams("cif", cif)
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PROTECTED + cardType)
                .then().log().all().statusCode(statusCode);
    }

    public ReadCardCvv1 fetchCardsCvvForUser(final AlphaTestUser alphaTestUser, final String cardId) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/cvv")
                .then().log().all().statusCode(200)
                .extract().body().as(ReadCardCvv1.class);
    }

    public ReadCardCvv1 fetchCardsCvvForUserV2(final AlphaTestUser alphaTestUser, final String cardId, String type) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when().param("type", "credit")
                .get(cardsConfiguration.getBasePath() + GET_CARDS_CVV_V2)
                .then().log().all().statusCode(200)
                .extract().body().as(ReadCardCvv1.class);
    }

    public OBErrorResponse1 fetchCardsCvvForUserV2Error(final AlphaTestUser alphaTestUser, final String cardId, String type, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("cardId", cardId)
                .contentType(ContentType.JSON)
                .when().param("type", "credit")
                .get(cardsConfiguration.getBasePath() + GET_CARDS_CVV_V2)
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 fetchCardsCvvForUserError(final AlphaTestUser alphaTestUser, final String cardId,
                                                      final int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/cvv")
                .then().log().all().statusCode(statusCode)
                .extract().body().as(OBErrorResponse1.class);
    }

    public void fetchCardsCvvForUserErrorVoid(final AlphaTestUser alphaTestUser, final String cardId,
                                              final int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/cvv")
                .then().log().all().statusCode(statusCode);
    }

    public CreateCard1Response createVirtualDebitCard(final AlphaTestUser alphaTestUser,
                                                      final CreateCard1 createCard1) {

        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(createCard1)
                .when()
                .post(cardsConfiguration.getBasePath() + CREATE_VIRTUAL_DEBIT_CARD)
                .then().log().all().statusCode(201).extract().body().as(CreateCard1Response.class);
    }

    public WritePhysicalCardResponse1 issuePhysicalCard(final AlphaTestUser alphaTestUser, final String cardId, final WritePhysicalCard1 physicalCard1) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(physicalCard1)
                .when()
                .post(cardsConfiguration.getBasePath() + CREATE_VIRTUAL_DEBIT_CARD + "/" + cardId + "/physicalcards")
                .then().log().all().statusCode(201).extract().body().as(WritePhysicalCardResponse1.class);
    }

    public void validateDebitCardPin(final AlphaTestUser alphaTestUser, final String cardId, final CardPinValidation1 cardPinValidation1) {

        given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(cardPinValidation1)
                .when()
                .put(cardsConfiguration.getBasePath() + CREATE_VIRTUAL_DEBIT_CARD + "/" + cardId + "/validate")
                .then().log().all().statusCode(200);
    }

    public OBErrorResponse1 createVirtualDebitCardError(final AlphaTestUser alphaTestUser,
                                                        final CreateCard1 createCard1, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(createCard1)
                .when()
                .post(cardsConfiguration.getBasePath() + CREATE_VIRTUAL_DEBIT_CARD)
                .then().log().all().statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }

    public OBErrorResponse1 createVirtualDebitCardError(final AlphaTestUser alphaTestUser,
                                                        final JSONObject jsonObject, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(jsonObject.toString())
                .when()
                .post(cardsConfiguration.getBasePath() + CREATE_VIRTUAL_DEBIT_CARD)
                .then().log().all().statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }

    public void createVirtualDebitCardErrorVoid(final AlphaTestUser alphaTestUser, final CreateCard1 createCard1,
                                                int statusCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(createCard1)
                .when()
                .post(cardsConfiguration.getBasePath() + CREATE_VIRTUAL_DEBIT_CARD)
                .then().log().all().statusCode(statusCode);
    }

    public ReadCardLimits1 fetchCardLimitsForTransactionType(final AlphaTestUser alphaTestUser,
                                                             final String transactionType, final String cardId) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/limits?transactionType=" + transactionType)
                .then().log().all().statusCode(200).assertThat().extract().body().as(ReadCardLimits1.class);
    }

    public OBErrorResponse1 fetchCardLimitsForTransactionTypeError(final AlphaTestUser alphaTestUser,
                                                                   final String transactionType, final String cardId,
                                                                   int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/limits?transactionType=" + transactionType)
                .then().log().all().statusCode(statusCode).extract().body().as(OBErrorResponse1.class);
    }

    public void fetchCardLimitsForTransactionTypeErrorVoid(final AlphaTestUser alphaTestUser,
                                                           final String transactionType, final String cardId,
                                                           int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/limits?transactionType=" + transactionType)
                .then().log().all().statusCode(statusCode);
    }

    public void setDebitCardPin(final AlphaTestUser alphaTestUser, final WriteCardPinRequest1 writeCardPinRequest1,
                                int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(writeCardPinRequest1)
                .put(cardsConfiguration.getBasePath() + SET_CARD_PIN)
                .then().log().all()
                .statusCode(statusCode);

    }

    public OBErrorResponse1 setDebitCardPinError(final AlphaTestUser alphaTestUser,
                                                 final WriteCardPinRequest1 writeCardPinRequest1, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(writeCardPinRequest1)
                .put(cardsConfiguration.getBasePath() + SET_CARD_PIN)
                .then().log().all()
                .statusCode(statusCode).extract().body().as(OBErrorResponse1.class);

    }

    public void activateDebitCard(final AlphaTestUser alphaTestUser, final ActivateCard1 activateCard1,
                                  int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(activateCard1)
                .put(cardsConfiguration.getBasePath() + ACTIVATE_CARDS)
                .then().log().all()
                .statusCode(statusCode);

    }

    public OBErrorResponse1 activateDebitCardError(final AlphaTestUser alphaTestUser,
                                                   final ActivateCard1 activateCard1, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(activateCard1)
                .put(cardsConfiguration.getBasePath() + ACTIVATE_CARDS)
                .then().log().all()
                .statusCode(statusCode).extract().body().as(OBErrorResponse1.class);

    }

    public void activateDebitCardErrorVoid(final AlphaTestUser alphaTestUser, final ActivateCard1 activateCard1,
                                           int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(activateCard1)
                .put(cardsConfiguration.getBasePath() + ACTIVATE_CARDS)
                .then().log().all()
                .statusCode(statusCode);

    }

    public void activateDebitCardProtected(final WriteCardActivation1 activateCard2, final String cardId, final String cif,
                                           int statusCode) {
        given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .pathParams("cardId", cardId)
                .pathParams("cif", cif)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(activateCard2)
                .patch(cardsConfiguration.getBasePath() + ACTIVATE_CARDS_PROTECTED)
                .then().log().all()
                .statusCode(statusCode);

    }


    public void activateDebitCardErrorNoToken(final WriteCardActivation1 activateCard2, final String cardId, final String cif,
                                              int statusCode) {
        given()
                .config(config)
                .log().all()
                .pathParams("cardId", cardId)
                .pathParams("cif", cif)
                .contentType(ContentType.JSON)
                .when()
                .body(activateCard2)
                .then().log().all()
                .statusCode(statusCode);

    }

    public void updateDefaultLinkedAccount(final AlphaTestUser alphaTestUser, final WriteCardAccount1 writeCardAccount1,
                                           String cardId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(writeCardAccount1)
                .pathParams("cardId", cardId)
                .put(cardsConfiguration.getBasePath() + UPDATE_DEFAULT_CARD)
                .then().log().all()
                .statusCode(statusCode);
    }

    public void updateCardLimits(final AlphaTestUser alphaTestUser, final WriteDailyMonthlyLimits1 cardLimits,
                                 int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .contentType(ContentType.JSON)
                .when()
                .body(cardLimits)
                .put(cardsConfiguration.getBasePath() + CARD_LIMITS)
                .then().log().all()
                .statusCode(statusCode);
    }

    public ReadCardParameters1 fetchCardFiltersV2(final AlphaTestUser alphaTestUser, final String cardId, final String type) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("cardId", cardId)
                .param("type", type)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PARAMETERS_V2)
                .then().log().all().statusCode(200).assertThat().extract().body().as(ReadCardParameters1.class);
    }
    public OBErrorResponse1 fetchCardFiltersV2Error(final AlphaTestUser alphaTestUser, final String cardId, final String type, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("cardId", cardId)
                .param("type", type)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PARAMETERS_V2)
                .then().log().all().statusCode(statusCode).assertThat().extract().body().as(OBErrorResponse1.class);
    }

 public ReadCardParameters1 fetchCardFilters(final AlphaTestUser alphaTestUser, final String cardId) {
        return given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .pathParams("cardId", cardId)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PARAMETERS)
                .then().log().all().statusCode(200).assertThat().extract().body().as(ReadCardParameters1.class);
    }

    public OBErrorResponse1 fetchCardFiltersError(final AlphaTestUser alphaTestUser, final String cardId, int statusCode) {
        return given()
                .config(config)
                .log().all()
                .pathParams("cardId", cardId)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PARAMETERS)
                .then().log().all().statusCode(statusCode).assertThat().extract().body().as(OBErrorResponse1.class);
    }

    public void fetchCardFiltersVoid(final AlphaTestUser alphaTestUser, final String cardId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .pathParams("cardId", cardId)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_PARAMETERS)
                .then().log().all().statusCode(statusCode);
    }

    public void updateCardParameters(final AlphaTestUser alphaTestUser,
                                     final UpdateCardParameters1 updateCardParameters1, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(updateCardParameters1)
                .put(cardsConfiguration.getBasePath() + "/internal/v1/cards/parameters")
                .then().log().all().statusCode(statusCode);
    }

    public void blockCardLimitedApi(final AlphaTestUser alphaTestUser, final String cardId, int statusCode) {
        given()
                .config(config)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .put(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/block")
                .then().log().all()
                .statusCode(statusCode);
    }

    public void blockCard(final AlphaTestUser alphaTestUser, final JSONObject jsonObject, final String cardId, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .body(jsonObject.toString())
                .put(cardsConfiguration.getBasePath() + "/internal/v1/cards/" + cardId + "/block")
                .then().log().all()
                .statusCode(statusCode);
    }

    public void blockCardProtected(final WriteCardParameters1 updateCardParameters2, final String cardId, final String cif,
                                   int statusCode) {
        given()
                .config(config)
                .header(X_API_KEY, cardsConfiguration.getApiKey())
                .pathParams("cardId", cardId)
                .pathParams("cif", cif)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(updateCardParameters2)
                .put(cardsConfiguration.getBasePath() + BLOCK_CARDS_PROTECTED)
                .then().log().all()
                .statusCode(statusCode);
    }

    public void blockCardProtectedNoKey(final WriteCardParameters1 updateCardParameters2, final String cardId, final String cif,
                                        int statusCode) {
        given()
                .config(config)
                .pathParams("cardId", cardId)
                .pathParams("cif", cif)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .body(updateCardParameters2)
                .put(cardsConfiguration.getBasePath() + BLOCK_CARDS_PROTECTED)
                .then().log().all()
                .statusCode(statusCode);
    }

    public void createPhysicalCard(final AlphaTestUser alphaTestUser, final WritePhysicalCard1 writePhysicalCard1,
                                   final String cardId, int statusCode) {

        given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(writePhysicalCard1)
                .when()
                .pathParams("cardId", cardId)
                .post(cardsConfiguration.getBasePath() + CREATE_PHYSICAL_CARD)
                .then().log().all().statusCode(statusCode);
    }

    public ValidatableResponse createPhysicalCard2(final AlphaTestUser alphaTestUser, final WritePhysicalCard1 physicalCard1,
                                                   final String cardId, int statusCode) {

        return given()
                .config(config)
                .log().all()
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .body(physicalCard1)
                .when()
                .pathParams("cardId", cardId)
                .post(cardsConfiguration.getBasePath() + CREATE_PHYSICAL_CARD)
                .then().log().all()
                .statusCode(statusCode).assertThat();
    }

    public <T> T getCreditCardDetails(final AlphaTestUser alphaTestUser, Map<String, String> queryParams, final Class <T> classType, HttpStatus status) {
        return (T) given()
                .config(config)
                .header(HEADER_X_FAPI_INTERACTION_ID, RequestUtils.generateCorrelationId())
                .log().all()
                .queryParams(queryParams)
                .header("Authorization", "Bearer " + alphaTestUser.getLoginResponse().getAccessToken())
                .contentType(ContentType.JSON)
                .when()
                .get(cardsConfiguration.getBasePath() + GET_CARDS_DETAILS_V1)
                .then().log().all().statusCode(status.getCode())
                .extract().body().as(classType);

    }
}
