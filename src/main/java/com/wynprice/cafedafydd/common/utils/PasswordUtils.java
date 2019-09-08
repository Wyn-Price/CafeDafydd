package com.wynprice.cafedafydd.common.utils;

import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {

    private static final MessageDigest MD5 = getDigest();

    public static String genetatePasswordHash(String username, String password) {
        SecureRandom rand = new SecureRandom((username + password).getBytes());
        byte[] abyte = new byte[256];
        rand.nextBytes(abyte);

        return bytesToHex(MD5.digest(abyte));
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte abyte : hash) {
            String hex = Integer.toHexString(0xff & abyte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("SHA-256");
    }
}
