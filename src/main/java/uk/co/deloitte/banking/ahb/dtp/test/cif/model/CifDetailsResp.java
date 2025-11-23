package uk.co.deloitte.banking.ahb.dtp.test.cif.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CifDetailsResp {
    private uk.co.deloitte.banking.ahb.dtp.test.cif.model.Data data;
}
