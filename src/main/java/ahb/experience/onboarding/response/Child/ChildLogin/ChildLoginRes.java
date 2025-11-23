package ahb.experience.onboarding.response.Child.ChildLogin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChildLoginRes {

    private Token token;
    private Profile profile;
}
