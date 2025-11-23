package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
@Builder
@Slf4j
//TODO:: Rename to UserV1
public class UserDto {


    @JsonProperty("Mail")
    @Schema(name = "Mail")
    private String mail;

    @JsonProperty("Sn")
    @Schema(name = "Sn")
    private String sn;

    @JsonProperty("PhoneNumber")
    @Schema(name = "PhoneNumber")
    private String phoneNumber;


}
