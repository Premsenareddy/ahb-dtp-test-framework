package ahb.experience.onboarding.IDNowDocs;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

import java.util.List;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachments {

        @JsonProperty("pdf")
        private String pdf;
        @JsonProperty("xml")
        private String xml;
        @JsonProperty("idBackSide")
        private Object idBackSide;
        @JsonProperty("livenessscreenshot1")
        private Object livenessscreenshot1;
        @JsonProperty("idFrontSide")
        private String idFrontSide;
        @JsonProperty("livenessscreenshot2")
        private Object livenessscreenshot2;
        @JsonProperty("userFace")
        private String userFace;
}
