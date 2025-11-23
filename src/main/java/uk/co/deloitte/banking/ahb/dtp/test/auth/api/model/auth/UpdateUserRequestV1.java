package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
@Builder
public class UpdateUserRequestV1 {

    @JsonProperty("Mail")
    @Schema(name = "Mail")
    private String mail;

    @JsonProperty("Sn")
    @Schema(name = "Sn")
    private String sn;

    @JsonProperty("PhoneNumber")
    @Schema(name = "PhoneNumber")
    private String phoneNumber;

    @JsonProperty("UserPassword")
    @Schema(name = "UserPassword")
    private String userPassword;
}
