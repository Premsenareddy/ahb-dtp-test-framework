package ahb.experience.onboarding.response.Child.ChildLogin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @JsonProperty("accessToken")
    private String childAccessToken;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private Integer expiresIn;
    @JsonProperty("userId")
    private String childUserId;
    private String[] entitlements;
}
