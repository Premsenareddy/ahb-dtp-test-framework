package ahb.experience.borrow.creditCards.scenario;

import ahb.experience.borrow.creditCards.api.ApplicaitonServiceBuilderDTO;
import ahb.experience.borrow.creditCards.api.ApplicationService;
import ahb.experience.creditCard.ExpApplicationService.EXPCreateApplication;
import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.ExperienceAddressDetails;
import ahb.experience.onboarding.ExperienceEmploymentDetails;
import ahb.experience.onboarding.ExperienceFATCADetails;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.experienceOnboardUser.api.bankingUserLogin_Para;
import ahb.experience.onboarding.experienceOnboardUser.utils.ExperienceBankAccountUser;
import ahb.experience.onboarding.util.ExperienceTestUser;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.DONE;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.TEST;

@MicronautTest
@Slf4j
@Singleton
public class CreateCCApplication {

    @Inject
    bankingUserLogin_Para bankingUserLogin;

    @Inject
    ExperienceBankAccountUser experienceBankAccountUser;

    @Inject
    ApplicationService applicationService;


    private ExperienceTestUser experienceTestUser;
    private ExperienceLoginResponse login;

    String jwsSignature = "eyJhbGciOiJFUzI1NiJ9.eyJzaWduYXR1cmUiOiJNRVVDSVFEWEpyUUQ1bHd1d3llcVA1Vm54dUd0dW91cUxPQlwvemZzVGpjaHhjXC8zbmZnSWdGYXdFM25iSjdaTkdVWUVOaUhyYnpOQXZxeE5Gc216bTNcL3o4UjlZUEYxST0ifQ.KFhknWoNNKntI8eukr1I64ZnQRy8qaCy2IHYYAuhwE0PSZVkvjd2qCGYwT62WIOYE_jYjK8UTXSYme8qbOOllw";
    String deviceId = "4A7B2099-AE76-45F9-B8B3-371E5LQ731";
    String mobileNumber = "+971508711437";
    String passcode = "d404559f602eab6fd602ac7680dacbfaadd13630335e951f097af3900e9de176b6db28512f2e000b9d04fba5133e8b1c6e8df59db3a8ab9d60be4b97cc9e81db";
    String bearerToken = "";

    public void setupTestUser() {
        if (experienceTestUser == null) {
            experienceTestUser = new ExperienceTestUser();
            BankingUserOnboardingDto bankingUserOnboardingBuilder = BankingUserOnboardingDto.builder()
                    .addressDetails(ExperienceAddressDetails.builder().build())
                    .empDetails(ExperienceEmploymentDetails.builder().build())
                    .fatcDetails(ExperienceFATCADetails.builder().build())
                    .idDetails(IDDetails.builder().build())
                    .taxCountries(TaxCountries.builder().build())
                    .build();

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("applicantType", "ADULT");

            login = experienceBankAccountUser.bankAdultUserOnboard(bankingUserOnboardingBuilder, experienceTestUser, queryParams);
        }
    }

    /**
     * This test is to validate marketplace user is onboarded as bank customer
     */
    @Order(1)
    @Test
    public void createBankUser() {

        TEST("AHBDB-XXX: Test Experience User Onboarding");
        bearerToken = bankingUserLogin.getAccessToken_Common(deviceId, mobileNumber, passcode, jwsSignature);

        EXPCreateApplication response = applicationService.createApplication(bearerToken, ApplicaitonServiceBuilderDTO.request, 200);
        Assertions.assertTrue(StringUtils.isNotBlank(response.applicationReferenceNumber));
        DONE();
    }
}
