package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogoutResponse {

    @Size(min = 4, max = 50)
    @Schema(description = "Informative message detailing if the token(s) were blacklist")
    @Builder.Default
    private String status = "Token(s) blacklisted";

    @Schema(description = "Ids of tokens that are now blacklisted")
    private List<String> tokenIds;

}
