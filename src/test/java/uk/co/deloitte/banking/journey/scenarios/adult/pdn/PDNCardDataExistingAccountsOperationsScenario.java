package uk.co.deloitte.banking.journey.scenarios.adult.pdn;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBBalanceType1Code;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1Data;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.OBReadBalance1DataBalance;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.payment.OBWriteDomesticResponse5Data;
import uk.co.deloitte.banking.journey.scenarios.adult.AdultOnBoardingBase;
import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.PDNTestDataResultDataHolder;
import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.transactions.PDNDataTransactions;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;
import static uk.co.deloitte.banking.journey.scenarios.adult.AdultBankingCustomerScenarioThin.ACCOUNT_TEST;
import static uk.co.deloitte.banking.journey.scenarios.adult.AdultBankingCustomerScenarioThin.SMOKE_TEST;

@Slf4j
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PDNCardDataExistingAccountsOperationsScenario extends AdultOnBoardingBase {

    private static final String AFTER_TRANSACTIONS_FILE_NAME = "after_transactions.csv";
    private static final Set<PDNTestDataResultDataHolder> RESULT = new HashSet<>();
    private static final String LEGACY_IBAN = "AED1005500420001";

    @Tag(ACCOUNT_TEST)
    @Tag(SMOKE_TEST)
    @Test
    public void executeDataCreation() throws IOException, URISyntaxException {
        TEST("Execute PDN data creation process | execute transactions");
        List<PDNDataTransactions> data = PDNDataTransactions.getData();
        System.out.println(data);
        AND("Data is successfully gathered. number of items -> " + data.size());

        for (int i = 0; i < data.size(); i++) {
            System.out.println("Iteration -> " + i);
            PDNDataTransactions pdnDataTransactions = data.get(i);
            System.out.println("Data -> " + pdnDataTransactions.toString());

            if (pdnDataTransactions.getAmountToCredit() != null) {
                payment(LEGACY_IBAN, pdnDataTransactions.getAccountId(), pdnDataTransactions.getAmountToCredit());
            }

            if (pdnDataTransactions.getAmountToDebit() != null) {
                payment(pdnDataTransactions.getAccountId(), LEGACY_IBAN, pdnDataTransactions.getAmountToDebit());
            }

            BigDecimal balance = getAccountBalance(pdnDataTransactions.getAccountId());
            PDNTestDataResultDataHolder build = PDNTestDataResultDataHolder.builder().accountId(pdnDataTransactions.getAccountId()).balance(balance).build();

            RESULT.remove(build);
            RESULT.add(build);
            dump();
        }

        dump();
        NOTE("Dump");
        FileDumper.dumpToFile(AFTER_TRANSACTIONS_FILE_NAME, RESULT);
    }

    private void dump() {
        for (PDNTestDataResultDataHolder testData : RESULT) {
            System.out.println(testData.toString());
        }
    }

    private void payment(String debtorAccount, String creditorAccount, BigDecimal amount) {
        OBWriteDomesticResponse5 response = paymentProtectedApi.createLegacyDtpPaymentWebhooks(debtorAccount,
                creditorAccount,
                amount);

        OBWriteDomesticResponse5Data data = response.getData();
        assertEquals(data.getStatus(), OBWriteDomesticResponse5Data.StatusEnum.ACCEPTED_CREDIT_SETTLEMENT_COMPLETED);
    }

    private BigDecimal getAccountBalance(String accountId) {
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(accountId, false);

        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        return new BigDecimal(getBalance(balanceResponseData.getBalance(), 0));
    }

    private String getBalance(List<OBReadBalance1DataBalance> balance1Data, int i) {
        if (balance1Data.get(i).getType().equals(OBBalanceType1Code.INTERIM_AVAILABLE)) {
            return balance1Data.get(i).getAmount().getAmount();
        }

        return getBalance(balance1Data, ++i);
    }
}
