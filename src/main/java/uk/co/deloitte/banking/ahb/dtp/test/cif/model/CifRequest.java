package uk.co.deloitte.banking.ahb.dtp.test.cif.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CifRequest {

  @JsonProperty("PhoneNumber")
  private String phoneNumber;
  @JsonProperty("EmiratesId")
  private String emiratesId;
  @JsonProperty("DateOfBirth")
  private LocalDate dateOfBirth;
}
