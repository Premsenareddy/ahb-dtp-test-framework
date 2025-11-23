package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IDNowValue {

    private String status;
    private String value;
}
