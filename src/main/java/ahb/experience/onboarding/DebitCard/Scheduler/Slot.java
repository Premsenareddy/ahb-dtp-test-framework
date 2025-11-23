package ahb.experience.onboarding.DebitCard.Scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Slot {

        @JsonProperty("timeslotId")
        private int timeslotId;
        @JsonProperty("externalTimeslotId")
        private int externalTimeslotId;
        @JsonProperty("timeslotStart")
        private String timeslotStart;
        @JsonProperty("timeslotEnd")
        private String timeslotEnd;
}
