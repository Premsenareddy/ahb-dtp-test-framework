package uk.co.deloitte.banking.ahb.dtp.test.cif;

import javax.inject.Singleton;
import io.micronaut.context.annotation.Value;
import lombok.Data;

@Singleton
@Data
public class CifConfig {

  @Value("${cif-service.path}")
  private String basePath;
}
