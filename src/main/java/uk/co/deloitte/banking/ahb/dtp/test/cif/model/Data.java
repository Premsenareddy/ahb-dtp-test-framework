package uk.co.deloitte.banking.ahb.dtp.test.cif.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Data {
    private LegacyCustomerDetails legacyCustomerDetails;
}
