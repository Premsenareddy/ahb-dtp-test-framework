package ahb.experience.onboarding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ApplicationType {
    KID("KID"),
    ADULT("ADULT"),
    BOTH("BOTH");

    private final String type;
}
