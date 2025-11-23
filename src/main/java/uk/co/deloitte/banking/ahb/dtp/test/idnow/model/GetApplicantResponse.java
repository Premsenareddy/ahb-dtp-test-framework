package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetApplicantResponse {

  @Schema(description = "Applicant's UUID.", example = "uuid", name = "UserId")
  @JsonProperty("UserId")
  private String userId;

  @Schema(description = "Applicant Id.", example = "uuid", name = "ApplicantId")
  @JsonProperty("ApplicantId")
  private String applicantId;

  @Size(min = 2, max = 50)
  @Schema(description = "User id of the responsible creating the applicant.", example = "uuid", name = "ResponsibleId")
  @JsonProperty("ResponsibleId")
  private String responsibleId;

  @Schema(description = "Result of the idv process", example = "SUCCESS", name = "Result")
  @JsonProperty("Result")
  private ApplicantExtractedDTO result;

  @Schema(description = "Document type of the applicant", example = "PASSPORT", name = "DocumentType")
  @JsonProperty("DocumentType")
  private DocumentType documentType;
}