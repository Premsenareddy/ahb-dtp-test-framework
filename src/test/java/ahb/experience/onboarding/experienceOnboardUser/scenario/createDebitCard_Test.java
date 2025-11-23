package ahb.experience.onboarding.experienceOnboardUser.scenario;

import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.ExperienceAddressDetails;
import ahb.experience.onboarding.ExperienceEmploymentDetails;
import ahb.experience.onboarding.ExperienceFATCADetails;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceBankAccountUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceDebitCardSetup;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceMarketPlaceUser;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceTestUserFactory;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;

@MicronautTest
@Slf4j
@Singleton
public class createDebitCard_Test {

    @Inject
    ExperienceDebitCardSetup experienceDebitCardSetup;
    @Inject
    ExperienceBankAccountUser experienceBankAccountUser;

    private ExperienceTestUser experienceTestUser;

    @Inject
    ExperienceMarketPlaceUser experienceMarketPlaceUser;

    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;

    @BeforeEach
    public void setupTestUser() {
        if (experienceTestUser == null) {
            experienceTestUser = new ExperienceTestUser();
        }
    }

    /**
     * This test is to create bank account user with debit card delivered
     */
    @Test
    public void debitCard_Test() {
        TEST("AHBDB-XXX: Test Experience User Onboarding with Debit card");
        BankingUserOnboardingDto bankingUserOnboardingBuilder = BankingUserOnboardingDto.builder()
                .addressDetails(ExperienceAddressDetails.builder().build())
                .empDetails(ExperienceEmploymentDetails.builder().build())
                .fatcDetails(ExperienceFATCADetails.builder().build())
                .idDetails(IDDetails.builder().build())
                .taxCountries(TaxCountries.builder().build())
                .build();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "ADULT");
        experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser);
        experienceBankAccountUser.bankAdultUserOnboard(bankingUserOnboardingBuilder, experienceTestUser, queryParams);

        experienceDebitCardSetup.setUpDebitCard(queryParams,"Success","pass");


        DONE();
    }
}
