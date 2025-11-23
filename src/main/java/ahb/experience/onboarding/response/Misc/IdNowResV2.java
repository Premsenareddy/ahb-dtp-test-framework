package ahb.experience.onboarding.response.Misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IdNowResV2 {

    private String status;
    private List<IdDetails> idDetails;
}
