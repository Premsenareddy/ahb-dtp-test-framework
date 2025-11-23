package ahb.experience.onboarding.response.Child;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChildSavePassCodeRes {

    @JsonProperty("accessToken")
    public String childAccessToken;
    public String refreshToken;
    public String scope;
    public String tokenType;
    public Integer expiresIn;
    @JsonProperty("userId")
    public String childUserId;
    public String[] entitlements;
}
