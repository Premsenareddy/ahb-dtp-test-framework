package ahb.experience.onboarding.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@Slf4j
public class ExperienceTestUser {

    private String deviceId;
    private String deviceHash;
    private String userId;
    private String mobileNumber;
    private String email;

    public ExperienceTestUser() {
        init();
    }

    public void init() {

        this.mobileNumber = generateRandomMobileExperience();
        this.deviceHash = UUID.randomUUID().toString();
        this.deviceId = UUID.randomUUID().toString();
        this.email = generateRandomEmail();
    }
}

