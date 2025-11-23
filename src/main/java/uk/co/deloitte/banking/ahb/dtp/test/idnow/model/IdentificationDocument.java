package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentificationDocument {

    private IDNowValue type;
    private IDNowValue country;
    private IDNowValue validuntil;
    private IDNowValue number;
    private IDNowValue issuedby;
    private IDNowValue dateissued;
}
