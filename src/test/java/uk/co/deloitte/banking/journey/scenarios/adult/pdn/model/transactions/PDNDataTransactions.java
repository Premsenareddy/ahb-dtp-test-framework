package uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.transactions;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.PDNData;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Builder
@Getter
@ToString
public class PDNDataTransactions extends PDNData {
    private final String accountId;
    private final BigDecimal amountToCredit;
    private final BigDecimal amountToDebit;

    private static final char CREDIT_INDICATOR = 'C';
    private static final char DEBIT_INDICATOR = 'D';
    private static final String  ACCOUNT_TRANSACTIONS_FILE = "account_transactions.txt";

    public static List<PDNDataTransactions> getData() throws URISyntaxException, IOException {
        int indexOfAccountId = 0;

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        if (classloader == null) {
            Assertions.fail("Class loader is null");
        }

        List<String> lines = Files.readAllLines(Paths.get(classloader.getResource(ACCOUNT_TRANSACTIONS_FILE).toURI()), StandardCharsets.UTF_8);
        assertNotNull(lines);

        return lines.stream().map(line -> {
                    String[] split = line.split(DIVIDER);
                    String accountId = split[indexOfAccountId];

                    if (split.length < 2) {
                        fail("Data is incorrect. Account id is not followed by balance to credit or debit. Line number -> " + lines.indexOf(line));
                    }

                    List<PDNDataTransactions> result = new ArrayList<>();
                    if (StringUtils.isNotBlank(accountId)) {

                        for (int i = 1; i < split.length; i++) {
                            String amount = split[i];
                            PDNDataTransactionsBuilder builder = builder().accountId(accountId);
                            Pair<Boolean, BigDecimal> creditDebitAmount = mapCreditDebitBalance(amount);

                            if (isCredit(creditDebitAmount)) {
                                result.add(builder.amountToCredit(creditDebitAmount.getRight()).build());
                            } else {
                                result.add(builder.amountToDebit(creditDebitAmount.getRight()).build());
                            }
                        }
                    }

                    return result;
                }
        ).collect(ArrayList::new, List::addAll, List::addAll);
    }

    private static boolean isCredit(Pair<Boolean, BigDecimal> creditDebitAmount) {
        return creditDebitAmount.getLeft();
    }

    private static Pair<Boolean, BigDecimal> mapCreditDebitBalance(String amount) {
        if (StringUtils.isNotBlank(amount)) {
            if (amount.charAt(0) == CREDIT_INDICATOR) {
                return Pair.of(true, mapBalance(amount.replace(String.valueOf(CREDIT_INDICATOR), "")));
            }

            if (amount.charAt(0) == DEBIT_INDICATOR) {
                return Pair.of(false, mapBalance(amount.replace(String.valueOf(DEBIT_INDICATOR), "")));
            }

            fail("There is no credit/debit indicator (C/D) in the amount value");
        }

        Assertions.fail("Amount cannot be blank or null");
        return null;
    }
}
