package perf.simulation

import ch.qos.logback.classic.{Level, LoggerContext}
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader, JWSObject, Payload, Requirement}
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory
import perf.base.AlphaSimulation
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2
import uk.co.deloitte.banking.customer.api.customer.model.{OBWritePartialCustomer1, OBWritePartialCustomer1Data}
import uk.co.deloitte.banking.payments.certificate.signing.{AlphaKeyService, CertificateException, SecurityUtils}

import java.math.BigInteger
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey
import java.security.spec.{ECPrivateKeySpec, InvalidKeySpecException}
import scala.concurrent.duration._
import scala.language.postfixOps


class CertificateLoadTest extends AlphaSimulation {

  val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  context.getLogger("io.gatling.http").setLevel(Level.valueOf("INFO"))

//  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost:8180/internal/v2/customers")
  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost")


  val httpProtocol2: HttpProtocolBuilder = http
    .baseUrl("http://localhost")

  object Certificate {


    var userId: String = "${userId}"
    var password: String = "${password}"
    var deviceId: String = "${deviceId}"
    var publicKey: String = "${publicKey}"
    var privateKey: String = "${privateKey}"

    val userLoginRequestV2: UserLoginRequestV2 = UserLoginRequestV2.builder()
      .userId(userId)
      .password(password)
      .build();

    private val userLoginRequestAsString: String = objectMapperPascalCase.writeValueAsString(userLoginRequestV2)


    val uploadCertificate: ChainBuilder = exec(http("uploadCert")
      .post(":8180/service/alpha-certificate-service.obp-dev/internal/v1/certificates")
      .header("Authorization", "Bearer ${token}")
      .header("x-device-id", "${deviceId}")
      .body(StringBody(publicKey))
      .check(bodyString.saveAs("responseBody"))
    )


    val validatePayload: ChainBuilder = exec(http("validatePayload")
      .put(":8181/service/alpha-certificate-service.obp-dev/protected/v1/certificates/" + userId)
      .sign(calculateSignature)
      .header("x-device-id", "${deviceId}")
      .header("x-key", "${privateKey}")
      .header("x-payload", userLoginRequestAsString)
      .header("x-api-key","ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
      .body(StringBody(userLoginRequestAsString)).asJson
      .check(bodyString.saveAs("responseBody"))
    )


    val validatePayload2: ChainBuilder = exec(http("validatePayload")
      .put(":8182/service/alpha-certificate-service.obp-dev/protected/v1/certificates/" + userId)
      .sign(calculateSignature)
      .header("x-device-id", "${deviceId}")
      .header("x-key", "${privateKey}")
      .header("x-payload", userLoginRequestAsString)
      .header("x-api-key","ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
      .body(StringBody(userLoginRequestAsString)).asJson
      .check(bodyString.saveAs("responseBody"))
    )

    val validatePayload3: ChainBuilder = exec(http("validatePayload")
      .put(":8182/service/alpha-certificate-service.obp-dev/protected/v1/certificates/" + userId)
      .sign(calculateSignature)
      .header("x-device-id", "${deviceId}")
      .header("x-key", "${privateKey}")
      .header("x-payload", userLoginRequestAsString)
      .header("x-api-key","ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
      .body(StringBody(userLoginRequestAsString)).asJson
      .check(bodyString.saveAs("responseBody"))
    )

  }

  //====================================================================================
  //====================================================================================
  //====================================================================================

  val myScenario: ScenarioBuilder = scenario("Certificate")
    .feed(randomUserFeeder)
    .feed(correlationIdFeeder)
    .feed(idempotentIdFeeder)
    .exec(Certificate.uploadCertificate)
    .exec(Certificate.validatePayload)
    .exec(Certificate.validatePayload2)
    .exec(Certificate.validatePayload3)

  setUp(
    myScenario.inject(
      constantUsersPerSec(30).during(1.minutes)
    ).protocols(httpProtocol2)
  )
}
