package uk.co.deloitte.banking.ahb.dtp.test.customer.fetchEID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mapstruct.Mapper;

@Mapper
@EqualsAndHashCode
@Data

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Result",
        "TransactionNumber",
        "IdentId",
        "IdType",
        "DocumentNumber",
        "IdCountry",
        "DateOfExpiry",
        "IdentificationTime",
        "Type",
        "DocumentSource"
})
public class fetchEIDDetails {
    @JsonProperty("Result")
    public String result;
    @JsonProperty("TransactionNumber")
    public String transactionNumber;
    @JsonProperty("IdentId")
    public String identId;
    @JsonProperty("IdType")
    public String idType;
    @JsonProperty("DocumentNumber")
    public String documentNumber;
    @JsonProperty("IdCountry")
    public String idCountry;
    @JsonProperty("DateOfExpiry")
    public String dateOfExpiry;
    @JsonProperty("IdentificationTime")
    public String identificationTime;
    @JsonProperty("Type")
    public String type;
    @JsonProperty("DocumentSource")
    public String documentSource;
}
