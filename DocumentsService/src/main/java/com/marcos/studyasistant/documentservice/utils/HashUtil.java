package com.marcos.studyasistant.documentservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
public class HashUtil {

    public static String generateSHA256Hash(String input) {
        log.info("Generating SHA-256 hash for document: " + input);

        if (input == null || input.isEmpty()) {
            log.warn("Input string is null or empty, returning empty hash.");
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no está disponible", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        log.info("Converting byte array to hex string.");
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}
