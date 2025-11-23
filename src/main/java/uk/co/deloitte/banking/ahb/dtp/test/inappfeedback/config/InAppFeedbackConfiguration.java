package uk.co.deloitte.banking.ahb.dtp.test.inappfeedback.config;

import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

import javax.inject.Singleton;

@Singleton
@Data
public class InAppFeedbackConfiguration implements AlphaProperties {

  @Value("${inapp.path}")
  private String baseurl;

}

