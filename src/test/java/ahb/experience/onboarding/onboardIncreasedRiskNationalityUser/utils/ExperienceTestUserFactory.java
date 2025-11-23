package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils;

import ahb.experience.onboarding.ApplicationType;
import ahb.experience.onboarding.DebitCard.ExperienceCardProducts;
import ahb.experience.onboarding.DebitCard.ExperienceDebitCardDetails;
import ahb.experience.onboarding.DebitCard.ExperienceNameOnCard;
import ahb.experience.onboarding.DebitCard.Scheduler.SchedulerMain;
import ahb.experience.onboarding.IDNowDocs.ExperienceApplicantID;
import ahb.experience.onboarding.auth.api.*;
import ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.*;
import ahb.experience.onboarding.util.ExperienceTestUser;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.auth.config.AuthConfiguration;
import uk.co.deloitte.banking.customer.otp.api.OtpApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
public class ExperienceTestUserFactory {

    @Inject
    AuthConfiguration authConfiguration;
    @Inject
    private OtpApi otpApi;
    @Inject
    ExperienceAuthenticateApi experienceAuthenticateApi;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.saveMarketPlaceDetails saveMarketPlaceDetails;
    @Inject
    bankingUserLogin_Para bankingUserLogin;
    @Inject
    VerifyMobileNumber verifyMobileNumber;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.getIDNOWDetails getIDNOWDetails;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.createApplicantID createApplicantID;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.scanDocument scanDocument;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.saveIDNowUser saveIDNowUser;
    @Inject
    saveResidentialAddress saveResAddress;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.saveEmploymentDetails saveEmploymentDetails;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.saveFATCADetails saveFATCADetails;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.saveTAXDetails saveTAXDetails;
    @Inject
    ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.api.updateCustomerState updateCustomerState;
    @Inject
    CreateBankAccount createBankAccount;
    @Inject
    SaveAccountPreference saveAccountPreference;
    @Inject
    DebitCardDetails debitCardDetails;
    ExperienceDebitCardDetails experienceDebitCardDetails;
    SchedulerMain scheduleInfo;

    public static int getRandom(int max){ return (int) (Math.random()*max); }

    private ExperienceRegisterResponse experienceRegisterResponse;
    public ExperienceLoginResponse experienceLoginResponse;

    private ExperienceProfile experienceProfile;
    private ExperienceTestUser experienceTestUserDetails;
    private ExperienceSignature experienceSignature;

    // Base method to create a new customer record on the Experience platform
    public ExperienceLoginResponse setupExperienceMarketPlaceUser(ExperienceTestUser experienceTestUser, String name, String language, String gender, String dateOfBirth, String email, String nationality, boolean termsAccepted, boolean consentToMarketingCommunication, boolean consentToPrivacyPolicy) {

        log.info("Creating valid experience test user");
        experienceRegisterResponse = experienceAuthenticateApi.registerDevice(experienceTestUser);

        experienceTestUser.setUserId(experienceRegisterResponse.getUserId());

        experienceAuthenticateApi.generateOtp(experienceTestUser, 200, experienceRegisterResponse);
        OtpCO otpCO = null;
                //experienceAuthenticateApi.getOtpFromDevSimulator(experienceTestUser.getUserId());
        ExperienceKeys experienceKeys = experienceAuthenticateApi.getSignatureKeys();
        experienceAuthenticateApi.validateOtp(experienceTestUser, experienceKeys, experienceRegisterResponse, 200, otpCO);
        saveMarketPlaceUserDetails(experienceTestUser,name,language,gender,dateOfBirth,email,nationality, termsAccepted,consentToMarketingCommunication,consentToPrivacyPolicy,200);

        experienceSignature = experienceAuthenticateApi.fetchSignature(experienceTestUser, experienceKeys, 200);

        experienceRegisterResponse = experienceAuthenticateApi.savePasscode(experienceTestUser, experienceSignature, experienceRegisterResponse, 200);
        verifyEmail(experienceTestUser);
        experienceSignature = experienceAuthenticateApi.fetchLoginSignature(experienceTestUser, experienceRegisterResponse.getAccessToken(), experienceKeys, 200);
        return experienceLoginResponse = loginUser(experienceTestUser);
    }

    public ExperienceLoginResponse loginUser(ExperienceTestUser experienceTestUser){
        experienceLoginResponse = experienceAuthenticateApi.Login(experienceTestUser, experienceSignature, experienceRegisterResponse, 200);
        return experienceLoginResponse;
    }

    public ExperienceLoginResponse loginUser(String strDeviceID, String strMobNumber, String strPasscode, String strSignature){
        experienceLoginResponse =experienceAuthenticateApi.Login(strDeviceID, strMobNumber, strPasscode, strSignature,200);
        return experienceLoginResponse;
    }

