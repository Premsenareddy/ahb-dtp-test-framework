package ahb.experience.onboarding.request.child;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildDetailsReqBody {

    private String name;
    private String dateOfBirth;
    @Builder.Default
    private String language = "en";
    private String email;
    @Builder.Default
    private Boolean termsAccepted = true;
    @Builder.Default
    private String gender = "MALE";
    @Builder.Default
    private String tempPasscode = "9E23086F1184177766297B4561894CCBAA27747AFB9183FCF8DB00B0987ECFE7A472FDFDCD85E3940BBD3140B127B44AE0175F26DDCA5436188330F8B8F24020";
}
