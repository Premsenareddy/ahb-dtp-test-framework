package ahb.experience.onboarding.response.Parent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParentLoginRes {

    private Token token;
    private Profile profile;
}
