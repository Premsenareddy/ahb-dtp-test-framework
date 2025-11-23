package ahb.experience.onboarding.experienceOnboardUser.scenario;

import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceTestUserFactory;
import ahb.experience.onboarding.util.ExperienceTestUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceMarketPlaceUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;

@Slf4j
@MicronautTest
public class createMarketPlaceUser_Test {

    @Inject
    ExperienceMarketPlaceUser experienceMarketPlaceUser;

    private ExperienceTestUser experienceTestUser;

    @BeforeEach
    public void setupTestUser() {
        if (experienceTestUser == null) {
            experienceTestUser = new ExperienceTestUser();
        }
    }

    @Test
    public void test_setup_experience_user() {
        TEST("AHBDB-XXX: Test Experience User Onboarding");
        //ExperienceMarketPlaceUser experienceMarketPlaceUser = new ExperienceMarketPlaceUser();
        experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        DONE();
    }

}
