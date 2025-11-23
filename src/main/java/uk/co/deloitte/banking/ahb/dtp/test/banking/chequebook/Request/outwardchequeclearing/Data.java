package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Request.outwardchequeclearing;

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
        "ValueDate",
        "CCId",
        "MessageId"
})
@Generated("jsonschema2pojo")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Data {

    @JsonProperty("ChequeStatus")
    private String chequeStatus;
    @JsonProperty("ValueDate")
    private String valueDate;
    @JsonProperty("CCId")
    private String cCId;
    @JsonProperty("MessageId")
    private String messageId;

    @JsonProperty("ChequeStatus")
    public String getChequeStatus() {
        return chequeStatus;
    }

    @JsonProperty("ChequeStatus")
    public void setChequeStatus(String chequeStatus) {
        this.chequeStatus = chequeStatus;
    }

    @JsonProperty("ValueDate")
    public String getValueDate() {
        return valueDate;
    }

    @JsonProperty("ValueDate")
    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    @JsonProperty("CCId")
    public String getCCId() {
        return cCId;
    }

    @JsonProperty("CCId")
    public void setCCId(String cCId) {
        this.cCId = cCId;
    }

    @JsonProperty("MessageId")
    public String getMessageId() {
        return messageId;
    }

    @JsonProperty("MessageId")
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}