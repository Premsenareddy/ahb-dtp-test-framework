package uk.co.deloitte.banking.ahb.dtp.test.auth.api.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.*;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.operation.UserProtectedOperationV2;

@Client("${authentication-adapter.path}/protected/v2")
public interface UserProtectedClientV2 extends UserProtectedOperationV2 {


    /**
     * Login a user
     *
     * @param apiKey
     * @param xIdempotencyKey
     * @param xJwsSignature
     * @param xDeviceId
     * @param xFapiInteractionId
     * @param loginRequest       The login request containing either phone number or emmail and password
     * @return a JWT token
     */
    @Override
    Single<HttpResponse<UserLoginResponseV2>> loginUser(
            String apiKey,
            String xIdempotencyKey,
            String xJwsSignature,
            String xDeviceId,
            String xFapiInteractionId,
            UserLoginRequestV2 loginRequest);


}
