package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Introspected
public class CreateApplicantResponse {

  @Schema(description = "Unique Id of the client.", example = "standard", name = "ClientId")
  @JsonProperty("ClientId")
  private String clientId;

  @Schema(description = "API Token needed for the request.", example = "s88vndsd8sd8fyvbsdhz8vhg8dhbsd89sdgyv89nwdnsviovn89", name = "Token")
  @JsonProperty("Token")
  private String token;
}