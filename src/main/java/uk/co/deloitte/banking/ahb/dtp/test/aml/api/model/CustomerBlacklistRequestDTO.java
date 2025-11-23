package uk.co.deloitte.banking.ahb.dtp.test.aml.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
public class CustomerBlacklistRequestDTO {
    @JsonProperty("FullName")
    @NotBlank(message = "Full name cannot be empty")
    private String fullName;
    @JsonProperty("Country")
    @NotBlank(message = "Country cannot be empty")
    private String country;
    @JsonProperty("DateOfBirth")
    @NotNull(message = "Date of birth cannot be empty")
    private LocalDate dateOfBirth;
    @JsonProperty("Gender")
    @NotBlank(message = "Gender cannot be empty")
    @Size(min = 1, max = 1, message = "Gender must be either M or F")
    @Pattern(regexp = "[MF]", message = "Invalid Field")
    private String gender;
}
