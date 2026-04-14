import java.util.Base64;

public class PasswordUtilTest {
    public static void main(String[] args) throws Exception {
        testHashAndVerifyHappyPath();
        testHashUsesRandomSalt();
        testVerifyNullStoredValue();
        testVerifyLegacyPlainTextFallback();
        testVerifyMalformedFormatReturnsFalse();
        testVerifyTamperedHashFails();
        testVerifyInvalidBase64Throws();
        testVerifyInvalidIterationThrows();
        System.out.println("PasswordUtilTest: all tests passed");
    }

    private static void testHashAndVerifyHappyPath() throws Exception {
        String password = "StrongPass123!";
        String hashed = PasswordUtil.hash(password);
        assertTrue(hashed.startsWith("PBKDF2$"), "hash should use PBKDF2 format prefix");
        String[] parts = hashed.split("\\$");
        assertEquals(4, parts.length, "hash should contain 4 parts");
        assertTrue(PasswordUtil.verify(password, hashed), "correct password should verify");
        assertFalse(PasswordUtil.verify("WrongPassword", hashed), "wrong password should fail verification");
    }

    private static void testHashUsesRandomSalt() throws Exception {
        String password = "same-password";
        String hash1 = PasswordUtil.hash(password);
        String hash2 = PasswordUtil.hash(password);
        assertFalse(hash1.equals(hash2), "hash values should differ due to random salt");
    }

    private static void testVerifyNullStoredValue() throws Exception {
        assertFalse(PasswordUtil.verify("any", null), "null stored value should return false");
    }

    private static void testVerifyLegacyPlainTextFallback() throws Exception {
        assertTrue(PasswordUtil.verify("legacy", "legacy"), "legacy plaintext should match exactly");
        assertFalse(PasswordUtil.verify("legacy", "other"), "legacy plaintext mismatch should fail");
    }

    private static void testVerifyMalformedFormatReturnsFalse() throws Exception {
        assertFalse(PasswordUtil.verify("pass", "PBKDF2$65536$onlyThreeParts"),
                "malformed PBKDF2 format should return false");
    }

    private static void testVerifyTamperedHashFails() throws Exception {
        String password = "keep-safe";
        String hashed = PasswordUtil.hash(password);
        String[] parts = hashed.split("\\$");
        byte[] hashBytes = Base64.getDecoder().decode(parts[3]);
        hashBytes[0] = (byte) (hashBytes[0] ^ 1);
        String tampered = parts[0] + "$" + parts[1] + "$" + parts[2] + "$" + Base64.getEncoder().encodeToString(hashBytes);
        assertFalse(PasswordUtil.verify(password, tampered), "tampered hash bytes should not verify");
    }

    private static void testVerifyInvalidBase64Throws() {
        assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Exception {
                PasswordUtil.verify("pass", "PBKDF2$65536$not_base64$not_base64");
            }
        }, "invalid base64 in PBKDF2 value should throw");
    }

    private static void testVerifyInvalidIterationThrows() {
        assertThrows(NumberFormatException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Exception {
                PasswordUtil.verify("pass", "PBKDF2$NaN$c2FsdA==$aGFzaA==");
            }
        }, "invalid iteration count should throw");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    private static void assertThrows(Class<? extends Throwable> expected, ThrowingRunnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                return;
            }
            throw new AssertionError(message + " (unexpected exception type: " + t.getClass().getName() + ")", t);
        }
        throw new AssertionError(message + " (no exception thrown)");
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
