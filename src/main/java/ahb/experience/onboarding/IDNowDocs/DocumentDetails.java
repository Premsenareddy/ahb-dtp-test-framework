package ahb.experience.onboarding.IDNowDocs;
import ahb.experience.onboarding.IDNowDocs.*;
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
public class DocumentDetails {

        @JsonProperty("identificationProcess")
        private IdentificationProcess identificationProcess;
        @JsonProperty("customData")
        private CustomData customData;
        @JsonProperty("contactData")
        private ContactData contactData;
        @JsonProperty("userData")
        private UserData userData;
        @JsonProperty("identificationDocument")
        private IdentificationDocument identificationDocument;
        @JsonProperty("attachments")
        private Attachments attachments;

        //public void setAttachments(Attachments attachments) {this.attachments = attachments;}
}
