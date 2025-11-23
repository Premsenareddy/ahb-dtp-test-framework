package ahb.experience.onboarding.experienceOnboardUser.utils;

import ahb.experience.onboarding.ApplicationType;
import ahb.experience.onboarding.BankingUserOnboardingDto;
import ahb.experience.onboarding.auth.api.ExperienceLoginResponse;
import ahb.experience.onboarding.util.ExperienceTestUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Slf4j
@Singleton
public class ExperienceBankAccountUser {

    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;

    public ExperienceLoginResponse bankAdultUserOnboard(BankingUserOnboardingDto bankingUserOnboardingDto, ExperienceTestUser experienceTestUser, Map<String, String> queryParams){

        String strEmail = experienceTestUser.getEmail();
        ExperienceLoginResponse experienceLoginResponse;

        experienceTestUserFactory.getUserProfile();
        experienceTestUserFactory.captureAccountPreference(ApplicationType.BOTH);

        idNowFlowFor(bankingUserOnboardingDto, strEmail, ApplicationType.ADULT, queryParams);
        saveDetailsOnReview(bankingUserOnboardingDto, queryParams);
        saveResidentailDetails(bankingUserOnboardingDto, queryParams);
        saveEmpDetails(bankingUserOnboardingDto, queryParams);
        saveFACTCADetails(bankingUserOnboardingDto, queryParams);
        saveTaxDetails(bankingUserOnboardingDto, queryParams);
        //experienceTestUserFactory.updateCustomerState(strCustomerState,200);

        experienceTestUserFactory.loginUser(experienceTestUser);
        createBankAccount(queryParams);
        experienceLoginResponse = experienceTestUserFactory.loginUser(experienceTestUser);
        return experienceLoginResponse;

    }

    public void createBankAccount(Map<String, String> queryParams) {
        experienceTestUserFactory.createBankUser(queryParams);
    }

    public void saveTaxDetails(BankingUserOnboardingDto bankingUserOnboardingDto,  Map<String, String> queryParams) {
        experienceTestUserFactory.saveTAXDetails(bankingUserOnboardingDto.getTaxCountries().isHaveTaxId(), bankingUserOnboardingDto.getTaxCountries().getTaxCountry(), bankingUserOnboardingDto.getTaxCountries().getSelectedReason(), queryParams, 200);
    }

    public void saveFACTCADetails(BankingUserOnboardingDto bankingUserOnboardingDto,  Map<String, String> queryParams) {
        experienceTestUserFactory.saveFATCADetails(bankingUserOnboardingDto.getFatcDetails().usCitizenOrResident, bankingUserOnboardingDto.getFatcDetails().birthCity, bankingUserOnboardingDto.getFatcDetails().birthCountry, queryParams, 200);
    }

    public void saveEmpDetails(BankingUserOnboardingDto bankingUserOnboardingDto,  Map<String, String> queryParams) {
        experienceTestUserFactory.saveEmploymentDetails(bankingUserOnboardingDto.getEmpDetails().employmentStatus, bankingUserOnboardingDto.getEmpDetails().companyName, bankingUserOnboardingDto.getEmpDetails().employerCode
                , bankingUserOnboardingDto.getEmpDetails().monthlyIncome, bankingUserOnboardingDto.getEmpDetails().incomeSource, bankingUserOnboardingDto.getEmpDetails().businessCode,
                bankingUserOnboardingDto.getEmpDetails().designationLapsCode, bankingUserOnboardingDto.getEmpDetails().professionCode, bankingUserOnboardingDto.getEmpDetails().otherSourceOfIncome,queryParams ,200);
    }

    public void saveResidentailDetails(BankingUserOnboardingDto bankingUserOnboardingDto, Map<String, String> queryParams) {
        experienceTestUserFactory.saveResidentialDetails(bankingUserOnboardingDto.getAddressDetails().buildingName, bankingUserOnboardingDto.getAddressDetails().buildingNumber, bankingUserOnboardingDto.getAddressDetails().street,
                bankingUserOnboardingDto.getAddressDetails().villaNameNumber, bankingUserOnboardingDto.getAddressDetails().emirate, bankingUserOnboardingDto.getAddressDetails().city, bankingUserOnboardingDto.getAddressDetails().addressLine, queryParams,204);
    }

    public void saveDetailsOnReview(BankingUserOnboardingDto bankingUserOnboardingDto, Map<String, String> queryParams) {
        experienceTestUserFactory
                .saveDetailsOnReviewScreen(bankingUserOnboardingDto.getIdDetails().getFirstName(), bankingUserOnboardingDto.getIdDetails().getLastName(), bankingUserOnboardingDto.getIdDetails().getFullName(), queryParams, 204);
    }

    public void idNowFlowFor(BankingUserOnboardingDto bankingUserOnboardingDto, String email, ApplicationType applicationType, Map<String, String> queryParams) {
        experienceTestUserFactory.performIDNowFlow(bankingUserOnboardingDto.getIdDetails().getFirstName(), bankingUserOnboardingDto.getIdDetails().getFullName(), bankingUserOnboardingDto.getIdDetails().getPassBirthday()
                    , bankingUserOnboardingDto.getIdDetails().getEidBirthday(), bankingUserOnboardingDto.getIdDetails().getPassGender(), bankingUserOnboardingDto.getIdDetails().getEidGender(),
                    bankingUserOnboardingDto.getIdDetails().getLastName(), bankingUserOnboardingDto.getIdDetails().getPassNationality(), bankingUserOnboardingDto.getIdDetails().getEidNationality(),
                    bankingUserOnboardingDto.getIdDetails().getCountry(), bankingUserOnboardingDto.getIdDetails().getPassportValidUntil(), bankingUserOnboardingDto.getIdDetails().getEIDValidUntil(), bankingUserOnboardingDto.getIdDetails().getEidNum()
                    , bankingUserOnboardingDto.getIdDetails().getDateIssued(), email, applicationType, queryParams);

    }
}

