package ahb.experience.onboarding.request.child;

import lombok.*;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildOTPReqBody {

    private String mobileNumber;

    @Builder.Default
    private String type = "REGISTRATION_TEXT";
}
