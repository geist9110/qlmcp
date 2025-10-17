package com.qlmcp.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class PkceVerifier {

    public boolean verify(
        String codeChallenge,
        String codeChallengeMethod,
        String codeVerifier
    ) {
        if ("S256".equals(codeChallengeMethod)) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                String computed = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

                return computed.equals(codeChallenge);

            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        } else if ("plain".equals(codeChallengeMethod)) {
            return codeVerifier.equals(codeChallenge);
        }

        return false;
    }
}