    public void saveMarketPlaceUserDetails(ExperienceTestUser experienceTestUser,String name,String language,String gender,String dateOfBirth,String email, String nationality,boolean termsAccepted, boolean consentToMarketingCommunication, boolean consentToPrivacyPolicy, int StatusCode){
        saveMarketPlaceDetails.updateUser(experienceRegisterResponse.getAccessToken(),experienceTestUser.getDeviceId(),email,name,language,gender,dateOfBirth,nationality,termsAccepted,consentToMarketingCommunication,consentToPrivacyPolicy,StatusCode);
    }

    public void verifyEmail(ExperienceTestUser experienceTestUser){
        ExperienceEmail experienceEmail = experienceAuthenticateApi.generateEmailToken(experienceTestUser, experienceRegisterResponse);
        experienceAuthenticateApi.verifyEmail(experienceEmail, 200);

    }

    public void scanUserDocument( String strDoctype ,String strFirstName,String strFullName,String strBirthday,String strGender,String strLastName,String strNationality,String strPersonNum,String strDocName, String strCountry,String strDocValidUntil,String strDocNum,String strDateIssued, String strEmail, Map<String, String> queryParams){
        queryParams.put("documentType", strDoctype);

        ExperienceApplicantID expApplicantID= createApplicantID
                .createAppID(experienceLoginResponse.getToken().getAccessToken(), queryParams, 200);
        scanDocument.setApplicantID(expApplicantID.getApplicantId());
        scanDocument.setUserDocumentData(strFirstName,strFullName,strBirthday,strGender,strLastName,strNationality,strPersonNum);
        scanDocument.setUserIdentityDocumentData(strDocName,strCountry,strDocValidUntil,strDocNum,strDateIssued);
        scanDocument.setContactEmail(strEmail);

        scanDocument.scanDoc(experienceLoginResponse.getToken().getAccessToken(), queryParams);
    }

    public void performIDNowFlow(String strFirstName,String strFullName,String strPassBirthday,String strEIDBirthday,String strPassGender,String strEIDGender,String strLastName,String strPassNationality,String strEIDNationality,String strCountry, String strPassValidUntil, String strEIDValidUntil, String strEIDNum,String strdateIssued, String strEmail, ApplicationType applicationType, Map<String, String> queryParams){
        String passportNum = "S209"+String.valueOf(getRandom(10000));
        String eidPersonalNum = "784-1968-00"+String.valueOf(getRandom(100000))+"-1";

        getIDNOWDetails.getIDnowdetails(experienceLoginResponse.getToken().getAccessToken(), queryParams);

        scanUserDocument("PASSPORT",strFirstName,strFullName,strPassBirthday,strPassGender,strLastName,strPassNationality,null,"PASSPORT",strCountry,strPassValidUntil,passportNum,strdateIssued,strEmail, queryParams);
        scanUserDocument("EID", strFirstName,null,strEIDBirthday,strEIDGender,strLastName,strEIDNationality,eidPersonalNum,"RESIDENCE_PERMIT",strCountry,strEIDValidUntil,strEIDNum,null,strEmail, queryParams);
        //scanDocument.scanDoc(experienceLoginResponse.getToken().getAccessToken());

        queryParams.remove("documentType");
        getIDNOWDetails.getIDnowdetails(experienceLoginResponse.getToken().getAccessToken(), queryParams);

    }

    public void saveDetailsOnReviewScreen(String strFirstName,String strLastName,String strFullName,Map<String, String> queryParams, int StatusCode){
        //Save User
        saveIDNowUser.saveIDUser(experienceLoginResponse.getToken().getAccessToken(),strFirstName,strLastName,strFullName,queryParams, StatusCode);
    }

    public void saveResidentialDetails(String strBuildName, String strBuildNum, String strStreet, String strVillaNameNumber, String strEmirate, String strCity, List<String> strAddressLine, Map<String, String> queryParams,int StatusCode){
        //Save Address Details
        saveResAddress.saveResAddressUser(experienceLoginResponse.getToken().getAccessToken(),strBuildName,strBuildNum,strStreet,strVillaNameNumber,strEmirate,strCity,strAddressLine,queryParams, StatusCode);
    }

    public void saveEmploymentDetails(String strEmpStatus,String strCompanyName,String strEmpCode,float fMontlyIncome,String IncomeSrc, String strBusinessCode, String strDesgnLapseCode, String ProfCode, String OtherSrcIncome, Map<String, String> queryParams, int StatucCode){
        //Save employment details
        saveEmploymentDetails.saveEmpDetails(experienceLoginResponse.getToken().getAccessToken(),strEmpStatus,strCompanyName,strEmpCode,fMontlyIncome,IncomeSrc,strBusinessCode,strDesgnLapseCode,ProfCode,OtherSrcIncome, queryParams, StatucCode);
    }

    public void saveFATCADetails(boolean strUSCitizenOrResident,String strBirthCity,String strBirthCountry , Map<String, String> queryParams, int StatusCode){
        //Save FATCA details
        saveFATCADetails.captureFATCADetails(experienceLoginResponse.getToken().getAccessToken(),strUSCitizenOrResident,strBirthCity,strBirthCountry,queryParams, StatusCode);
    }

