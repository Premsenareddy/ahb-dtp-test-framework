package ahb.experience.onboarding.IDNowDocs;
import ahb.experience.onboarding.StatusValue_Object;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData {

        @JsonProperty("birthday")
        private StatusValue_Object birthday;
        @JsonProperty("firstName")
        private StatusValue_Object firstName;
        @JsonProperty("address")
        private StatusValue_Object address;
        @JsonProperty("personalNumber")
        private Object personalNumber;
        @JsonProperty("nationality")
        private StatusValue_Object nationality;
        @JsonProperty("gender")
        private StatusValue_Object gender;
        @JsonProperty("lastName")
        private StatusValue_Object lastName;
        @JsonProperty("fullName")
        private StatusValue_Object fullName;
}
