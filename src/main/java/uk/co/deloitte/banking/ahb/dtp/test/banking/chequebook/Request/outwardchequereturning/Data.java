package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequereturning;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ChequeStatus",
        "ChequeReturnCode",
        "CCId"
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Generated("jsonschema2pojo")
public class Data {

    @JsonProperty("ChequeStatus")
    private String chequeStatus;
    @JsonProperty("ChequeReturnCode")
    private String chequeReturnCode;
    @JsonProperty("CCId")
    private String cCId;

    @JsonProperty("ChequeStatus")
    public String getChequeStatus() {
        return chequeStatus;
    }

    @JsonProperty("ChequeStatus")
    public void setChequeStatus(String chequeStatus) {
        this.chequeStatus = chequeStatus;
    }

    @JsonProperty("ChequeReturnCode")
    public String getChequeReturnCode() {
        return chequeReturnCode;
    }

    @JsonProperty("ChequeReturnCode")
    public void setChequeReturnCode(String chequeReturnCode) {
        this.chequeReturnCode = chequeReturnCode;
    }

    @JsonProperty("CCId")
    public String getCCId() {
        return cCId;
    }

    @JsonProperty("CCId")
    public void setCCId(String cCId) {
        this.cCId = cCId;
    }

}