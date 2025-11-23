package ahb.experience.onboarding.childMarketPlace.data;

import ahb.experience.onboarding.*;
import ahb.experience.onboarding.IDNowDocs.IDDetails;
import ahb.experience.onboarding.TaxDetails.TaxCountries;
import ahb.experience.onboarding.request.child.*;
import ahb.experience.onboarding.request.misc.CustomerData;
import ahb.experience.onboarding.request.misc.CustomerServiceReqBody;
import ahb.experience.onboarding.request.misc.PrivilegedReqBody;
import ahb.experience.onboarding.request.misc.Privileges;
import uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.generateRandomEmail;

public class ChildOnboardingBuilders {

    public static final String X_API_KEY = "X-API-KEY";
    public static final String DEVICE_ID = RandomDataGenerator.generateRandomNumeric(4);
    public static final String CHILD_DEVICE_ID = RandomDataGenerator.generateRandomNumeric(4);
    public static final String X_FAPI_INTERACTION_ID = "4A7B2089-FG34-45F9-I90O-401E5C";

    public static final Supplier<ChildDetailsReqBody> childDetailsSupplier = () -> ChildDetailsReqBody.builder()
            .language("en")
            .name("Narnia")
            .email(generateRandomEmail())
            .dateOfBirth("2018-03-30")
            .build();

    public static final Supplier<ChildOnboardingDTO> createChildMarketPlaceData =() -> ChildOnboardingDTO.builder()
            .childDetailsReqBody(childDetailsSupplier.get())
            .childOTPReqBody(ChildOTPReqBody.builder().mobileNumber("+971559906" + DEVICE_ID).build())
            .build();

    public static final BiFunction<Supplier<ChildDetailsReqBody>, Supplier<ChildOTPReqBody>, ChildOnboardingDTO> childOnboardingDataGenerator = (childDetailsReqBody, childOTPReqBody) ->
            ChildOnboardingDTO.builder()
                .childDetailsReqBody(childDetailsReqBody.get())
                .childOTPReqBody(childOTPReqBody.get())
                .build();

    public static final Supplier<BankingUserOnboardingDto> childUserOnboardingData= () -> BankingUserOnboardingDto.builder()
            .addressDetails(ExperienceAddressDetails.builder().build())
            .empDetails(ExperienceEmploymentDetails.builder().build())
            .fatcDetails(ExperienceFATCADetails.builder().build())
            .idDetails(IDDetails.builder()
                    .firstName("Narnia").lastName("Philip").fullName("Narnia Philip")
                    .passBirthday("2018-03-30")
                    .eidBirthday("2018-03-30")
                    .build())
            .taxCountries(TaxCountries.builder().build())
            .privilegedReqBody(PrivilegedReqBody.builder()
                    .privileges(List
                            .of(Privileges.builder()
                                            .identifier("engage.view.marketplace")
                                            .permitted(true)
                                            .build()
                                    ,Privileges.builder()
                                            .identifier("onboarding.view.cardDetails")
                                            .permitted(true)
                                            .build()))
                    .build())
            .customerServiceReqBody(CustomerServiceReqBody.builder()
                    .Data(CustomerData.builder()
                            .CustomerState("ACCOUNT_CREATION_BC_REVIEW_APPROVED")
                            .build())
                    .build())
            .childDetailsReqBody(childDetailsSupplier.get())
            .childOTPReqBody(ChildOTPReqBody.builder().mobileNumber("+971559906" + DEVICE_ID).build())
            .build();
}