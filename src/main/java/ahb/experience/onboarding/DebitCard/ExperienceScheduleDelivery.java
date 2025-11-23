package ahb.experience.onboarding.DebitCard;

import ahb.experience.onboarding.ExperienceAddressDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceScheduleDelivery {

        @JsonProperty("address")
        private ExperienceAddressDetails address;
        @JsonProperty("timeslotId")
        private int timeslotId;
        @JsonProperty("scheduledDate")
        private String scheduledDate;
        @Builder.Default
        @JsonProperty("phoneNumber")
        private String phoneNumber = "1234";
        @JsonProperty("customerName")
        private String customerName;
        @Builder.Default
        @JsonProperty("token")
        private String token = "D6y93RUyL/2eOj7IQwrTYQ==";
        @JsonProperty("isHomeAddress")
        private boolean isHomeAddress;
        @Builder.Default
        @JsonProperty("latitude")
        private double latitude = 25.1561085;
        @Builder.Default
        @JsonProperty("longitude")
        private double longitude = 55.4624448;
        @JsonProperty("timeslotStart")
        private String timeslotStart;
        @JsonProperty("timeslotEnd")
        private String timeslotEnd;
        @Builder.Default
        @JsonProperty("type")
        private String type = "DEBIT";
        @JsonProperty("isParentCard")
        private boolean isParentCard;
        @JsonProperty("cifNumber")
        private String cifNumber;
        private String relationshipId;
}
