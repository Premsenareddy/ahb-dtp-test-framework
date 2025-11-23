package ahb.experience.save.transactionList;

import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "totalAmount",
            "currency",
            "transactions"
    })

    @NoArgsConstructor
    public class Data {

        public Data(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @JsonProperty("totalAmount")
        private Double totalAmount;
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("transactions")
        private List<Transaction> transactions = null;

        @JsonProperty("totalAmount")
        public Double getTotalAmount() {
            return totalAmount;
        }

        @JsonProperty("totalAmount")
        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        @JsonProperty("currency")
        public String getCurrency() {
            return currency;
        }

        @JsonProperty("currency")
        public void setCurrency(String currency) {
            this.currency = currency;
        }

        @JsonProperty("transactions")
        public List<Transaction> getTransactions() {
            return transactions;
        }

        @JsonProperty("transactions")
        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
        }

    }

