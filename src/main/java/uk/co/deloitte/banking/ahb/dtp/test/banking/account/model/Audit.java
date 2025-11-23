package uk.co.deloitte.banking.ahb.dtp.test.banking.account.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Audit {
    public String T24_time;
    public String responseParse_time;
    public String requestParse_time;
}
