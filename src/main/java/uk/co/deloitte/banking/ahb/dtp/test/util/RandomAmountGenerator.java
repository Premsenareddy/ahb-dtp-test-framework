package uk.co.deloitte.banking.ahb.dtp.test.util;

import java.util.Random;

public class RandomAmountGenerator {
    private static Random random = new Random();
    public static double generateAmount() {
        return Math.round((random.nextDouble() + 10 + random.nextInt(50)) * 100.0) / 100.0;
    }
}
