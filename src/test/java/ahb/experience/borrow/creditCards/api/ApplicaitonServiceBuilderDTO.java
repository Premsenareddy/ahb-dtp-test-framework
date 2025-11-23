package ahb.experience.borrow.creditCards.api;

import ahb.experience.creditCard.ExpApplicationService.*;

import java.util.ArrayList;

public class ApplicaitonServiceBuilderDTO {

    public static EXPCreateApplication createAppReq() {
        ArrayList<Address> listAddress = new ArrayList<>();
        listAddress.add( Address.builder().build());

        ArrayList<Consent> listConsent = new ArrayList<>();
        listConsent.add( Consent.builder().build());
        ArrayList<IncomeDetail> listIncome = new ArrayList<>();
        listIncome.add( IncomeDetail.builder().build());

        return  EXPCreateApplication.builder()
                .addresses(listAddress)
                .consents(listConsent)
                .incomeDetails(listIncome)
                .personalDetails( PersonalDetails.builder().build())
                .productDetails( ProductDetails.builder().build())
                .employmentDetails( EmploymentDetails.builder().build())
                .salaryDetails( SalaryDetails.builder().build())
                .build();
    }

    public static final String request = "{\n" +
            "    \"productCategory\": \"CREDIT_CARD\",\n" +
            "    \"personalDetails\": {\n" +
            "        \"maritalStatus\": \"S\",\n" +
            "        \"academicDegree\": \"1\"\n" +
            "    },\n" +
            "    \"productDetails\": {\n" +
            "        \"productCode\": \"P123\",\n" +
            "        \"productType\": \"CREDIT_CARD\",\n" +
            "        \"productName\": \"Etihad\"\n" +
            "    },\n" +
            "    \"addresses\": [\n" +
            "        {\n" +
            "            \"country\": \"India\",\n" +
            "            \"city\": \"Delhi\",\n" +
            "            \"address\": \"Cannaught Palace\",\n" +
            "            \"phoneCountryCode\": \"+91\",\n" +
            "            \"phoneNumber\": \"9999999999\",\n" +
            "            \"addressType\": \"HOME\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"employmentDetails\": {\n" +
            "        \"employerName\": \"S\",\n" +
            "        \"employerEmirate\": \"1\",\n" +
            "        \"employerAddress\": {\n" +
            "            \"country\": \"India\",\n" +
            "            \"city\": \"Delhi\",\n" +
            "            \"address\": \"Cannaught Palace\",\n" +
            "            \"phoneCountryCode\": \"+91\",\n" +
            "            \"phoneNumber\": \"9999999999\",\n" +
            "            \"addressType\": \"OFFICE\"\n" +
            "        },\n" +
            "        \"employmentStartDate\": \"09/2012\"\n" +
            "    },\n" +
            "    \"incomeDetails\": [\n" +
            "        {\n" +
            "            \"monthlyIncome\": \"11000\",\n" +
            "            \"incomeType\": \"CREDIT_CARD\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"salaryDetails\": {\n" +
            "        \"iban\": \"AE121212131314\",\n" +
            "        \"bankName\": \"Mashreq\",\n" +
            "        \"amount\": \"12000\"\n" +
            "    },\n" +
            "    \"consents\": [\n" +
            "        {\n" +
            "            \"isAccepted\": true,\n" +
            "            \"consentType\": \"MURABAHA\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
}
