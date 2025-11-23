package uk.co.deloitte.banking.ahb.dtp.test.banking.temenos;


import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.InternalTransferRequest;
import uk.co.deloitte.banking.ahb.dtp.test.banking.temenos.dto.InternalTransferRequestBody;

import javax.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class TemenosRequestUtils {

    public InternalTransferRequest prepareInternalTransferRequest(final String amount, final String debtorAccountId, final String creditorAccountId) {
        return InternalTransferRequest.builder()
                .body(InternalTransferRequestBody
                        .builder()
                        .creditAccountId(creditorAccountId)
                        .debitAccountId(debtorAccountId)
                        .creditAmount(new BigDecimal(amount))
                        .transactionType("AC")
                        .creditCurrencyId("AED")
                        .debitCurrencyId("AED")
                        .build())
                .build();
    }
}
