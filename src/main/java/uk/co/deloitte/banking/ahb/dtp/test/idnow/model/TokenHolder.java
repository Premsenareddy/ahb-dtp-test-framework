package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenHolder {

    @Schema(description = "Unique Id of the client.", example = "standard", name = "ApplicantId")
    private String applicantId;

    @Schema(description = "API Token needed for the request.", example = "s88vndsd8sd8fyvbsdhz8vhg8dhbsd89sdgyv89nwdnsviovn89", name = "SdkToken")
    private String sdkToken;

    @Schema(description = "Shortname used to create the customer in idnow", example = "alhilalbanktestauto", name = "ShortName")
    private String shortName;

}
