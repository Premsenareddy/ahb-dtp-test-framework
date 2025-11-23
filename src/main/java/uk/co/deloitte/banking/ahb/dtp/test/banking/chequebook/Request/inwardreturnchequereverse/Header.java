package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.inwardreturnchequereverse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Header {
    @JsonProperty("TransactionStatus")
    public String transactionStatus;
    @JsonProperty("Audit")
    public Audit audit;
    @JsonProperty("Id")
    public String id;
    @JsonProperty("Status")
    public String status;

    public Header(Audit audit) {
        this.audit=audit;
    }
}
