package uk.co.deloitte.banking.ahb.dtp.test.auth.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

import javax.inject.Singleton;

@Singleton
@Data
public class AuthConfiguration implements AlphaProperties {
    @Value("${authentication-adapter.path}")
    private String basePath;

    @Value("${authentication-adapter.apiKey}")
    private String apiKey;

    @Value("${authentication-adapter.apiKey_disable_device}")
    private String disableDeviceApiKey;

    @Value("${experience.path}")
    private String experienceBasePath;

    @Value("${experience.apiKey}")
    private String experienceApiKey;

    @Value("${uaepass.uaepassprefer}")
    private String uaepassPrefer;

    @Value("${uaepass.uaepass-path}")
    private String uaepasspath;

    @Value("${uaepass.uaepass-accesstoken}")
    private String uaepassaccesstoken;

    @Value("${experience.experienceDevPath}")
    private String experienceDevPath;

    @Value("${experience.kong-url}")
    private String kongInternalURL;

    @Value("${experience.apiKey_1}")
    private String experienceApiKey_1;

    @Value("${experience.env}")
    private String experienceEnv;
}