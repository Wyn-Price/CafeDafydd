package com.wynprice.cafedafydd.common.utils;

import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A util class for all things password
 */
public class PasswordUtils {

    /**
     * The sha256 message digest instance.
     */
    private static final MessageDigest SHA256 = getDigest();

    /**
     * Generates a password hash based on the username and password
     * @param username the username to use
     * @param password the password to use
     * @return a 64 character long hash of the username + password
     */
    public static String generatePasswordHash(String username, String password) {
        SecureRandom rand = new SecureRandom((username + password).getBytes());
        byte[] abyte = new byte[256];
        rand.nextBytes(abyte);

        return bytesToHex(SHA256.digest(abyte));
    }

    /**
     * Converts a byte array to a String. Each byte gets converted to 2 hex characters.
     * @param array the array to convert.
     * @return A string representing the array, with double as much characters as the array has bytes.
     */
    public static String bytesToHex(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte abyte : array) {
            String hex = Integer.toHexString(0xff & abyte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Gets the sha-256 message digest without the potential to throw an exception.
     * @return the SHA-256 message digest
     * @see SneakyThrows
     */
    @SneakyThrows(NoSuchAlgorithmException.class)
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("SHA-256");
    }
}
