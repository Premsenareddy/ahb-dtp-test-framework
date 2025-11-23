package uk.co.deloitte.banking.ahb.dtp.test.banking.chequebook.Response.outwardchequebookreturn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class outwardChequeBookReturnSchema {

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
        @JsonProperty("Amount")
        public String amount;
        @JsonProperty("ChequeReturnCode")
        public String chequeReturnCode;
        @JsonProperty("ChequeStatus")
        public String chequeStatus;
        @JsonProperty("Currency")
        public String currency;
        @JsonProperty("ValueDate")
        public String valueDate;
        @JsonProperty("ChequeNumber")
        public String chequeNumber;
        @JsonProperty("AccountNumber")
        public String accountNumber;
        @JsonProperty("DepositReference")
        public String depositReference;
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
