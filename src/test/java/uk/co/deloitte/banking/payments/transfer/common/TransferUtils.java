package uk.co.deloitte.banking.payments.transfer.common;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.balance.*;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.config.TemenosConfig;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.banking.account.api.AccountApi;
import uk.co.deloitte.banking.cards.api.CardProtectedApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.deloitte.banking.ahb.dtp.test.util.BDDUtils.*;

@Singleton
public class TransferUtils {

    public static final int LESS_THEN = -1;
    public static final int EQUAL_TO = 0;
    public static final int GREATER_THEN = 1;
    public static final String PAYMENTS_ASSUMPTION_MSG = "Assumption failed before executing Payment tests";

    @Inject
    protected AccountApi accountApi;

    @Inject
    private TemenosConfig temenosConfig;

    @Inject
    protected CardProtectedApi cardProtectedApi;

    public boolean canProceedTestWithBalance(final AlphaTestUser alphaTestUser,
                                             final BigDecimal paymentAmount,
                                             final List<Integer> compare,
                                             final String accountId,
                                             final OBBalanceType1Code balanceType1Code) {
        final BigDecimal balance = new BigDecimal(getBalanceForAccountAndType(alphaTestUser, accountId, balanceType1Code).getAmount().getAmount());
        return CollectionUtils.isNotEmpty(compare) && compare.contains(paymentAmount.compareTo(balance)) ? true : false;
    }

    public void topUpUserAccountWithCardPayment(final AlphaTestUser alphaTestUser, final BigDecimal amount) {
        cardProtectedApi.createCardDeposit(temenosConfig.getCreditorAccountId(),alphaTestUser.getAccountNumber(),
                amount);
    }

    private OBReadBalance1DataBalance getBalanceForAccountAndType(final AlphaTestUser alphaTestUser, String accountNumber, OBBalanceType1Code balanceType) {
        OBReadBalance1 balanceResponse = this.accountApi.getAccountBalances(alphaTestUser,
                accountNumber);
        THEN("Status code 200 is returned");

        OBReadBalance1Data balanceResponseData = balanceResponse.getData();
        assertNotNull(balanceResponseData);

        List<OBReadBalance1DataBalance> balanceList = balanceResponseData.getBalance();
        assertNotNull(balanceList);

        balanceList.forEach(this::checkFieldAssertionsForGetUserBalancesTestSuccess);

        AND("Need to confirm expected balance is returned");
        assertEquals(3, balanceList.size());
        final OBReadBalance1DataBalance requestedBalance = balanceList.stream()
                .filter(balance -> balance.getType() == balanceType)
                .findFirst()
                .get();
        Assertions.assertNotNull(requestedBalance);
        Assertions.assertNotNull(requestedBalance.getAmount());
        return requestedBalance;
    }

    private void checkFieldAssertionsForGetUserBalancesTestSuccess(OBReadBalance1DataBalance balance) {
        assertNotNull(balance);

        assertNotNull(balance.getAccountId());

        assertNotNull(balance.getType());

        OBReadBalance1DataAmount balanceAmount = balance.getAmount();
        assertNotNull(balanceAmount);

        assertNotNull(balanceAmount.getCurrency());

        assertEquals("AED", balanceAmount.getCurrency());
    }
}
