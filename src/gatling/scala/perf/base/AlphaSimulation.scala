package perf.base

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, PropertyNamingStrategy}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomMobile
import uk.co.deloitte.banking.payments.certificate.signing.AlphaKeyService

import java.util.UUID
import scala.io.Source
import scala.util.Random

trait AlphaSimulation extends Simulation {

  val objectMapper: ObjectMapper = new ObjectMapper();
  objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  val objectMapperPascalCase: ObjectMapper = new ObjectMapper();
  objectMapperPascalCase.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  objectMapperPascalCase.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)


  val alphaTestUsers: AlphaTestPerfUsers = new AlphaTestPerfUsers();

  var alphaKeyService: AlphaKeyService = new AlphaKeyService();


  val random: Random = new Random();

  var availableUsers = List[Map[String, String]]()

  def pickARandomUser() = {
    availableUsers(random.nextInt(availableUsers.size))
  }

  def createUser() = {
    val keyPair = alphaKeyService.generateEcKeyPair
    Map(
      "publicKey" -> alphaKeyService.getPublicKeyAsBase64(keyPair),
      "privateKey" -> alphaKeyService.getPrivateKeyAsBase64(keyPair)
    )
  }


  val calculateSignature = new SignatureCalculator {
    override def sign(request: Request): Unit = {
      val privateKey: String = request.getHeaders.get("x-key")
      val payload: String = request.getHeaders.get("x-payload")

      val signature = alphaKeyService.generateJwsSignatureForPayload(payload, privateKey);

//      print("signature ->>" + signature)
//      print(" -- privateKey ->>" + privateKey)
//      println(" -- payload ->>" + payload)

      request.getHeaders.add("signature", signature)
      request.getHeaders.remove("x-key")
      request.getHeaders.remove("x-payload")
    }
  }


  /**
   * Takes the payload and privateKey from the session
   * Calculates the signature
   * Stores in the session
   * @param session
   * @return
   */
  def generateSignatureForPayload(session: Session): String = {
    val privateKey: String = session("privateKey").as[String]
    //    println("** -- privateKey ->>" + privateKey)
    //    println(" -- ***session[payload] ->>" + session.contains("payload"));

    val payload = session("payload").as[String]
    val signature = alphaKeyService.generateJwsSignatureForPayload(payload, privateKey);
    //    println("** signature ->>" + signature)
    return signature;
  }


  object UuidFeeder {
    val feeder = Iterator.continually(Map("uuid" -> java.util.UUID.randomUUID.toString()))
  }

  object TelephoneFeeder {
    val feeder = Iterator.continually(Map("telephone" -> generateRandomMobile()))
  }


  val randomUserFeeder =
    Iterator.continually(pickARandomUser())

  val createUserFeeder =
    Iterator.continually(createUser())


  val idempotentIdFeeder =
    Iterator.continually(Map("idempotentId" -> UUID.randomUUID().toString))


  val correlationIdFeeder =
    Iterator.continually(Map("correlationId" -> UUID.randomUUID().toString))

  before {
    val source: String = Source.fromFile("./users.json").getLines.mkString

    val alphaTestUsersFromFile: AlphaTestPerfUsers = objectMapper.readValue(source, classOf[AlphaTestPerfUsers]);
    System.out.println("ALPHA USER Size ---> " + alphaTestUsers.getAlphaTestUsers.size());


    //    alphaTestUsersFromFile.alphaTestUsers.forEach(atu => alphaTestUsers.alphaTestUsers(atu));

    alphaTestUsersFromFile.alphaTestUsers.forEach(atu => {
      System.out.println("ALPHA USER ---> " + atu);
      availableUsers = availableUsers :+ Map(
        "userId" -> atu.getUserId,
        "accountNumber" -> atu.getAccountNumber,
        "deviceId" -> atu.getDeviceId,
        "password" -> atu.getUserPassword,
        "publicKey" -> atu.getPublicKeyBase64,
        "privateKey" -> atu.getPrivateKeyBase64,
        "token" -> atu.getJwtToken)
    });

  }


}
