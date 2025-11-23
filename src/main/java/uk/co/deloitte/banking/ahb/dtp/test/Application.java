package uk.co.deloitte.banking.ahb.dtp.test;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Alpha ahb-dtp-test framework API",
                version = "1.0",
                description = ""
        ), servers = {
        @Server(description = "development", url = "https://api.dev.alpha-platform.co.uk/alpha/v1.0"),
        @Server(description = "test", url = "https://api.uat.alpha-platform.co.uk/alpha/v1.0"),
        @Server(description = "local", url = "http://localhost:8180")}
)
@SecurityScheme(name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "jwt")

@SecurityScheme(name = "ApiKey",
        type = SecuritySchemeType.APIKEY,
        description = "d8d4a69e-beb6-4878-be50-ee3455fc09f9",
        paramName = "apiKey",
        in = SecuritySchemeIn.HEADER)
//TODO: Change ProtectedAPIKEY  to final key used in Vault
@SecurityScheme(name = "ProtectedApiKey",
        type = SecuritySchemeType.APIKEY,
        description = "c631a51b-9227-48b7-ac76-f60649df59fd",
        paramName = "X-API-KEY",
        in = SecuritySchemeIn.HEADER)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class);

    }
}
