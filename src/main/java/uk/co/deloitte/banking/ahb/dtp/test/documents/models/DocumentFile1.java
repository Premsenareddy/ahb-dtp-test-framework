package uk.co.deloitte.banking.ahb.dtp.test.documents.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mapstruct.Mapper;

@Mapper
@EqualsAndHashCode
@Data
public class DocumentFile1 {
    @JsonProperty("Id")
    public String id;
    @JsonProperty("ETag")
    public String eTag;
    @JsonProperty("Name")
    public String name;
    @JsonProperty("WebUrl")
    public String webUrl;
    @JsonProperty("CTag")
    public String cTag;
    @JsonProperty("Size")
    public Integer size;
    @JsonProperty("DocumentType")
    private String documentType;
}
