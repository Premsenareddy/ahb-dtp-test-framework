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
public class Fields {

    private Project project;

    private String summary;

    private String description;

    private List<Customfield_10032> customfield_10032;

    private List<FixVersions> fixVersions;

    private Customfield_10040 customfield_10040;

    private List<Components> components;

    private Issuetype issuetype;
}
