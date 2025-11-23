package ahb.experience.onboarding.DebitCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceCardDeliveryStatus {

        @JsonProperty("status")
        private String status;
        @JsonProperty("address")
        private String address;
        @JsonProperty("timeslot")
        private String timeslot;
        @JsonProperty("bankPhoneNumber")
        private String bankPhoneNumber;
        @JsonProperty("isHomeAddress")
        private boolean isHomeAddress;
        @JsonProperty("trackingNumber")
        private String trackingNumber;
        @JsonProperty("deliveryDate")
        private Date deliveryDate;

}
