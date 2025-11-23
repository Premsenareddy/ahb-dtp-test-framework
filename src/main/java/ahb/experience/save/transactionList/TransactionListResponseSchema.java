package ahb.experience.save.transactionList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NoArgsConstructor;


@JsonPropertyOrder({
        "data"
})

@NoArgsConstructor
public class TransactionListResponseSchema {

    @JsonProperty("data")
    private Data data;

    @JsonProperty("data")
    public Data getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
    }

}