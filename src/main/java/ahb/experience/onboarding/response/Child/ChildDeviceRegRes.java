package ahb.experience.onboarding.response.Child;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildDeviceRegRes {

    @JsonProperty("accessToken")
    private String childAccessToken;
    @JsonProperty("refreshToken")
    private String childRefreshToken;
    private String scope;
    private String tokenType;
    private Integer expiresIn;
    @JsonProperty("userId")
    private String childUserId;
    private String[] entitlements;
}
