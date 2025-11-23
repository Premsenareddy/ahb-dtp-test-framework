package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.exchangeRate.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ExchangeRateResponseDo {
    @Schema(name = "Data", description = "Response for Exchange Rate")
    List<ExchangeRateResponse1> data;
}