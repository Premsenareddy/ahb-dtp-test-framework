package uk.co.deloitte.banking.ahb.dtp.test.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import uk.co.deloitte.banking.customer.api.customer.model.OBGender;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class RandomDataGenerator {
    private static Random random = new Random();
    private static String[] validMobilePrefix = {"+55550", "+55552", "+55554", "+55555", "+55556", "+55558"};
    private static String[] randomAgesOver21 = {"21", "30", "35", "42", "50", "65"};
    private static String[] businessTypes = {"BUT", "FIB", "REB", "AAN", "CHO", "CRY", "FBV", "TOB", "ONL", "ELC"};
    private static String[] lapsCodes = {"1", "9", "11", "36", "40", "54", "61", "70", "80", "91"};
    private static String[] professionCodes = {"32", "3", "6", "14", "131", "44", "21", "134", "132", "99"};
    private static String[] twoLetterIsoCodes = {"AE", "CY", "ME", "IN", "FR", "GB"};
    private static String[] validMobilePrefixExperience = {"+97150", "+97152", "+97154", "+97155", "+97156", "+97158"};


    public static OBGender generateRandomGender() {
        return new Random().nextInt(2) == 1 ? OBGender.MALE : OBGender.FEMALE;
    }

    public static String generateRandomLanguage() {
        return new Random().nextInt(2) == 1 ? "en" : "ar";
    }

    public static String generateRandomString() {
        return generateRandomString(10);
    }

    public static String generateRandomString(int max) {
        return generateEnglishRandomString(max);
    }

    public static String generateEnglishRandomString(int max) {
        return RandomStringUtils.randomAlphabetic(max).toLowerCase();
    }

    public static Long generateRandomLong() {
        return  Math.abs(random.nextLong());
    }

    public static String generateRandomAlphanumericUpperCase(int length) {
        return RandomStringUtils.randomAlphanumeric(length).toUpperCase();
    }

    public static String generateRandomEmail() {
        return UUID.randomUUID().toString().substring(0, 30) + "@ahb.com";
    }

    public static String generateRandomMobile() {
        return validMobilePrefix[new Random().nextInt(validMobilePrefix.length)] + random.ints(1_000_000, 9_999_999).findFirst().getAsInt();
    }

    public static String generateRandomMobileExperience() {
        return validMobilePrefixExperience[new Random().nextInt(validMobilePrefixExperience.length)] + random.ints(1_000_000, 9_999_999).findFirst().getAsInt();
    }

    public static long generateRandomAge() {
        return Long.valueOf(randomAgesOver21[new Random().nextInt(randomAgesOver21.length)]);
    }

    public static String generateRandomBusinessCode() {
        return businessTypes[new Random().nextInt(businessTypes.length)];
    }

    public static String generateRandomCountryCode() {
        return twoLetterIsoCodes[new Random().nextInt(twoLetterIsoCodes.length)];
    }

    public static String generateRandomProfessionCode() {
        return professionCodes[new Random().nextInt(professionCodes.length)];
    }

    public static String generateRandomLAPSCode() {
        return lapsCodes[new Random().nextInt(lapsCodes.length)];
    }

    public static Integer generateRandomInteger(Integer max) {
        return random.nextInt(max) + 1000;
    }

    public static Integer generateRandomIntegerInRange(Integer min, Integer max) {
        return min + random.nextInt((max - min) + 1);
    }

    public static String generateRandomSHA512enabledPassword() {
        // create a random 4 digit number as the mobile customer will enter this as their account passcode
        String passcode = String.format("%04d", random.nextInt(10000));
        log.info("4 digit passcode is {}", passcode);

        //Generate a SHA-512 enabled string
        //Reference: http://oliviertech.com/java/generate-SHA256--SHA512-hash-from-a-String/
        String password = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(passcode.getBytes(StandardCharsets.UTF_8));
            password = String.format("%0128x", new BigInteger(1, digest.digest()));
            log.info("SHA 512 generated password is {}", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return password;

    }

    public static String generateRandomSwift() {
        return "HLALAEAA" + random.nextInt(999);
    }

    public static String generateRandomBuildingNumber() {
        return Long.toString(Math.round(Math.random()*100));
    }
    public static String generateRandomPostalCode() {
        return "PO Box " + Long.toString(random.nextInt(1000000));
    }
    public static String generateRandomAddressLine() {
        if (Math.random()>0.5) {
            return "Flat " + Long.toString(random.nextInt(100));
        }
        else {
            return "Sector " + (char)(random.nextInt(26) + 'a') + Long.toString(Math.round(Math.random()*10));
        }
    }
    public static String generateRandomCityOfBirth() {
        return generateRandomTownName().replace("-", " ");
    }
    public static String generateRandomTownName() {
        ArrayList<String> towns = new ArrayList<>();
        Collections.addAll(towns, "Abu Dhabi City", "Al Ain", "Ruwais", "Ghayathi", "Madinat Zayed", "Liwa Oasis", "Ajman", "Dubai City", "Hatta", "Fujairah", "Dibba Al-Fujairah", "RAK City", "Sharjah", "Khor Fakkan", "Kalba", "Dhaid", "Dibba Al-Hisn", "Al Madam", "Umm al-Quwain");
        int randIndex = random.nextInt(towns.size());
        return towns.get(randIndex);
    }
    public static String generateRandomCountrySubDivision() {
        ArrayList<String> towns = new ArrayList<>();
        Collections.addAll(towns, "Abu Dhabi", "Sharjah", "Ajman", "Ras Al Khaimah", "Fujairah", "Dubai", "Umm Al Quwain");
        int randIndex = random.nextInt(towns.size());
        return towns.get(randIndex);
    }
    public static String generateRandomStreetName() {
        ArrayList<String> towns = new ArrayList<>();
        Collections.addAll(towns, "Al Ain Rd", "Emirates Rd", "Sheikh Mohammed Bin Zayed Rd", "Sheikh Zayed Rd", "Al Khail Rd", "Jumeira Rd", "Hatta Rd", "Al Wasl Rd", "Al Hudaiba St");
        // TODO: Add "Dubai Bypass Rd" when bypass rejection bug fixed --- https://ahbdigitalbank.atlassian.net/browse/AHBDB-4705
        int randIndex = random.nextInt(towns.size());
        return towns.get(randIndex);
    }
    public static String generateRandomDepartment() {
        ArrayList<String> towns = new ArrayList<>();
        Collections.addAll(towns, "Al Bahr Towers", "Burj Khalifa", "Marina 101", "Princess Tower", "23 Marina", "Elite Residence", "Burj Mohammed bin Rashid", "Address Boulevard", "Almas Tower", "Rose Tower", "Gevora Hotel", "JW Marriott Marquis Dubai Tower 1");
        int randIndex = random.nextInt(towns.size());
        return towns.get(randIndex);
    }
    public static String generateRandomSubDepartment() {
        ArrayList<String> towns = new ArrayList<>();
        Collections.addAll(towns, "Al Hilal Bank Head Office", "Human Resources", "Accounting", "Investment Advisory Department", "Consulting", "Finance", "IT", "Operations management");
        int randIndex = random.nextInt(towns.size());
        return towns.get(randIndex);
    }

    public static String generateRandomEID() {
        int part1 = getRandomIntegerInRange(100, 999);
        int part2 = getRandomIntegerInRange(1000, 9999);
        int part3 = getRandomIntegerInRange(1000000, 9999999);
        int part4 = getRandomIntegerInRange(0, 9);

        return String.format("%d%d%d%d", part1, part2, part3, part4);
    }

    public static int getRandomIntegerInRange(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static LocalDate generateRandomDateOfBirth() {
        // CRM minimum dates from 01/01/1753
        return LocalDate.of(getRandomIntegerInRange(1753, 2000), getRandomIntegerInRange(1, 12), getRandomIntegerInRange(1, 28));
    }



    public static String generateRandomPinBlock() {
        return  RandomStringUtils.randomAlphanumeric(16).toUpperCase();
    }

    public static String generateRandomNumeric(int count) {
        return  RandomStringUtils.randomNumeric(count);
    }

    public static String generateRandomBNumericTwoDB(int count) {
        return RandomStringUtils.randomNumeric(count) + "." + RandomStringUtils.randomNumeric(2);
    }

}
