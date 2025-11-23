package uk.co.deloitte.banking.ahb.dtp.test.aml.api.operation;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Parameter;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistRequestDTO;
import uk.co.deloitte.banking.ahb.dtp.test.aml.api.model.CustomerBlacklistResponseDTO;

import javax.validation.Valid;

import static io.micronaut.http.HttpResponse.status;
import static io.micronaut.http.HttpStatus.NOT_IMPLEMENTED;
import static io.reactivex.Single.just;

public interface CustomerBlacklistInternalOperation {

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    default Single<HttpResponse<CustomerBlacklistResponseDTO>> checkBlacklistedCustomer(
            @Parameter(description = "API Key to obtain access to the api ", required = true) @Header(value = "x-api-key", defaultValue = "") String apiKey,
            @Parameter(description = "Idempotency Key valid for 24 hours. ") @Header(value = "x-idempotency-key", defaultValue = "") String xIdempotencyKey,
            @Parameter(description = "A detached JWS signature of the body of the payload.", required = false) @Header(value = "x-jws-signature", defaultValue = "") String xJwsSignature,
            @Parameter(description = "The time when the PSU last logged in with the TPP.", required = false) @Header(value = "x-fapi-auth-date", defaultValue = "") String xFapiAuthDate,
            @Parameter(description = "The PSU's IP address if the PSU is currently logged in with the TPP.", required = false) @Header(value = "x-fapi-customer-ip-address", defaultValue = "") String xFapiCustomerIpAddress,
            @Parameter(description = "An RFC4122 UID used as a correlation id.", required = false) @Header(value = "x-fapi-interaction-id", defaultValue = "") String xFapiInteractionId,
            @Parameter(description = "Indicates the user-agent that the PSU is using.", required = false) @Header(value = "x-customer-user-agent", defaultValue = "") String xCustomerUserAgent,
            @Body @Valid CustomerBlacklistRequestDTO customer) {
        return just(status(NOT_IMPLEMENTED));
    }
}
