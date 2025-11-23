package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {
    private String email;
    private String token;
}
