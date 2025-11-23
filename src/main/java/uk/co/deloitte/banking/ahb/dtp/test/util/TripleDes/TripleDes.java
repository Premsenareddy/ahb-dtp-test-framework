package uk.co.deloitte.banking.ahb.dtp.test.util.TripleDes;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Singleton
public class TripleDes {

    public static byte[] encrypt(byte[] bMessage, byte[] bKey, String transformation)
    {
        try {
            final SecretKey secretKey = new SecretKeySpec(bKey, "DESede");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            final byte[] bCipherMessage = cipher.doFinal(bMessage);
            return bCipherMessage;
        } catch (NoSuchPaddingException e) {
			throw new RuntimeException("No Such Padding", e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No Such Algorithm", e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("Invalid Key", e);
		} catch (BadPaddingException e) {
			throw new RuntimeException("Invalid Key", e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("Invalid Key", e);
		}
    }

    public static byte[] decrypt(byte[] bCipherMessage, byte[] bKey, String transformation)
    {
        try {
            final SecretKey secretKey = new SecretKeySpec(bKey, "DESede");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            final byte[] bMessage = cipher.doFinal(bCipherMessage);
            return bMessage;
        } catch (NoSuchPaddingException e) {
			throw new RuntimeException("No Such Padding", e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No Such Algorithm", e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("Invalid Key IK", e);
		} catch (BadPaddingException e) {
			throw new RuntimeException("Invalid Key BP", e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("Invalid Key IBSE", e);
		}
    }

    public static byte[] makeupKey(String hexKey) {
		byte[] keyBytes;
		try {
			keyBytes = Hex.decodeHex(hexKey.toCharArray());
			final byte[] encryptionKey = Arrays.copyOf(keyBytes, 24);
			if (keyBytes.length == 16) {
				for (int j = 0, k = 16; j < 8;) {
					encryptionKey[k++] = keyBytes[j++];
				}
			}
			return encryptionKey;
		} catch (DecoderException e) {
			throw new RuntimeException("Hex decoder failed!", e);
		}
	}

    public static byte[] buildKey(String keySeed) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			final byte[] digestOfPassword = md.digest(keySeed.getBytes("utf-8"));
			final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
			return keyBytes;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unsupported digest algorithm", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding", e);
		}
	}

	public static byte[] hex2byte(String hex) throws Throwable {
		boolean var2 = (hex.length() & 1) != 1;
		boolean var3 = false;
		boolean var4 = false;
		var4 = false;
		boolean var5 = false;
		if (!var2) {
			boolean var17 = false;
			String var16 = "Failed requirement.";
			throw (Throwable) (new IllegalArgumentException(var16.toString()));
		} else {
			byte[] bytes = new byte[hex.length() / 2];
			int idx = 0;

			for (int var14 = bytes.length; idx < var14; ++idx) {
				int hi = Character.digit(hex.charAt(idx * 2), 16);
				int lo = Character.digit(hex.charAt(idx * 2 + 1), 16);
				boolean var7 = hi >= 0 && lo >= 0;
				boolean var8 = false;
				boolean var9 = false;
				var9 = false;
				boolean var10 = false;
				if (!var7) {
					boolean var11 = false;
					String var18 = "Failed requirement.";
					throw (Throwable) (new IllegalArgumentException(var18.toString()));
				}

				bytes[idx] = (byte) (hi << 4 | lo);
			}

			return bytes;
		}
	}

	public static String byte2hex(byte[] bytes) {
		char[] hex = new char[bytes.length * 2];
		int idx = 0;

		for (int var4 = bytes.length; idx < var4; ++idx) {

			int hi = (bytes[idx] & 240) >>> 4;
			int lo = (bytes[idx] & 15);
			hex[idx * 2] = (char) (hi < 10 ? 48 + hi : 55 + hi);
			hex[idx * 2 + 1] = (char) (lo < 10 ? 48 + lo : 55 + lo);
		}

		boolean var7 = false;
		return new String(hex);
	}
}