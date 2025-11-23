package uk.co.deloitte.banking.ahb.dtp.test.payment.beneficiary;

import lombok.Data;

import java.security.SecureRandom;

import static uk.co.deloitte.banking.ahb.dtp.test.util.RandomDataGenerator.*;

@Data
public class BeneficiaryData {


    private final SecureRandom random = new SecureRandom();

    private String beneficiaryName;
    private String nickName;
    private String iban;
    private String accountNumber;
    private String beneficiaryType;
    private String mobileNumber;
    private String swiftCode;
    private String creditorAgentSchema;
    private String creditorAccountSchema;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String creditorAccountCurrency;

    public BeneficiaryData() {
        init();
    }

    public void init() {
        this.beneficiaryName = generateRandomString(10);
        this.nickName = generateRandomString(10);
        this.mobileNumber = generateRandomMobile();
        this.beneficiaryType = "al_hilal";
        this.iban = "GB33BUKB20201555555555";
        this.swiftCode = generateRandomSwift();
        this.addressLine1 = generateRandomString(20);
        this.addressLine3 = generateRandomString(30);
        this.creditorAccountCurrency = "USD";
    }
}
