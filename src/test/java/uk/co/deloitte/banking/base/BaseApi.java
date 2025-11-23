package uk.co.deloitte.banking.base;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import com.github.dzieciou.testing.curl.Options;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.config.BankingConfig;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.config.IdNowConfiguration;

import javax.inject.Inject;

public class BaseApi {
    public static final String X_API_KEY = "X-API-KEY";

    @Inject
    public CustomerConfig customerConfig;

    @Inject
    public IdNowConfiguration idNowConfiguration;

    @Inject
    public TemenosConfig temenosConfig;

    @Inject
    BankingConfig bankingConfig;


    public BaseApi() {
        //Enable for ZAP
//        RestAssured.proxy("localhost",9090);
//        RestAssured.useRelaxedHTTPSValidation();
    }

    public static final RestAssuredConfig configCamelCase =
            CurlRestAssuredConfigFactory.createConfig().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                    (cls, charset) -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper;
                    }
            )).and();

    //https://www.baeldung.com/resteasy-client-tutorial

//    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//    CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
//    cm.setMaxTotal(200); // Increase max total connection to 200
//    cm.setDefaultMaxPerRoute(20); // Increase default max connection per route to 2


    public static final RestAssuredConfig config =
            CurlRestAssuredConfigFactory.createConfig(Options.builder()                     
                        .logStacktrace()
                        .printMultiliner()
                        .build())
                    .httpClient(CurlRestAssuredConfigFactory.createConfig().getHttpClientConfig().dontReuseHttpClientInstance())
                    .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                            (cls, charset) -> {
                                ObjectMapper objectMapper = new ObjectMapper();
                                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
                                objectMapper.registerModule(new JavaTimeModule());
                                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
                                return objectMapper;
                            }
                    ));

//    .connectionConfig(ConnectionConfig.connectionConfig()
//                            .closeIdleConnectionsAfterEachResponseAfter(100, TimeUnit.MILLISECONDS))
    //RestAssured.config().connectionConfig(ConnectionConfig.connectionConfig().closeIdleConnectionsAfterEachResponse
    // ());
}
