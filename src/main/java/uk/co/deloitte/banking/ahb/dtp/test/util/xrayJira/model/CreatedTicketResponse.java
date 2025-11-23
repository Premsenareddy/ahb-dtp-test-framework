package uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreatedTicketResponse {
    private String id;
    private String key;
    private String self;
}
