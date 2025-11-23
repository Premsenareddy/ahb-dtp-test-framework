package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DestinationRequest {
    private String destination;
    private String type;
}
