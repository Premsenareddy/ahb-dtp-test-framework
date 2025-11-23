package uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes;

import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

import javax.inject.Singleton;

@Singleton
@Data
public class TripleDesConfig implements AlphaProperties {
    @Value("${cards.tripledes.decrypt.key}")
    private String decryptKey;

    @Value("${cards.tripledes.encrypt.key}")
    private String encryptKey;
}
