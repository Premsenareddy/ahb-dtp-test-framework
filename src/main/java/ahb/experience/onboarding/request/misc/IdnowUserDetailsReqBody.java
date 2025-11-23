package ahb.experience.onboarding.request.misc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdnowUserDetailsReqBody {

    @Builder.Default
    private String firstName = "Narnia";
    @Builder.Default
    private String lastName = "Philip";
    @Builder.Default
    private String fullName = "Narnia Philip";
}
