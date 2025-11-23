package uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.creation;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import uk.co.deloitte.banking.account.api.openbanking.v3.model.account.OBExternalAccountType1Code;
import uk.co.deloitte.banking.journey.scenarios.adult.pdn.model.PDNData;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Getter
@Builder
@ToString
public class PDNDataAccounts extends PDNData {
    private final OBExternalAccountType1Code accountType;
    private final BigDecimal balance;
    private final List<PDNDataAccounts> dependants;

    //TODO - remove number 2
    private static final String ACCOUNT_CREATION_FILE = "account_creation-2.txt";

    private static final Map<String, OBExternalAccountType1Code> ACCOUNT_TYPE_DICTIONARY = Map.of(
            "BASICSAV", OBExternalAccountType1Code.AHB_BASIC_SAV,
            "YOUTHSAVING", OBExternalAccountType1Code.AHB_YOUTH_SAV,
            "SEGHAARSAVING", OBExternalAccountType1Code.AHB_SEGHAAR_SAV
    );


    public static List<PDNDataAccounts> getData() throws IOException, URISyntaxException {
        int indexOfMainAccountType = 0;
        int indexOfBalanceOfMainAccountType = 1;

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        if (classloader == null) {
            Assertions.fail("Class loader is null");
        }

        List<String> lines = Files.readAllLines(Paths.get(classloader.getResource(ACCOUNT_CREATION_FILE).toURI()), StandardCharsets.UTF_8);
        assertNotNull(lines);

        return lines.stream().map(line -> {
                    String[] split = line.split(DIVIDER);
                    int lineLength = split.length;

                    if (lineLength % 2 != 0) {
                        Assertions.fail("Data is incorrect. Either balance or account type is not provided. Line number -> " + lines.indexOf(line));
                    }
                    String mainAccountType = split[indexOfMainAccountType];
                    String balanceOfAccountType = split[indexOfBalanceOfMainAccountType];

                    if (StringUtils.isNotBlank(mainAccountType) && StringUtils.isNotBlank(balanceOfAccountType)) {
                        return builder()
                                .accountType(mapAccountType(mainAccountType))
                                .balance(mapBalance(balanceOfAccountType))
                                .dependants(
                                        formDependants(split, 2, new ArrayList<>())
                                )
                                .build();
                    }

                    Assertions.fail("Data is incorrect. Either balance or account type is not provided");
                    return null;
                }
        ).collect(Collectors.toList());
    }


    private static OBExternalAccountType1Code mapAccountType(String accountType) {
        String cleanItem = cleanItem(accountType);
        try {
            return OBExternalAccountType1Code.valueOf(cleanItem);
        } catch (IllegalArgumentException e) {
            OBExternalAccountType1Code obExternalAccountType1Code = ACCOUNT_TYPE_DICTIONARY.get(cleanItem.toUpperCase());

            if (obExternalAccountType1Code == null) {
                Assertions.fail("No such account type -> " + accountType);
            }

            return obExternalAccountType1Code;
        }
    }

    private static List<PDNDataAccounts> formDependants(String[] rawData, int indexOfFirstDependent, List<PDNDataAccounts> data) {
        if (indexOfFirstDependent == rawData.length) {
            return data;
        }

        String dependantAccountType = PDNData.cleanItem(rawData[indexOfFirstDependent]);
        String dependantBalance = cleanItem(rawData[++indexOfFirstDependent]);

        data.add(builder()
                .accountType(mapAccountType(dependantAccountType))
                .balance(mapBalance(dependantBalance))
                .build());

        return formDependants(rawData, ++indexOfFirstDependent, data);
    }
}
