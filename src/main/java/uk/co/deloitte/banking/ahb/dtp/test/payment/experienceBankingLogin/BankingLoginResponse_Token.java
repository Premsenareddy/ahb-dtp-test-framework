package uk.co.deloitte.banking.ahb.dtp.test.payment.experienceBankingLogin;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
@Introspected
public class BankingLoginResponse_Token {

    @JsonProperty("AccessToken")
    public String accessToken;

    @JsonProperty("RefreshToken")
    public String refreshToken;

    @JsonProperty("Scope")
    public String scope;

    @JsonProperty("tokenType")
    public String tokenType;

    @JsonProperty("expiresIn")
    public int expiresIn;

    @JsonProperty("userId")
    public String userId;

    @JsonProperty("entitlements")
    public List<Object> entitlements;
}
