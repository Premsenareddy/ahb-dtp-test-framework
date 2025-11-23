package uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;

@Singleton
public class TripleDesUtil {
    @Inject
    TripleDesConfig tripleDesConfig;

    public String decryptUserCardNumber(final String cipherMessage) throws Throwable {
        byte[] bCipherMessage = TripleDes.hex2byte(cipherMessage);
        final String hexKey = new String(Base64.getDecoder().decode(tripleDesConfig.getDecryptKey()));
        byte[] tmp = TripleDes.hex2byte(hexKey);
        byte[] key = new byte[24];
        System.arraycopy(tmp, 0, key, 0, 16);
        System.arraycopy(tmp, 0, key, 16, 8);
        byte[] bMessage = TripleDes.decrypt(bCipherMessage, key, "DESede/ECB/NoPadding");
        return TripleDes.byte2hex(bMessage).replace("F", "");
    }

    public String encryptUserPin(final String pin, final String plainAccountNumber) {
        final String hexKey = new String(Base64.getDecoder().decode(tripleDesConfig.getEncryptKey()));
        byte[] bKey = TripleDes.makeupKey(hexKey);
        return Pinblock.encode(pin, plainAccountNumber, bKey);
    }
}
