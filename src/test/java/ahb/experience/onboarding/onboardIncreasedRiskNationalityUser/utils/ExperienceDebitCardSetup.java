package ahb.experience.onboarding.onboardIncreasedRiskNationalityUser.utils;

import ahb.experience.onboarding.DebitCard.ExperienceDebitCardDetails;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Slf4j
@Singleton
public class ExperienceDebitCardSetup {

    @Inject
    ExperienceTestUserFactory experienceTestUserFactory;


    public ExperienceDebitCardDetails setUpDebitCard(Map<String, String> queryParams, String strDeliveryStatus,String strDeliveryReason){

        String buildNum = "12";
        String buildName = "Some Building name";
        String city = "Dubai";
        String emirate ="Dubai";

        Boolean isParent = true;

        if(queryParams.get("applicantType").equalsIgnoreCase("KID")) {
            isParent = false;
        } else queryParams.clear();

        String productCode = experienceTestUserFactory.getCardDetails(queryParams).getProducts().get(3).getProductCode();
        experienceTestUserFactory.saveNameOnCard("Abhi",productCode, queryParams);
        experienceTestUserFactory.scheduleDebitCardDelivery("Abhi Jain",false,isParent,buildName,buildNum,emirate,city, queryParams);
        queryParams.remove("applicantType");
        experienceTestUserFactory.updateDebitCardDeliveryStatus(isParent,strDeliveryStatus,"DELIVERED",strDeliveryReason, queryParams, 200);
        ExperienceDebitCardDetails experienceDebitCardDetails = experienceTestUserFactory.getDebitCardDetails(queryParams);
        experienceTestUserFactory.setUpDebitCardPin(queryParams);

        return experienceDebitCardDetails;
    }

}
