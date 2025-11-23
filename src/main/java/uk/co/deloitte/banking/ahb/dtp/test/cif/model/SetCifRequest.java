package uk.co.deloitte.banking.ahb.dtp.test.cif.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetCifRequest {

  @Schema(name= "EmiratesId", example = "123123412345671")
  @JsonProperty("EmiratesId")
  private String emiratesId;
}