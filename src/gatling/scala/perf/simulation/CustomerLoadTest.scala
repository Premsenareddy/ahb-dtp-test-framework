package perf.simulation

import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory
import perf.base.AlphaSimulation
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBReadAccount6
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2
import uk.co.deloitte.banking.customer.api.customer.model.{OBWritePartialCustomer1, OBWritePartialCustomer1Data}

import scala.concurrent.duration._
import scala.language.postfixOps;

class CustomerLoadTest extends AlphaSimulation {

//  val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
//  context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))

//  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost:8180/internal/v2/customers")
  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost")


  val httpProtocol2: HttpProtocolBuilder = http
    .baseUrl("http://localhost")

  object Customer {

    var userId: String = "${userId}"
    var password: String = "${userId}"
    var deviceId: String = "${deviceId}"

    val loginRequestV2: UserLoginRequestV2 = UserLoginRequestV2.builder()
      .userId(userId)
      .password(password)
      .build();


    val getCustomer: ChainBuilder = exec(http("GetCustomer")
      .get(":8180/service/alpha-customer-adapter.obp-dev/internal/v2/customers")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .check(bodyString.saveAs("responseBody"))
    )

    val getCustomerEmployment: ChainBuilder = exec(http("GetCustomerEmployment")
      .get(":8181/service/alpha-customer-adapter.obp-dev/internal/v2/customers/employments")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .check(bodyString.saveAs("responseBody"))
    )

    val getCustomerFatca: ChainBuilder = exec(http("GetCustomerFatca")
      .get(":8182/service/alpha-customer-adapter.obp-dev/internal/v2/customers/fatca")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .check(bodyString.saveAs("responseBody"))
    )


    val patch: OBWritePartialCustomer1 = OBWritePartialCustomer1.builder().data(
      OBWritePartialCustomer1Data.builder().fullName("PerfRun").build()).build();

    val updateCustomerRequest: String = objectMapperPascalCase.writeValueAsString(patch)

    val updateCustomer: ChainBuilder = exec(http("UpdateCustomer")
      .patch(":8183/service/alpha-customer-adapter.obp-dev/internal/v2/customers")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .body(StringBody(updateCustomerRequest)).asJson
      .check(bodyString.saveAs("responseBody"))
    )

  }

  //====================================================================================
  //====================================================================================
  //====================================================================================

  val myScenario: ScenarioBuilder = scenario("Customer")
    .feed(randomUserFeeder)
    .feed(correlationIdFeeder)
    .feed(idempotentIdFeeder)
    .exec(Customer.getCustomer)
    .exec(Customer.getCustomerEmployment)
    .exec(Customer.getCustomerFatca)
    .exec(Customer.updateCustomer)

  setUp(
    myScenario.inject(
      constantUsersPerSec(40).during(1.minutes)
    ).protocols(httpProtocol2)
  )
}
