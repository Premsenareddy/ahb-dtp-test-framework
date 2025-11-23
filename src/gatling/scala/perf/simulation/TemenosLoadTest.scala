package perf.simulation

import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory
import perf.base.AlphaSimulation
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.{OBWriteDomestic2, OBWriteDomestic2Data, OBWriteDomestic2DataInitiation, OBWriteDomestic2DataInitiationCreditorAccount, OBWriteDomestic2DataInitiationDebtorAccount, OBWriteDomestic2DataInitiationInstructedAmount, OBWriteDomestic2DataInitiationRemittanceInformation}
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomestic2Data.OBWriteDomestic2DataBuilder
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2

import java.math.BigDecimal
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps;

class TemenosLoadTest extends AlphaSimulation {

  val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))

  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost")


  val httpProtocol2: HttpProtocolBuilder = http
    .baseUrl("http://localhost")

  object Payment {

    var userId: String = "${userId}"
    var password: String = "${userId}"
    var deviceId: String = "${deviceId}"

    val pay  = OBWriteDomestic2.builder()
      .data(
        OBWriteDomestic2Data.builder()
          .consentId("${uuid}")
          .initiation(OBWriteDomestic2DataInitiation.builder()
            .localInstrument("UAE.IBAN")
            .instructedAmount(OBWriteDomestic2DataInitiationInstructedAmount.builder()
              .amount(BigDecimal.ONE)
              .currency("AED")
              .build())
            .debtorAccount(OBWriteDomestic2DataInitiationDebtorAccount.builder()
              .schemeName("UAE.AccountNumber")
              .identification("${accountNumber}")
              .build())
            .creditorAccount(OBWriteDomestic2DataInitiationCreditorAccount.builder()
              .schemeName("UAE.AccountNumber")
              .identification("0000125757")
              .build())
            .remittanceInformation(OBWriteDomestic2DataInitiationRemittanceInformation.builder()
              .reference("Perf payment")
            .build())
            .build())

          .build())
      .build();

    val makeInternalPayment: ChainBuilder = exec(http("makeInternalPayment")
      .post(":8180/service/alpha-temenos-banking-adapter.obp-dev/internal/v0/internal-payments")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .header("x-fapi-interaction-id", "${correlationId}")
      .header("x-idempotent-id", "${idempotentId}")
      .body(StringBody(objectMapper.writeValueAsString(pay))).asJson
      .check(bodyString.saveAs("paymentBody"))
    )

  }

  //====================================================================================
  //====================================================================================
  //====================================================================================

  val myScenario: ScenarioBuilder = scenario("Payment")
    .feed(UuidFeeder.feeder)
    .feed(randomUserFeeder)
    .feed(correlationIdFeeder)
    .feed(idempotentIdFeeder)
    .exec(Payment.makeInternalPayment)


  setUp(
    myScenario.inject(
//      constantUsersPerSec(1).during(1.minutes)
        atOnceUsers(3)
    ).protocols(httpProtocol2))

}
