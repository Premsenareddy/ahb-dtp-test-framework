package uk.co.deloitte.banking.ahb.dtp.test.auth.api.operation;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;

import javax.validation.Valid;

import static io.micronaut.http.HttpResponse.status;
import static io.micronaut.http.HttpStatus.NOT_IMPLEMENTED;
import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.reactivex.Single.just;

import static uk.co.deloitte.banking.ahb.dtp.test.auth.util.ScopeConstants.HEADER_X_DEVICE_ID;
import static uk.co.deloitte.banking.http.common.HttpConstants.*;

public interface UserProtectedOperationV2 {

    /**
     * Login a user
     *
     * @param loginRequest The login request containing either phone number or emmail and password
     * @return a JWT token
     */

    @Post(value = "/users/login", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @SecurityRequirement(name = "OpenID", scopes = DEVICE_SCOPE)
    @Operation(summary = "Login for a user",
            description = "Ticket : <a href=\"https://ahbdigitalbank.atlassian.net/browse/AHBDB-218\">AHBDB-218</a>",
            security = {@SecurityRequirement(name = "ApiKey")})
    default Single<HttpResponse<UserLoginResponseV2>> loginUser(
            @Parameter(description = "API Key to obtain access to the api ")
            @Header(value = HEADER_X_API_KEY, defaultValue = "") String apiKey,
            @Parameter(description = "Idempotency Key valid for 24 hours. ")
            @Header(value = HEADER_X_IDEMPOTENCY_KEY, defaultValue = "") String xIdempotencyKey,
            @Parameter(description = "A detached JWS signature of the body of the payload.", required = false)
            @Header(value = HEADER_X_JWS_SIGNATURE, defaultValue = "") String xJwsSignature,
            @Parameter(description = "Device Id used to submit the request.", required = false)
            @Header(value = HEADER_X_DEVICE_ID, defaultValue = "") String xDeviceId,
            @Parameter(description = "An RFC4122 UID used as a correlation id.", required = false)
            @Header(value = HEADER_X_FAPI_INTERACTION_ID, defaultValue = "") String xFapiInteractionId,
            @Body @Valid UserLoginRequestV2 loginRequest) {
        return just(status(NOT_IMPLEMENTED));
    }


}
