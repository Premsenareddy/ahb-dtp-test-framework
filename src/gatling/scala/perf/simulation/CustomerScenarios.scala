package perf.simulation


import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.http

object CustomerScenarios {

  def retrieveCustomer(namespace: String, version:String): ChainBuilder =
    exec(http("GetCustomer" + version)
    .get(":8180/service/alpha-customer-adapter." + namespace + "/internal/" + version + "/customers")
    .header("Authorization", "Bearer ${token}")
  )

  def retrieveCustomerEmployment(namespace: String, version:String): ChainBuilder =
    exec(http("GetCustomerEmployment" + version)
    .get(":8181/service/alpha-customer-adapter." + namespace + "/internal/" + version + "/customers/employments")
    .header("Authorization", "Bearer ${token}")
  )

  def retrieveCustomerFatca(namespace: String, version:String): ChainBuilder =
    exec(http("GetCustomerFatca")
    .get(":8182/service/alpha-customer-adapter." + namespace + "/internal/" + version + "/customers/fatca")
    .header("Authorization", "Bearer ${token}")
  )

  def createCustomer(namespace: String, version:String): ChainBuilder =
    exec(http("createCustomer" + version)
      .post(":8180/service/alpha-customer-adapter." + namespace + "/internal/" + version + "/customers")
      .header("Authorization", "Bearer ${token}")
      .body(StringBody("{\"Data\":{\"DateOfBirth\":[2001,4,8],\"MobileNumber\":\"${telephone}\"," +
        "\"PreferredName\":\"Testuzdov\",\"FirstName\":null,\"LastName\":null,\"FullName\":null,\"Gender\":null," +
        "\"Nationality\":null,\"CountryOfBirth\":null,\"CityOfBirth\":null,\"Language\":\"en\"," +
        "\"Email\":\"${uuid}@ahb.com\",\"EmailState\":\"NOT_VERIFIED\"," +
        "\"TermsVersion\":[2021,4,8],\"TermsAccepted\":true,\"Address\":{\"Department\":null,\"SubDepartment\":null," +
        "\"BuildingNumber\":\"101\",\"StreetName\":null,\"AddressLine\":[\"ufoqfesvib\",\"harqj\"],\"TownName\":null," +
        "\"CountrySubDivision\":null,\"Country\":\"AR\",\"PostalCode\":null},\"CustomerState\":null}}")).asJson
    )
}
