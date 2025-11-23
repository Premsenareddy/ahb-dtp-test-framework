package ahb.experience.onboarding.IDNowDocs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

public class IDDetails {
        @Builder.Default
        private String firstName = "Abhi";
        @Builder.Default
        private String fullName = "Abhi Jain";
        @Builder.Default
        private String passBirthday = "1988-03-30";
        @Builder.Default
        private String eidBirthday = "1988-03-30";
        @Builder.Default
        private String lastName = "Jain";
        @Builder.Default
        private String passGender = "MALE";
        @Builder.Default
        private String eidGender = "MALE";
        @Builder.Default
        private String passNationality = "AE";
        @Builder.Default
        private String eidNationality = "AE";
        private String personalNumber;
        @Builder.Default
        private String country = "AE";
        @Builder.Default
        private String passportNumber = "S209"+ Math.random()*10000;
        @Builder.Default
        private String passportValidUntil = "2028-01-01";
        @Builder.Default
        private String eIDValidUntil = "2022-12-05";
        @Builder.Default
        private String eidNum = "090379793";
        @Builder.Default
        private String dateIssued = "2018-01-23";

}
