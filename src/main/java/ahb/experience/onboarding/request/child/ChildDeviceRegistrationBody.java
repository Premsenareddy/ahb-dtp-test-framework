package ahb.experience.onboarding.request.child;

import lombok.*;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildDeviceRegistrationBody {

    private String deviceId;
    private String userId;
    @Builder.Default
    private String password = "9E23086F1184177766297B4561894CCBAA27747AFB9183FCF8DB00B0987ECFE7A472FDFDCD85E3940BBD3140B127B44AE0175F26DDCA5436188330F8B8F24020";
    private String otp;
    private String publicKey;
}