    public void saveTAXDetails(boolean isTaxID,String strTaxCountry, String strSelectedReason,  Map<String, String> queryParams, int StatusCode){
        //Save TAX details
        saveTAXDetails.captureTaxDetails(experienceLoginResponse.getToken().getAccessToken(),isTaxID,strSelectedReason,strTaxCountry, queryParams, StatusCode);
    }

    public void updateCustomerState(String strCustomerState,int StatusCode){
        //Update CustomerState
        updateCustomerState.updateCustomerState(experienceLoginResponse.getToken().getUserId(),strCustomerState,StatusCode);
    }

    public void createBankUser(Map<String, String> queryParams){
        createBankAccount.createAccount(experienceLoginResponse.getToken().getAccessToken(), queryParams);
    }

    public void captureAccountPreference(ApplicationType strAccType){
        saveAccountPreference.captureAccountType(experienceLoginResponse.getToken().getAccessToken(),strAccType);
    }

    public ExperienceProfile getUserProfile(){
        //ExperienceProfileDetails newUserDetails = getProfile.getUserDetails(experienceLoginResponse.getAccessToken());
        experienceProfile = experienceAuthenticateApi.getProfile(experienceLoginResponse);

        return experienceProfile;
    }

    public ExperienceCardProducts getCardDetails(Map<String, String> queryParams){
        return debitCardDetails.getCardVarieties(experienceLoginResponse.getToken().getAccessToken(),queryParams, 200);

    }

    public ExperienceNameOnCard saveNameOnCard(String name, String strProductCode, Map<String, String> queryParams){
        return debitCardDetails.captureNameOnCard(experienceLoginResponse.getToken().getAccessToken(),name,strProductCode,queryParams, 200);
    }

    public void scheduleDebitCardDelivery(String strFullName,boolean isHomeAddress,boolean isParentCard,String strBuildName, String strBuildNum, String strEmirate, String strCity, Map<String, String> queryParams){
        scheduleInfo = debitCardDetails.getDeliveryCardTimeSlots(experienceLoginResponse.getToken().getAccessToken(),200);
        int intTimeSlotID =scheduleInfo.getData().getTimeslots().get(0).getSlots().get(0).getTimeslotId();
        String strTimeslotStart = scheduleInfo.getData().getTimeslots().get(0).getSlots().get(0).getTimeslotStart();
        String strTimeslotEnd = scheduleInfo.getData().getTimeslots().get(0).getSlots().get(0).getTimeslotEnd();
        String strTimeslotDate = scheduleInfo.getData().getTimeslots().get(0).getDate();

        String cifNumber = "";
        if(isParentCard) cifNumber = experienceLoginResponse.getProfile().getCif();

        debitCardDetails.saveDeliveryCardTimeSlots(experienceLoginResponse.getToken().getAccessToken(),intTimeSlotID,strTimeslotDate,strFullName,isHomeAddress,strTimeslotStart,strTimeslotEnd,isParentCard, cifNumber, strBuildName,  strBuildNum,  strEmirate, strCity, queryParams, 200);
    }

    public void updateDebitCardDeliveryStatus(boolean isParentToken,String strDeliveryStatus,String strStatus,String strDeliveryReason, Map<String, String> queryParams, int StatusCode){
        String strTrackingNumber =debitCardDetails.getDebitCardDeliveryStatus(experienceLoginResponse.getToken().getAccessToken(),isParentToken,queryParams, StatusCode).getTrackingNumber();
        debitCardDetails.saveDebitCardDeliveryConfirmation(experienceLoginResponse.getToken().getAccessToken(),strTrackingNumber,strDeliveryStatus,strDeliveryReason,204);
        debitCardDetails.saveDebitCardDeliveryStatus(experienceLoginResponse.getToken().getAccessToken(),strStatus,strTrackingNumber,scheduleInfo.getData().getTimeslots().get(0).getDate(),StatusCode);
    }

    public ExperienceDebitCardDetails getDebitCardDetails(Map<String, String> queryParams){
        experienceDebitCardDetails=debitCardDetails.fetchDebitCardDetails(experienceLoginResponse.getToken().getAccessToken(),"DEBIT", queryParams, 200);
        return experienceDebitCardDetails;
    }

    public void setUpDebitCardPin(Map<String, String> queryParams){
        String strEncryptCardNo =experienceDebitCardDetails.getData().getDebitCardInfo().getCardNoEncrypt();
        String strCardNo =experienceDebitCardDetails.getData().getDebitCardInfo().getCardNo();
        String strExpiryDate =experienceDebitCardDetails.getData().getDebitCardInfo().getExpiryDate();
        String strCardNoLastFour =strCardNo.substring(12,16);
        String strPinBlock = debitCardDetails.generateDebitPinBlock(experienceLoginResponse.getToken().getAccessToken(),strEncryptCardNo);
        debitCardDetails.setDebitCardPin(experienceLoginResponse.getToken().getAccessToken(),strExpiryDate,strCardNo,strCardNoLastFour,strPinBlock, queryParams,200);
    }

}

