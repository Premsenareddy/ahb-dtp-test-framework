package uk.co.deloitte.banking.ahb.dtp.test.cards.configuration;

import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

import javax.inject.Singleton;

@Singleton
@Data
public class CardsConfiguration implements AlphaProperties {
    @Value("${cards.path}")
    private String basePath;

    @Value("${cards.apiKey}")
    private String apiKey;

    @Value("${cards.created-card}")
    private String createdCard;

}
