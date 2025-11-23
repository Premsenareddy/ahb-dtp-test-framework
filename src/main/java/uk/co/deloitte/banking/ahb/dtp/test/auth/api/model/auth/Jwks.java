package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class Jwks {


    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;
    private String crv;
    private String x;
    private String y;

}
