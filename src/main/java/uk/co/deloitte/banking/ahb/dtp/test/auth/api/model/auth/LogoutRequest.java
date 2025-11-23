package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogoutRequest {
    static final String JWT_PATTERN = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+=]*$";

    @Schema(description = "Token(s) to add to the blacklist")
    @NotEmpty
    private List<@Pattern(regexp = JWT_PATTERN) String> trustTokens;

}
