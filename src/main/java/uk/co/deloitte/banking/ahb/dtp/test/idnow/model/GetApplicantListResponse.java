package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetApplicantListResponse {

    @Schema(description = "Applicant list", name = "IdNowDetails")
    private List<GetApplicantResponse> idNowDetails;

}
