package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Response.inwardReturnChequeReverse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class inwardReturnChequeReverseSchema {

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
    }

    public class Body{
        @JsonProperty("DebitValueDate")
        public String debitValueDate;
        @JsonProperty("DebitCurrency")
        public String debitCurrency;
        @JsonProperty("CreditReference")
        public String creditReference;
        @JsonProperty("EndToEndReference")
        public String endToEndReference;
        @JsonProperty("DebitReference")
        public String debitReference;
        @JsonProperty("ChequeIssueBank")
        public String chequeIssueBank;
        @JsonProperty("CreditValueDate")
        public String creditValueDate;
        @JsonProperty("DebitAmount")
        public String debitAmount;
        @JsonProperty("ChequeNumber")
        public String chequeNumber;
        @JsonProperty("CreditAcctNo")
        public String creditAcctNo;
        @JsonProperty("BcSortCode")
        public String bcSortCode;
        @JsonProperty("CreditCurrency")
        public String creditCurrency;
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
