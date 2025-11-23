package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.scenario;

import ahb.experience.onboarding.*;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils.ExperienceBankAccountUser;
import ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils.ExperienceDebitCardSetup;
import ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils.ExperienceMarketPlaceUser;
import ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils.ExperienceTestUserFactory;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static ahb.experience.onboarding.childMarketPlace.data.ChildOnboardingBuilders.childUserOnboardingData;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;

@MicronautTest
@Slf4j
@Singleton
public class createIncreasedRiskNationality_Test {


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
     * This test is to create bank account user with increased risk nationality
     */
    @ParameterizedTest
    @MethodSource("IrOnboardingPayloadProvider")
    public void increasedRiskNationality_Test(BankingUserOnboardingDto increasedRiskDto) {
        TEST("AHBDB-21576: Test Experience User from increased risk nationality");
        BankingUserOnboardingDto bankingUserOnboardingBuilder = BankingUserOnboardingDto.builder()
                .addressDetails(ExperienceAddressDetails.builder().build())
                .empDetails(ExperienceEmploymentDetails.builder().build())
                .fatcDetails(ExperienceFATCADetails.builder().build())
                .idDetails(IDDetails.builder().build())
                .taxCountries(TaxCountries.builder().build())
                .build();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "ADULT");
        experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser,increasedRiskDto.getMarketPlaceUserDetails());
        experienceBankAccountUser.bankAdultUserOnboard(bankingUserOnboardingBuilder, experienceTestUser, queryParams);

        DONE();
    }

    @ParameterizedTest
    @MethodSource("IrOnboardingPayloadProviderNegative")
    public void increasedRiskNationalityNegative_Test(BankingUserOnboardingDto increasedRiskDto) {
        String customerStateExpected = "ACCOUNT_CREATION_RISK_REJECTION";
        TEST("AHBDB-21576: Test Experience User from increased risk nationality");
        BankingUserOnboardingDto bankingUserOnboardingBuilder = BankingUserOnboardingDto.builder()
                .addressDetails(ExperienceAddressDetails.builder().build())
                .empDetails(ExperienceEmploymentDetails.builder().build())
                .fatcDetails(ExperienceFATCADetails.builder().build())
                .idDetails(IDDetails.builder().build())
                .taxCountries(TaxCountries.builder().build())
                .build();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("applicantType", "ADULT");
       String customerState = experienceMarketPlaceUser.marketPlaceUserSignUp(experienceTestUser,increasedRiskDto.getMarketPlaceUserDetails()).getProfile().getCustomerState();
        Assert.assertTrue("Invalid Message: Customer state not matching",customerState.equalsIgnoreCase(customerStateExpected));

        DONE();
    }
    private static Stream<Arguments> IrOnboardingPayloadProvider() {
        return Stream.of(
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("AF").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("CG").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("GH").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("MZ").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("NG").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("PK").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("SS").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("SD").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("YE").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("RU").build()).build())

        );
    }

    private static Stream<Arguments> IrOnboardingPayloadProviderNegative() {
        return Stream.of(
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("IR").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("CU").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("UA").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("SY").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("KP").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("BY").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("VI").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("US").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("UM").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("AS").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("GU").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("MP").build()).build()),
                Arguments.of(BankingUserOnboardingDto.builder().marketPlaceUserDetails(MarketPlaceUserDetails.builder()
                        .nationality("PR").build()).build())
        );
    }
}
