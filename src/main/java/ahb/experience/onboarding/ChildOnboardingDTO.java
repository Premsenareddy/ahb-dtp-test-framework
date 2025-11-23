package ahb.experience.onboarding;

import ahb.experience.onboarding.request.child.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChildOnboardingDTO {
    @Builder.Default
    public ChildDetailsReqBody childDetailsReqBody = ChildDetailsReqBody.builder()
            .language("en")
            .name("Narnia")
            .email("moogliinjungle668@test.com")
            .dateOfBirth("2018-03-30")
            .build();
    public ChildOTPReqBody childOTPReqBody;
}
