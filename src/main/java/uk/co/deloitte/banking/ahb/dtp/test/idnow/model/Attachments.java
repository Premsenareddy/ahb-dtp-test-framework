package uk.co.deloitte.banking.ahb.dtp.test.idnow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachments {

    private String pdf;
    private String idfrontside;
    private String idbackside;
    private String idholograms;
    private String userface;
    private String videolog;
}
