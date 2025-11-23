package ahb.experience.onboarding.childMarketPlace.scenarios;

import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.childMarketPlace.utils.ChildOnboardingSteps;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.util.stream.Stream;

import static ahb.experience.onboarding.childMarketPlace.data.ChildOnboardingBuilders.childUserOnboardingData;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Slf4j
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChildMarketPlaceTest {

    @Inject
    ChildOnboardingSteps childOnboardingSteps;

    private ExperienceTestUser experienceTestUser;

    ExperienceLoginResponse experienceLoginResponse;

    @BeforeEach
    public void setupTestUser() {
        if (experienceTestUser == null) {
            experienceTestUser = new ExperienceTestUser();
        }

        experienceLoginResponse = childOnboardingSteps
                .onboardAdultBankUser(experienceTestUser);
    }

    @ParameterizedTest
    @MethodSource("childOnboardingPayloadProvider")
    public void verify_child_registration_marketplace(final BankingUserOnboardingDto bankingUserOnboardingDto) {

        TEST("Validate successful Child Onboarding");
        GIVEN("I have a valid test Adult Bank User");
        WHEN(" I onboard a child bank user");

        childOnboardingSteps
                .childMarketCreateAndLogin(experienceLoginResponse.getToken().accessToken, bankingUserOnboardingDto);

        childOnboardingSteps
                .childBankOnboarding(experienceLoginResponse.getToken().accessToken, bankingUserOnboardingDto);

        childOnboardingSteps
                .childDebitCardSetup();
        THEN("Child is successfully onboarded as Child Bank User");
        DONE();
    }

    private static Stream<Arguments> childOnboardingPayloadProvider() {
        return Stream.of(
                Arguments.of(childUserOnboardingData.get())
        );
    }
}
