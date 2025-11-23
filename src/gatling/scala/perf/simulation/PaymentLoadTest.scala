package perf.simulation

import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory
import perf.base.AlphaSimulation
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2

import scala.language.postfixOps;

class PaymentLoadTest extends AlphaSimulation {

  val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))

  //  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost:8180/internal/v1/payments")
  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost")


  val httpProtocol2: HttpProtocolBuilder = http
    .baseUrl("http://localhost")

  object Payment {

    var userId: String = "${userId}"
    var password: String = "${userId}"
    var deviceId: String = "${deviceId}"

    val loginRequestV2: UserLoginRequestV2 = UserLoginRequestV2.builder()
      .userId(userId)
      .password(password)
      .build();


    val getBeneficiary: ChainBuilder = exec(http("getBeneficiary")
      .get(":8180/service/alpha-payment-service.obp-dev/internal/v1/beneficiaries")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .header("x-fapi-interaction-id", "${correlationId}")
      .header("x-idempotent-id", "${idempotentId}")
      .check(bodyString.saveAs("responseBody"))
    )

    val paymentConsentBody = "{\n    \"Data\": {\n        \"ReadRefundAccount\": null,\n        \"Initiation\": {\n            \"InstructionIdentification\": null,\n            \"EndToEndIdentification\": null,\n            \"LocalInstrument\": null,\n            \"InstructedAmount\": {\n                \"Amount\": 1,\n                \"Currency\": \"AED\"\n            },\n            \"DebtorAccount\": {\n                \"SchemeName\": \"UAE.AccountNumber\",\n                \"Identification\": \"${accountNumber}\",\n                \"Name\": null,\n                \"SecondaryIdentification\": null\n            },\n            \"CreditorAccount\": {\n                \"SchemeName\": \"UAE.AccountNumber\",\n                \"Identification\": \"0000125757\",\n                \"Name\": null,\n                \"SecondaryIdentification\": null\n            },\n            \"CreditorPostalAddress\": null,\n            \"RemittanceInformation\": {\n                \"Unstructured\": \"unstructured\",\n                \"Reference\": \"Api tester - internal payments\"\n            },\n            \"SupplementaryData\": null\n        },\n        \"Authorisation\": null,\n        \"SCASupportData\": null,\n        \"SupplementaryData\": null\n    },\n    \"Risk\": {\n        \"PaymentContextCode\": null,\n        \"MerchantCategoryCode\": null,\n        \"MerchantCustomerIdentification\": null,\n        \"DeliveryAddress\": null\n    }\n}";
    val paymentBody = "{\n    \"Data\": {\n        \"ConsentId\": \"${consentId}\",\n        \"Initiation\": {\n            \"InstructionIdentification\": null,\n            \"EndToEndIdentification\": null,\n            \"LocalInstrument\": \"UAE.IBAN\",\n            \"InstructedAmount\": {\n                \"Amount\": 1,\n                \"Currency\": \"AED\"\n            },\n            \"DebtorAccount\": {\n                \"SchemeName\": \"UAE.AccountNumber\",\n                \"Identification\": \"${accountNumber}\",\n                \"Name\": null,\n                \"SecondaryIdentification\": null\n            },\n            \"CreditorAccount\": {\n                \"SchemeName\": \"UAE.AccountNumber\",\n                \"Identification\": \"0000125757\",\n                \"Name\": null,\n                \"SecondaryIdentification\": null\n            },\n            \"CreditorPostalAddress\": null,\n            \"RemittanceInformation\": {\n                \"Unstructured\": \"unstructured\",\n                \"Reference\": \"Api tester - internal payments\"\n            },\n            \"SupplementaryData\": null\n        },\n        \"SupplementaryData\": null\n    },\n    \"Risk\": {\n        \"PaymentContextCode\": null,\n        \"MerchantCategoryCode\": null,\n        \"MerchantCustomerIdentification\": null,\n        \"DeliveryAddress\": null\n    }\n}";


    val makeInternalPayment: ChainBuilder = exec(http("makeInternalPayment")
      .post(":8180/service/alpha-temenos-service.obp-dev/internal/v0/internal-payments")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .header("x-fapi-interaction-id", "${correlationId}")
      .header("x-idempotent-id", "${idempotentId}")
      .body(StringBody(paymentBody)).asJson
      .check(bodyString.saveAs("paymentBody"))
    )

    val makeInternalPaymentConsent: ChainBuilder = exec(http("makeInternalPaymentConsent")
      .post(":8180/service/alpha-payment-service.obp-dev/internal/v1/internal-payment-consents")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .header("x-fapi-interaction-id", "${correlationId}")
      .header("x-idempotent-id", "${idempotentId}")
      .body(StringBody(paymentConsentBody)).asJson
      .check(
        jsonPath("$.Data.ConsentId").saveAs("consentId")
      )
    ).exec(makeInternalPayment)

  }

  //====================================================================================
  //====================================================================================
  //====================================================================================

  val myScenario: ScenarioBuilder = scenario("Payment")
    .feed(UuidFeeder.feeder)
    .feed(randomUserFeeder)
    .feed(correlationIdFeeder)
    .feed(idempotentIdFeeder)
    .exec(Payment.makeInternalPaymentConsent)


  setUp(
    myScenario.inject(
      //      constantUsersPerSec(10).during(1.minutes)
      atOnceUsers(5)
    ).protocols(httpProtocol2))

}
