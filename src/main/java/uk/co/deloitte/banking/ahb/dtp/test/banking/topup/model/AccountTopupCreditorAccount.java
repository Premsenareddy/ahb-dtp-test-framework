package uk.co.deloitte.banking.ahb.dtp.test.banking.topup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountTopupCreditorAccount {

    private String identification;
}
