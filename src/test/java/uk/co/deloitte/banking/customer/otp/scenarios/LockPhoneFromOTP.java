package uk.co.deloitte.banking.customer.otp.scenarios;

import org.junit.jupiter.api.Tag;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.deloitte.banking.customer.authentication.api.AuthenticateApi;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;

import javax.inject.Inject;


import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Tag("@BuildCycle1")
@MicronautTest
public class LockPhoneFromOTP {

    @Inject
    private OtpApi otpUserFlow;

    @Inject
    private AuthenticateApi authenticateApiTest;

    static final AlphaTestUser alphaTestUser = new AlphaTestUser();

    public void createUserWithToken() {
        alphaTestUser.init();
        final LoginResponse response = this.authenticateApiTest.authenticateDevice(alphaTestUser);
        Assertions.assertNotNull(response);
        alphaTestUser.setLoginResponse(response);
        alphaTestUser.setUserId(response.getUserId());
        Assertions.assertNotNull(alphaTestUser.getLoginResponse());
        Assertions.assertNotNull(alphaTestUser.getLoginResponse().getAccessToken());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456"})
    public void locking_phone_number_from_further_OTP_requests_and_tries(String invalidOtp) {

        TEST("AHBDB-1388 - Locking phone number from further OTP requests and tries");
        TEST("locking_phone_number_from_further_OTP_requests_and_tries " + invalidOtp);
        GIVEN("I have a valid access token");
        createUserWithToken();

        WHEN("The client has a valid destination number and type");

        NOTE("destination number " + alphaTestUser.getUserTelephone());

        THEN("The client submits the destination payload and receives a 204 response");
        this.otpUserFlow.sendDestinationToOTP(alphaTestUser, 204);

        THEN("The client receives a 400 when they try to validate their invalid code - first time");
        this.otpUserFlow.postOTPCode(alphaTestUser, 400, invalidOtp);

        THEN("The client receives a 400 when they try to validate their invalid code - second time");
        this.otpUserFlow.postOTPCode(alphaTestUser, 400, invalidOtp);

        THEN("The client receives a 400 when they try to validate their invalid code - third time");
        this.otpUserFlow.postOTPCode(alphaTestUser, 400, invalidOtp);

        THEN("The client receives a 423 when they try to validate their invalid code - fourth time");
        this.otpUserFlow.postOTPCode(alphaTestUser, 423, invalidOtp);
        DONE();
    }
}
