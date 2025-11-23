package uk.co.deloitte.banking.ahb.dtp.test.cards.models.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter(onMethod_ = @Valid)
@Setter(onParam_ = @Valid)
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class CreateCardAccount1 {
    @NotNull
    @Schema(name = "AccountType")
    @JsonProperty("AccountType")
    private String accountType;

    @NotNull
    @Schema(name = "AccountNumber")
    @JsonProperty("AccountNumber")
    private String accountNumber;

    @NotNull
    @Schema(name = "AccountName")
    @JsonProperty("AccountName")
    private String accountName;

    @NotNull
    @Schema(name = "AccountCurrency")
    @JsonProperty("AccountCurrency")
    private String accountCurrency;

    @NotNull
    @Schema(name = "OpenDate")
    @JsonProperty("OpenDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime openDate;

    @NotNull
    @Schema(name = "SeqNumber")
    @JsonProperty("SeqNumber")
    private String seqNumber;
}
