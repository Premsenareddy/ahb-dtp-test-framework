package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class JwksList {

    private List<Jwks> keys;

}
