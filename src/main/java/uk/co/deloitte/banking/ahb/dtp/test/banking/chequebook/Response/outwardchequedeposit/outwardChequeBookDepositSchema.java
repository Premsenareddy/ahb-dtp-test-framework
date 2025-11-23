package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Response.outwardchequedeposit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class outwardChequeBookDepositSchema {

    public class Audit{
        @JsonProperty("T24_time")
        public int t24_time;
        @JsonProperty("ResponseParse_time")
        public int responseParse_time;
        @JsonProperty("RequestParse_time")
        public int requestParse_time;
        @JsonProperty("VersionNumber")
        public String versionNumber;
    }

    public class Header{
        @JsonProperty("TransactionStatus")
        public String transactionStatus;
        @JsonProperty("Audit")
        public Audit audit;
        @JsonProperty("Id")
        public String id;
        @JsonProperty("Status")
        public String status;
        @JsonProperty("UniqueIdentifier")
        public String uniqueIdentifier;
    }

    public class Body{
        @JsonProperty("DebitValueDate")
        public String debitValueDate;
        @JsonProperty("StockNumber")
        public String stockNumber;
        @JsonProperty("DebitCurrency")
        public String debitCurrency;
        @JsonProperty("ChequeIssueBank")
        public String chequeIssueBank;
        @JsonProperty("CreditValueDate")
        public String creditValueDate;
        @JsonProperty("CheqDepBr")
        public String cheqDepBr;
        @JsonProperty("ChequeNumber")
        public String chequeNumber;
        @JsonProperty("CheqDate")
        public String cheqDate;
        @JsonProperty("PayeeName")
        public String payeeName;
        @JsonProperty("ChequeType")
        public String chequeType;
        @JsonProperty("CreditAcctNo")
        public String creditAcctNo;
        @JsonProperty("BcSortCode")
        public String bcSortCode;
        @JsonProperty("CheqAcNo")
        public String cheqAcNo;
        @JsonProperty("EndToEndReference")
        public String endToEndReference;
        @JsonProperty("CreditCurrency")
        public String creditCurrency;
        @JsonProperty("CreditAmount")
        public String creditAmount;
        @JsonProperty("TransactionType")
        public String transactionType;
        @JsonProperty("DebitAcctNo")
        public String debitAcctNo;
    }

    public class Data{
        @JsonProperty("Header")
        public Header header;
        @JsonProperty("Body")
        public Body body;
    }

    public class Root{
        @JsonProperty("Data")
        public Data data;
    }
}
