package com.ecommerce.security;

import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Session-bound CSRF tokens for state-changing seller operations. */
public final class CsrfTokens {
    private static final String SESSION_KEY = CsrfTokens.class.getName() + ".token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokens() {
    }

    public static String getOrCreate(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("An active session is required.");
        }
        synchronized (session) {
            Object current = session.getAttribute(SESSION_KEY);
            if (current instanceof String token && !token.isBlank()) {
                return token;
            }
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_KEY, token);
            return token;
        }
    }

    public static boolean isValid(HttpSession session, String suppliedToken) {
        if (session == null || suppliedToken == null || suppliedToken.isBlank()) {
            return false;
        }
        Object current = session.getAttribute(SESSION_KEY);
        if (!(current instanceof String expected) || expected.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8));
    }
}
