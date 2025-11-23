package uk.co.deloitte.banking.base;

import ahb.experience.onboarding.DocumentGeneration.DocumentList;
import ahb.experience.onboarding.ExperienceErrValidations;
import ahb.experience.onboarding.response.Child.*;
import ahb.experience.onboarding.response.Child.ChildLogin.ChildLoginRes;
import ahb.experience.onboarding.response.Misc.IdNowRes;
import ahb.experience.onboarding.response.Misc.IdNowResV2;
import ahb.experience.onboarding.response.Parent.ParentLoginRes;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import com.github.dzieciou.testing.curl.Options;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.customer.config.CustomerConfig;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.idnow.config.IdNowConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;

import javax.inject.Inject;

public abstract class BaseStep<T extends BaseStep<T>> {
    public static final String X_API_KEY = "X-API-KEY";
    public static final String DEVICE_ID = RandomDataGenerator.generateRandomNumeric(4);
    public static final String CHILD_DEVICE_ID = RandomDataGenerator.generateRandomNumeric(4);
    public static final String X_FAPI_INTERACTION_ID = "4A7B2089-FG34-45F9-I90O-401E5C";

    @Inject
    public AuthConfiguration authConfiguration;

    @Inject
    public DevSimConfiguration devSimConfiguration;

    @Inject
    public CustomerConfig customerConfig;

    @Inject
    public IdNowConfiguration idNowConfiguration;

    public static Child child;
    public ChildOtpRes childOtpRes;
    public ChildKeysRes childKeysRes;
    public ChildDeviceRegRes childDeviceRegRes;
    public ChildSignatureRes childSignatureRes;
    public ChildSavePassCodeRes childSavePassCodeRes;
    public ChildLoginRes childLoginRes;
    public ParentLoginRes parentLoginRes;
    public ExperienceErrValidations experienceErrValidations;
    public DocumentList documentList;

    public static final RestAssuredConfig config =
            CurlRestAssuredConfigFactory.createConfig(Options.builder()
                    .logStacktrace()
                    .printMultiliner()
                    .build())
                    .httpClient(CurlRestAssuredConfigFactory.createConfig().getHttpClientConfig().dontReuseHttpClientInstance())
                    .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                            (cls, charset) -> {
                                ObjectMapper objectMapper = new ObjectMapper();
                                objectMapper.registerModule(new JavaTimeModule());
                                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
                                return objectMapper;
                            }
                    ));

    protected abstract T getThis();
}
