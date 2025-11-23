package uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Update {
    private List<Issuelinks> issuelinks;

}
