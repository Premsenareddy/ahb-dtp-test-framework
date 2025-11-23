package uk.co.deloitte.banking.ahb.dtp.test.idnow.config;

import javax.inject.Singleton;
import io.micronaut.context.annotation.Value;
import lombok.Data;
import uk.co.deloitte.banking.http.properties.AlphaProperties;

@Singleton
@Data
public class IdNowConfiguration implements AlphaProperties {

  @Value("${idnow-idv-adapter.path}")
  private String basePath;

  @Value("${idnow-idv-adapter.webhook}")
  private String webhookPath;

  @Value("${idnow-idv-adapter.apiKey}")
  private String apiKey;


  //@Value("${idnow-idv-adapter.local}")
  //private String idnowAdapterLocalPath;
}

