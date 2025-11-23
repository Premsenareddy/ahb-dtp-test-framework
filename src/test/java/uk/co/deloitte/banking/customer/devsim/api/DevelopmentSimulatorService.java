package uk.co.deloitte.banking.customer.devsim.api;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import uk.co.deloitte.alpha.error.model.OBErrorResponse1;
import uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth.OtpCO;
import uk.co.deloitte.banking.ahb.dtp.test.devSim.DevSimConfiguration;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.DocumentGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.Request;
import uk.co.deloitte.banking.ahb.dtp.test.documents.models.StatementGenerationRequestEvent;
import uk.co.deloitte.banking.ahb.dtp.test.util.AlphaTestUser;
import uk.co.deloitte.banking.base.BaseApi;
import uk.co.deloitte.banking.http.common.Alt;
import uk.co.deloitte.banking.http.kafka.BaseEvent;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@Slf4j
public class DevelopmentSimulatorService extends BaseApi {

    @Inject
    DevSimConfiguration devSimConfiguration;

    private static final String API_STATEMENTS_GENERATE = "/api/statements/generate";
    private static final String API_DOCUMENTS_GENERATE = "/api/documents/generate";
    private static final String API_OTPS_KEY = "/api/otps/{key}";

    public OtpCO retrieveOtpFromDevSimulator(String key) {
        final String customerPath = devSimConfiguration.getBasePath() + "/api/otps/" + key;

        log.info("OTP url : {}", customerPath);

        return given()
                .config(config)
                .pathParam("key", key)
                .log().all()
                .header(X_API_KEY, devSimConfiguration.getApiKey())
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath() + API_OTPS_KEY)
                .then().log().all().statusCode(200).assertThat()
                .body("Password", notNullValue())
                .extract().body().as(OtpCO.class);
    }

    public OtpCO retrieveOtpFromDevSimulator2(String resetPasswordHash, String jwt) {
        final String customerPath = devSimConfiguration.getBasePath() + "/api/otps/" + resetPasswordHash;

        log.info("OTP url : {}", customerPath);

        return given()
                .config(config)
                .pathParam("key", resetPasswordHash)
                .log().all()
                .header(X_API_KEY, devSimConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + jwt)
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath() + API_OTPS_KEY)
                .then().log().all().statusCode(200).assertThat()
                .body("Password", notNullValue())
                .extract().body().as(OtpCO.class);
    }

    public OBErrorResponse1 retrieveOtpFromDevSimulatorErrorResponse(String resetPasswordHash, String jwt, int statusCode) {
        final String customerPath = devSimConfiguration.getBasePath() + "/api/otps/" + resetPasswordHash;

        log.info("OTP url : {}", customerPath);

        return given()
                .config(config)
                .pathParam("key", resetPasswordHash)
                .log().all()
                .header(X_API_KEY, devSimConfiguration.getApiKey())
                .header(AUTHORIZATION, "Bearer " + jwt)
                .contentType(ContentType.JSON)
                .when()
                .get(devSimConfiguration.getBasePath() + API_OTPS_KEY)
                .then().log().all().statusCode(statusCode).assertThat()
                .extract().body().as(OBErrorResponse1.class);
    }

    public void generateStatement(final StatementGenerationRequestEvent statementGenerationRequestEvent, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(X_API_KEY, devSimConfiguration.getApiKey())
                .header(AUTHORIZATION, "Basic " + generateDevSimBasicAuth())
                .contentType(ContentType.JSON)
                .when()
                .body(statementGenerationRequestEvent)
                .post(devSimConfiguration.getBasePath() + API_STATEMENTS_GENERATE)
                .then().log().all().statusCode(statusCode);

    }

    public void generateDocument(DocumentGenerationRequestEvent documentGenerationRequest, int statusCode) {
        given()
                .config(config)
                .log().all()
                .header(AUTHORIZATION, "Basic " + generateDevSimBasicAuth())
                .contentType(ContentType.JSON)
                .when()
                .body(documentGenerationRequest)
                .post(devSimConfiguration.getBasePath() + API_DOCUMENTS_GENERATE)
                .then().log().all().statusCode(statusCode);
    }

    private String generateDevSimBasicAuth() {
        return Base64.getEncoder().encodeToString(("dev-cleardown-user:ZGQyNDhkYTAtZjBhZS00NmEyLTkyYmEtZmM3YWVhZmFhMTli").getBytes(StandardCharsets.UTF_8));
    }

    public DocumentGenerationRequestEvent getKYCDocumentRequest(final AlphaTestUser alphaTestUser) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("KYC")
                        .customerId(alphaTestUser.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("Barcode4", "barcode");
                                put("additionalincomeamount", "9999");
                                put("additionalincomesource", "5000");
                                put("addressproof", "Lorem ipsum dolor sit amet; consectetur adipiscing elit; sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam; quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident; sunt in culpa qui officia deserunt mollit anim id est laborum");
                                put("annualnetincome", "99999");
                                put("annualsalary", "10000");
                                put("annualturnover", "88888");
                                put("bankaddress1", "100 Bank Street; London");
                                put("bankaddress2", "101 Bank Street; London");
                                put("bankaddress3", "102 Bank Street; London");
                                put("bankaddress4", "");
                                put("bankaddress5", "");
                                put("bankaddress6", "");
                                put("bankname1", "Barclays");
                                put("bankname2", "Nat West");
                                put("bankname3", "Bank 3");
                                put("bankname4", "");
                                put("bankname5", "");
                                put("bankname6", "");
                                put("branch", "Dubai");
                                put("businessaccountheld", "Business Helder");
                                put("businessaddress", "Business address");
                                put("businessname", "Business name");
                                put("businessrelationship", "Business Relationship");
                                put("businesswebsite", "www.business.com");
                                put("businessyesno", "Yes");
                                put("cashamount1", "60");
                                put("cashamount2", "99");
                                put("cashtxn1", "5");
                                put("cashtxn2", "10");
                                put("cifnumber", "1234567");
                                put("crossborderpurpose", "Purpose");
                                put("crossinwardamount", "Inward amnt");
                                put("crossinwardtxn", "In tx");
                                put("crossoutwardamount", "Out amnt");
                                put("crossoutwardtxn", "Out tx");
                                put("crosspaymentcountries", "");
                                put("currentdesignation", "F");
                                put("customercategory", "B");
                                put("date", "10/4/2021");
                                put("deliverybranch", "Delivery branch");
                                put("deliveryoption", "Delivery option");
                                put("deliverystatus", "Delivery status");
                                put("documenttype", "Type");
                                put("employeraddress", "4 Romford Road; London");
                                put("employername", "Deloitte");
                                put("employerwebsite", "www.deloitte.com");
                                put("employmentstatus", "Employed");
                                put("estimatetotalannumincome", "20000");
                                put("existingrelationsince", "10/10/2010");
                                put("expectedannualbonus", "5000");
                                put("fullname", "Full Name");
                                put("groupheadid", "Group head");
                                put("groupheadname", "Head name");
                                put("hawalayesno", "No");
                                put("homecountryresidence", "UK; reason is AHB is such an awesome bank. This is a long sentence to test the spacing and line breaks.");
                                put("interviewerstaffid", "123ABC");
                                put("interviewerstaffname", "Jane");
                                put("iscustomerdefense", "Is defense");
                                put("linemanagerid", "456ABC");
                                put("linemanagername", "Mary");
                                put("makecrossborder", "No");
                                put("nationality", "British");
                                put("natureemployerbusiness", "Finance");
                                put("natureofbusinessactivity", "Lorem ipsum dolor sit amet; consectetur adipiscing elit; sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam; quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident; sunt in culpa qui officia deserunt mollit anim id est laborum");
                                put("noncashamount1", "4000");
                                put("noncashamount2", "5000");
                                put("noncashtxn1", "30");
                                put("noncashtxn2", "50");
                                put("noofemployees", "99");
                                put("noofyearsinbusiness", "1");
                                put("noofyearsinuae", "5");
                                put("noofyearswithemployer", "3");
                                put("otherallowance", "3000");
                                put("ownershippercentage", "100");
                                put("pepnames", "Example PEP");
                                put("peppoa", "Yes");
                                put("previousoccupation", "Lorem ipsum dolor sit amet; consectetur adipiscing elit; sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam; quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident; sunt in culpa qui officia deserunt mollit anim id est laborum");
                                put("purposeaccountopen", "Lorem ipsum dolor sit amet; consectetur adipiscing elit; sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam; quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident; sunt in culpa qui officia deserunt mollit anim id est laborum");
                                put("purposeofprofiling", "purpose");
                                put("purposeyesno", "Yes");
                                put("reasonforbankinginuae", "Reason UAE");
                                put("remarks", "Remarks");
                                put("residencestatus", "Residence status");
                                put("riskrating", "Risk rating");
                                put("risksanctioncountry", "Risk country");
                                put("salaryaccountheld", "Person Name");
                                put("salarytransfer", "Yes");
                                put("sanctionednationality", "Nationality");
                                put("sanctionednationalityresident", "Not resident");
                                put("santionyesno", "No");
                                put("selfemployedbusinessnature", "Self employment business nature");
                                put("selfemployedyesno", "No");
                                put("sourcefunds", "Source funds");
                                put("sourceofsponsorincome", "Cash");
                                put("specificcomments", "Lorem ipsum dolor sit amet; consectetur adipiscing elit; sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam; quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident; sunt in culpa qui officia deserunt mollit anim id est laborum");
                                put("sponsorname", "Sponsor Name");
                                put("title", "Mr");
                                put("unitheadid", "Head ID");
                                put("unitheadname", "Head name");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "12345",
                                "EIDNumber", "987654321"
                        ))
                        .build())
                .build();
    }

    public DocumentGenerationRequestEvent getAccountOpeningDocumentRequest(final AlphaTestUser alphaTestUser) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("AC_OPEN")
                        .customerId(alphaTestUser.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("AccountNo", "11111111");
                                put("AccountTitle", "Account title");
                                put("AccountType", "Account type");
                                put("AddSourceofIn", "Add source");
                                put("AnnualExpenses", "5000");
                                put("AnnualTurnover", "900000");
                                put("Branch", "Branch");
                                put("BuildingNameNo", "Name no");
                                put("BusinessName", "Business name");
                                put("BusinessType", "Business type");
                                put("CIF", "1234567");
                                put("WatchListCheckBox", "Yes");
                                put("USIndicaFoundNoCheckBox", "Off");
                                put("W8CheckBox", "Off");
                                put("W9CheckBox", "Yes");
                                put("ChequeBookLeaves", "Cheque");
                                put("ContactNo", "0111111111");
                                put("ContactPerson", "Contact Person Name");
                                put("Country", "UK");
                                put("CountryOfBirth", "UK");
                                put("Currency", "Currency");
                                put("CustomerFullName", "Full name");
                                put("Date", "01/02/2020");
                                put("DateofBirth", "01/01/2000");
                                put("DateofEmployment", "01/01/2010");
                                put("Department", "Department");
                                put("Designation", "Designation Title");
                                put("DetailsOfSourceOfIn", "Income additional details");
                                put("Document Type", "Document type");
                                put("DualNationality", "UK");
                                put("EIDAExpiryDate", "01/01/2032");
                                put("EStatement", "E-statement");
                                put("EmailAddress", "email@ahb.com");
                                put("EmiratesIDNo", "QWE123");
                                put("EmployerName", "Employ name");
                                put("Employeraddress", "Employ address");
                                put("EmploymentStatus", "Employ status");
                                put("FamilyName", "Family name");
                                put("FingerprintServices", "Fingerprint");
                                put("Gender", "M");
                                put("HighestEducation", "University");
                                put("HomeCountry", "UK");
                                put("HomeCountryBuildingNameNo", "Bil name");
                                put("HomeCountryPOBox", "789");
                                put("HomeCountryPostCodeCity", "Code city");
                                put("HomeCountryResidenceNumber", "Red no");
                                put("HomeCountryStreet", "Street");
                                put("IncomingVol", "Incoming vol");
                                put("InternetBanking", "Net bank");
                                put("JointACC", "Joint acc");
                                put("JointCIF", "Joint CIF");
                                put("JointREL", "Joint rel");
                                put("MaritalStatus", "Married");
                                put("MobileBanking", "Mobile banking");
                                put("MobileNumber", "0987654321");
                                put("ModeOfOperation", "Mode");
                                put("MonthlySalary", "10000");
                                put("MotherName", "Mother name");
                                put("Nationality", "UAE");
                                put("NoOfDependents", "3");
                                put("NoOfTrxnsPerMonth", "Tx Pm");
                                put("OfficeBuildingNameNo", "F5");
                                put("OfficeCountry", "Office country");
                                put("OfficeNumber", "123");
                                put("OfficePOBox", "456");
                                put("OfficePostCodeCity", "London");
                                put("OfficeStreet", "Off Street");
                                put("OutgoingVol", "Out vol");
                                put("POBox", "123");
                                put("PassportExpiryDate", "01/01/2030");
                                put("PassportNo", "ABC123");
                                put("PostCodeCity", "City");
                                put("PreferredLanguage", "English");
                                put("PurposeOfAccount", "Purpose");
                                put("ReferredBy", "Referred");
                                put("Relationship", "Relation");
                                put("ResidenceEmirate", "Residence Emirate");
                                put("ResidenceNumber", "444");
                                put("ResidenceVisaNo", "ZYX789");
                                put("SMSBanking", "SMS");
                                put("SalaryDate", "09/09/2010");
                                put("ShoppingCardColor", "Yellow");
                                put("Signatories", "Signatories");
                                put("SocialSecurityNumber", "999999");
                                put("SourceOfIncome", "Income source");
                                put("SourceofIncomeCo", "Source co");
                                put("Street", "Street");
                                put("Title", "Title");
                                put("TypeOfTrxns", "Tx type");
                                put("UIDNo", "QQQ000");
                                put("VisaExpiryDate", "01/01/2031");
                                put("YearsinUAE", "10");
                                put("by", "by");
                                put("customername", "Customer Name");
                                put("embossingname", "Embossing name");
                                put("fullname", "Full Name");
                                put("homecountrymobilenumber", "home country mobile");
                                put("homecountrynearestlandmark", "landmark");
                                put("inputterid", "inputter id");
                                put("inputtername", "inputter name");
                                put("officeMobileNumber", "Office Mobile Number");
                                put("officeemirate", "Office Emirate");
                                put("officenearestlandmark", "Office landmark");
                                put("preferredhomecountryaddress", "Yes");
                                put("preferredofficeaddress", "Yes");
                                put("preferredresidenceaddress", "Yes");
                                put("residencenearestlandmark", "nearest landmark");
                                put("signaturedate", "10/02/2021");
                                put("verifierid", "verify id");
                                put("verifiername", "verify name");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "12345",
                                "EIDNumber", "987654321",
                                "AccountNumber", "11110000,99998888"
                        ))
                        .build())
                .build();
    }

    public DocumentGenerationRequestEvent getCRSDocumentRequest(final AlphaTestUser alphaTestUser) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("CRS")
                        .customerId(alphaTestUser.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("SignatureTextENG", "Signature ENG");
                                put("SignatureTextAR", "Signature AR");
                                put("CertifyENG", "On");
                                put("CertifyAR", "On");
                                put("SigDateENG", "01/01/2000");
                                put("SigDateAR", "01/01/1999");
                                put("TaxQueENG", "Tax Queue ENG");
                                put("TaxQueAR", "Tax Queue AR");
                                put("ReasonOtherENG", "Other reason ENG");
                                put("ReasonOtherAR", "Other reason AR");
                                put("TIN1", "123ABC");
                                put("ReasonNoTin1", "A");
                                put("COB", "AE");
                                put("EmirateState", "Dubai");
                                put("CityOfBirth", "Dubai");
                                put("Country", "AE");
                                put("City", "Dubai");
                                put("VilFlatNo", "17");
                                put("CIDNoAR", "CID");
                                put("LastName", "Last Name");
                                put("Street", "Sunbay Area");
                                put("BldgName", "16");
                                put("CIDNoENG", "CID UK");
                                put("DateAR", "01/12/2020");
                                put("DateENG", "01/12/2021");
                                put("COTaxRes2", "Residency");
                                put("TIN2", "456DEF");
                                put("ReasonNoTin2", "B");
                                put("COTaxRes1", "Tax res");
                                put("TaxResENG", "Yes");
                                put("TaxResAR", "Yes");
                                put("TaxRISENG", "Yes");
                                put("TaxRISAR", "Yes");
                                put("FirstName", "First Name");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "12345",
                                "EIDNumber", "987654321"
                        ))
                        .build())
                .build();
    }

    public DocumentGenerationRequestEvent getIBANDocumentRequest(final AlphaTestUser alphaTestUser) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("IBAN")
                        .customerId(alphaTestUser.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("Date", "01/01/2021");
                                put("StaffID", "CIF8888888");
                                put("FullName", "Full Name");
                                put("AccountNumber", "Account number");
                                put("AccountOpenDate", "01/01/2020");
                                put("IBAN", "Test IBAN 123");
                            }
                        })
                        .documentMetadata(Map.of(
                                "AccountNumber", "123456789",
                                "EIDNumber", "987654321",
                                "CIF", "12345"))
                        .build())
                .build();
    }

    public DocumentGenerationRequestEvent getGRANTORDocumentRequest(final AlphaTestUser alphaTestUser, final AlphaTestUser alphaTestUserChild) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("GRANTOR")
                        .customerId(alphaTestUserChild.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("SonDauNameENG", "Jim");
                                put("SignatureEn", "Jim Sig");
                                put("Date", "01/01/2020");
                                put("Name", "Bob");
                                put("SavAccNoENG", "12345678");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "123456789",
                                "EIDNumber", "987654321"))
                        .build())
                .build();
    }

    public DocumentGenerationRequestEvent getW8DocumentRequest(final AlphaTestUser alphaTestUser) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("W8")
                        .customerId(alphaTestUser.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("Nationality", "UAE");
                                put("StreetAddress", "Street address");
                                put("City", "Dubai");
                                put("Country", "UAE");
                                put("OfficeStreetAddress", "Office street address");
                                put("OfficeCity", "Office city");
                                put("OfficeCountry", "Office country");
                                put("DOB", "10/10/2000");
                                put("Residence", "Permanent residence address");
                                put("FullName", "Full Name");
                                put("signaturescript", "Signature");
                                put("Date", "10/10/2020");
                                put("Relationship", "Mother");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "12345",
                                "EIDNumber", "987654321"
                        ))
                        .build())
                .build();
    }

    public DocumentGenerationRequestEvent getW9DocumentRequest(final AlphaTestUser alphaTestUser) {
        return DocumentGenerationRequestEvent.builder()
                .request(Request.builder()
                        .origin("PTC")
                        .templateId("W9")
                        .customerId(alphaTestUser.getCustomerId())
                        .customerName(alphaTestUser.getName())
                        .metadata(BaseEvent.builder()
                                .alt(Alt.builder()
                                        .userId(alphaTestUser.getUserId())
                                        .build())
                                .date(LocalDateTime.of(LocalDate.of(2021, 04, 10), LocalTime.of(12, 00)))
                                .origin("PTC")
                                .build())
                        .fieldMappings(new HashMap<>() {
                            {
                                put("SocialSecurityNumber","123 AB WXYZ");
                                put("BusinessName","Business name");
                                put("Trustestate","On");
                                put("Limited liability company Enter the tax classification CC corporation SS corporation Ppartnership a","On");
                                put("City","City / State / ZIP");
                                put("Individualsole proprietor","On");
                                put("S Corporation","On");
                                put("StreetAddress","Street address");
                                put("C Corporation","On");
                                put("Partnership","On");
                                put("Other see instructions a","On");
                                put("SignatureDate","10/10/2021");
                                put("FullName","Full name");
                                put("Signature","Signature text");
                                put("Exempt payee","On");
                            }
                        })
                        .documentMetadata(Map.of(
                                "CIF", "12345",
                                "EIDNumber", "987654321"
                        ))
                        .build())
                .build();
    }
}
