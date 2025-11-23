package uk.co.deloitte.banking.ahb.dtp.test.auth.api.operation;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceLoginRequest;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceRegistrationRequestV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.DeviceRegistrationRequestV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.RefreshRequest;

import javax.validation.Valid;

import static io.micronaut.http.HttpResponse.status;
import static io.micronaut.http.HttpStatus.NOT_IMPLEMENTED;
import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.reactivex.Single.just;
import static uk.co.deloitte.banking.http.common.HttpConstants.HEADER_X_API_KEY;

@Validated
public interface DeviceProtectedOperationV2 {


    /**
     * Register a device on the platform
     *
     * @param registrationRequest The device id and password to register
     * @return Tokens to allow device access
     */
    @Post(consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Register a device", description = "Allows for the registration" +
            "of a device on the platform.  Accepts an user id, a device id and device hash value.  " +
            "This method once registered also performs a login and returns an access token to be used for user " +
            "registration<br/>" +
            "If a device already exists this endpoint will return a Http Conflict error - Ticket : <a href=\"https://ahbdigitalbank.atlassian.net/browse/AHBDB-213\">AHBDB-213</a>")
    default Single<HttpResponse<LoginResponseV2>> registerDevice(@Body @Valid DeviceRegistrationRequestV2 registrationRequest,
                                                                 @Parameter(description = "API Key to obtain access to " +
                                                                         "the api ")
                                                                 @Header(value = HEADER_X_API_KEY, defaultValue = "") String apiKey) {
        return just(status(NOT_IMPLEMENTED));
    }

    /**
     * Login with a device id and password
     *
     * @param loginRequest The device id and password to register with
     * @return Tokens that allow device access
     */
    @Post(value = "/login", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Login for a device", description = "Allows for the authentication" +
            "of a device on the platform.  accepts a device id and device hash value returns an access token to be " +
            "used for user registration<br/>" +
            "If a device does not exist it will return a 401 error.")
    default Single<HttpResponse<LoginResponseV2>> loginDevice(@Body @Valid DeviceLoginRequest loginRequest) {
        return just(status(NOT_IMPLEMENTED));
    }

    /**
     * Refresh an access token for a device
     *
     * @param refreshRequest The existing valid refresh token for the device
     * @return An updated access token
     */
    @Post(value = "/refresh", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Refresh an access token for a device", description = "Allows for the refresh" +
            "of a device access token.  accepts a valid refresh token for a given device and returns an updated " +
            "access token")
    default Single<MutableHttpResponse<LoginResponseV2>> refreshDeviceToken(@Body @Valid RefreshRequest refreshRequest) {
        return just(status(NOT_IMPLEMENTED));
    }


}
