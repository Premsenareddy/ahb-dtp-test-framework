package perf.simulation

import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory
import perf.base.AlphaSimulation
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.{DeviceLoginRequest, UpdateUserRequestV1, UserLoginRequestV2}

import scala.language.postfixOps


class AuthenticateLoadTest extends AlphaSimulation {

  val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))

  //  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost:8180/internal/v2/customers")
  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost")


  val httpProtocol2: HttpProtocolBuilder = http
    .baseUrl("http://localhost")

  object Auth {


    var publicKey: String = "${publicKey}"
    var privateKey: String = "${privateKey}"


    val userLoginRequestV2: UserLoginRequestV2 = UserLoginRequestV2.builder()
      .userId("${userId}")
      .password("${userId}")
      .build();


    private val namespace = "obp-local"
    /**
     * Create payload + signature
     * Validates payload
     * Execute update passcode request
     */
    val validateAndUpdatePasscode: ChainBuilder =
      exec(session => session.set("payload", objectMapperPascalCase.writeValueAsString(
        UpdateUserRequestV1.builder
          .sn("REGISTRATION")
          .userPassword(session("userId").as[String])
          .build()
      )))
        .exec(session => session.set("signature", generateSignatureForPayload(session)))
        .exec(http("validatePayload")
          .put(":8180/service/alpha-certificate-service." + namespace + "/protected/v1/certificates/${userId}")
          .header("x-jws-signature", "${signature}")
          .header("x-device-id", "${uuid}")
          .header("x-api-key", "ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
          .body(StringBody("${payload}")).asJson
          .check(bodyString.saveAs("responseBody"))
        ).pause(2)
        .exec { session =>
          println("signature --> " + session("signature").as[String])
          session
        }
        .pause(2)
        .exec(http("updatePasscode")
          .patch(":8180/service/alpha-authentication-adapter." + namespace + "/internal/v2/users")
          .header("x-device-id", "${uuid}")
          .header("x-jws-signature", "${signature}")
          .header("Authorization", "Bearer ${token}")
          .body(StringBody("${payload}")).asJson
        )

    val validateAndLoginUser: ChainBuilder =
      exec(session => session.set("payload", objectMapperPascalCase.writeValueAsString(
        UpdateUserRequestV1.builder
          .sn("REGISTRATION")
          .userPassword(session("userId").as[String])
          .build()
      )))
        .exec(session => session.set("signature", generateSignatureForPayload(session)))
        .exec(http("validatePayload")
          .put(":8180/service/alpha-certificate-service." + namespace + "/protected/v1/certificates/${userId}")
          .header("x-jws-signature", "${signature}")
          .header("x-device-id", "${uuid}")
          .header("x-api-key", "ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
          .body(StringBody("${payload}")).asJson
          .check(bodyString.saveAs("responseBody"))
        ).pause(2)
        .exec { session =>
          println("signature --> " + session("signature").as[String])
          session
        }
        .pause(2)
        .exec(http("updatePasscode")
          .patch(":8180/service/alpha-authentication-adapter." + namespace + "/internal/v2/users")
          .header("x-device-id", "${uuid}")
          .header("x-jws-signature", "${signature}")
          .header("Authorization", "Bearer ${token}")
          .body(StringBody("${payload}")).asJson
        )


    val registerDevice: ChainBuilder =
      exec(http("registerDevice")
        .post(":8180/service/alpha-authentication-adapter." + namespace + "/protected/v2/devices")
        .header("x-api-key", "8d6153d6-8271-4efd-b093-c3196fbcc073")
        .body(StringBody(objectMapperPascalCase.writeValueAsString(
          DeviceLoginRequest.builder()
            .deviceId("${uuid}")
            .deviceHash("${uuid}")
            .build()))).asJson
        .check(
          jsonPath("$.AccessToken").saveAs("token")
        )
        .check(
          jsonPath("$.UserId").saveAs("userId")
        )
      )
        .pause(2)
        .exec(http("addTelephone")
          .patch(":8180/service/alpha-authentication-adapter." + namespace + "/internal/v2/users")
          .header("x-api-key", "8d6153d6-8271-4efd-b093-c3196fbcc073")
          .header("Authorization", "Bearer ${token}")
          .body(StringBody(objectMapperPascalCase.writeValueAsString(
            UpdateUserRequestV1.builder.phoneNumber("${telephone}").build()
          ))).asJson
        )
        .pause(2)
        .exec(http("uploadCert")
          .post(":8180/service/alpha-certificate-service." + namespace + "/internal/v1/certificates")
          .header("Authorization", "Bearer ${token}")
          .header("x-device-id", "${uuid}")
          .body(StringBody(publicKey))
          .check(bodyString.saveAs("responseBody"))
        )
        .pause(2)
        .exec(validateAndUpdatePasscode)


    //
    //    val validatePayload: ChainBuilder = exec(http("validatePayload")
    //      .put(":8181/service/alpha-certificate-service.obp-dev/protected/v1/certificates/" + userId)
    //      .sign(calculateSignature)
    //      .header("x-device-id", "${deviceId}")
    //      .header("x-key", "${privateKey}")
    //      .header("x-payload", userLoginRequestAsString)
    //      .header("x-api-key","ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
    //      .body(StringBody(userLoginRequestAsString)).asJson
    //      .check(bodyString.saveAs("responseBody"))
    //    )
    //
    //
    //    val validatePayload2: ChainBuilder = exec(http("validatePayload")
    //      .put(":8182/service/alpha-certificate-service.obp-dev/protected/v1/certificates/" + userId)
    //      .sign(calculateSignature)
    //      .header("x-device-id", "${deviceId}")
    //      .header("x-key", "${privateKey}")
    //      .header("x-payload", userLoginRequestAsString)
    //      .header("x-api-key","ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
    //      .body(StringBody(userLoginRequestAsString)).asJson
    //      .check(bodyString.saveAs("responseBody"))
    //    )
    //
    //    val validatePayload3: ChainBuilder = exec(http("validatePayload")
    //      .put(":8182/service/alpha-certificate-service.obp-dev/protected/v1/certificates/" + userId)
    //      .sign(calculateSignature)
    //      .header("x-device-id", "${deviceId}")
    //      .header("x-key", "${privateKey}")
    //      .header("x-payload", userLoginRequestAsString)
    //      .header("x-api-key","ce62881e-8b27-4d9e-ad33-0e4f9f723f0e")
    //      .body(StringBody(userLoginRequestAsString)).asJson
    //      .check(bodyString.saveAs("responseBody"))
    //    )

  }

  //====================================================================================
  //====================================================================================
  //====================================================================================

  val myScenario: ScenarioBuilder = scenario("Authentication")
    .feed(UuidFeeder.feeder)
    .feed(createUserFeeder)
    .feed(TelephoneFeeder.feeder)
    .feed(correlationIdFeeder)
    .feed(idempotentIdFeeder)
    .exec(Auth.registerDevice)


  setUp(
    myScenario.inject(
//      constantUsersPerSec(10).during(60.seconds)
      atOnceUsers(1)
    ).protocols(httpProtocol2)
  )
}
