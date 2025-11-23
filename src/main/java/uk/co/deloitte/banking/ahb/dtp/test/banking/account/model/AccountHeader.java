package uk.co.deloitte.banking.ahb.dtp.test.banking.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import lombok.*;

@Data
@ToString
@Builder(toBuilder = true)
@Introspected
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountHeader {
    public Audit audit;
    public String page_start;
    public String page_token;
    public String total_size;
    public String page_size;
    public String status;
}
