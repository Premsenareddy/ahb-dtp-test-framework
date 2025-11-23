package uk.co.deloitte.banking.journey.scenarios.adult.pdn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class PDNTestDataResultDataHolder {
    private final String accountId;
    private final String cif;
    private final BigDecimal balance;
    private final String accountType;

    public String toCSVFormat() {
        return getData(this.cif) + getData(this.accountId) + getData(this.balance.toString()) + getData(this.accountType) + System.lineSeparator();
    }

    private String getData(String dataItem) {
        if (dataItem == null) {
            return "";
        }

        return dataItem + ",";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDNTestDataResultDataHolder that = (PDNTestDataResultDataHolder) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
