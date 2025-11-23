package uk.co.deloitte.banking.ahb.dtp.test.auth.util;

import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponse;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.LoginResponseV1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.UserLoginResponseV2;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;

public class TokenUtils {


    public static AlphaTestUser parseLoginResponse(AlphaTestUser atu, UserLoginResponseV2 loginResponse) {

        atu.setJwtToken(loginResponse.getAccessToken());
        atu.setScope(loginResponse.getScope());
        atu.setRefreshToken(loginResponse.getRefreshToken());
        atu.setUserId(loginResponse.getUserId());

        //TODO::REMOVE
        atu.setLoginResponse(
                LoginResponse.builder()
                        .accessToken(loginResponse.getAccessToken())
                        .scope(loginResponse.getScope())
                        .refreshToken(loginResponse.getRefreshToken())
                        .userId(loginResponse.getUserId())
                        .build());

        return atu;
    }

    public static AlphaTestUser parseLoginResponse(AlphaTestUser alphaTestUser, LoginResponseV1 loginResponse) {
        alphaTestUser.setJwtToken(loginResponse.getAccessToken());
        alphaTestUser.setScope(loginResponse.getScope());
        alphaTestUser.setRefreshToken(loginResponse.getRefreshToken());
        alphaTestUser.setUserId(loginResponse.getUserId());

        //TODO::REMOVE
        alphaTestUser.setLoginResponse(
                LoginResponse.builder()
                        .accessToken(loginResponse.getAccessToken())
                        .scope(loginResponse.getScope())
                        .refreshToken(loginResponse.getRefreshToken())
                        .userId(loginResponse.getUserId())
                        .build());
        return  alphaTestUser;
    }
}
