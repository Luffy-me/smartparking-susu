import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SecurityUtilTest {
    public static void main(String[] args) {
        testEscapeHtml();
        testEnsureCsrfTokenCreatesAndReusesToken();
        testIsValidCsrf();
        testCsrfHiddenInputContainsEscapedToken();
        System.out.println("SecurityUtilTest: all tests passed");
    }

    private static void testEscapeHtml() {
        assertEquals("", SecurityUtil.escapeHtml(null), "null input should become empty string");
        assertEquals("", SecurityUtil.escapeHtml(""), "empty input should stay empty");
        assertEquals("&lt;tag&gt; &amp; &quot;x&#x27;y&#x2F;z&quot;",
                SecurityUtil.escapeHtml("<tag> & \"x'y/z\""),
                "special HTML characters should be escaped");
        assertEquals("plainText123", SecurityUtil.escapeHtml("plainText123"),
                "safe text should be unchanged");
    }

    private static void testEnsureCsrfTokenCreatesAndReusesToken() {
        MockSession session = new MockSession();
        String token1 = SecurityUtil.ensureCsrfToken(session);
        assertTrue(token1 != null && !token1.isEmpty(), "ensureCsrfToken should create a non-empty token");
        String token2 = SecurityUtil.ensureCsrfToken(session);
        assertEquals(token1, token2, "ensureCsrfToken should reuse existing token");
    }

    private static void testIsValidCsrf() {
        MockSession session = new MockSession();
        String expected = SecurityUtil.ensureCsrfToken(session);

        MockRequest okRequest = new MockRequest();
        okRequest.setParameter("csrfToken", expected);
        assertTrue(SecurityUtil.isValidCsrf(okRequest, session), "matching csrf token should validate");

        MockRequest badRequest = new MockRequest();
        badRequest.setParameter("csrfToken", "bad-token");
        assertFalse(SecurityUtil.isValidCsrf(badRequest, session), "mismatched csrf token should fail");

        MockRequest missingRequest = new MockRequest();
        assertFalse(SecurityUtil.isValidCsrf(missingRequest, session), "missing csrf token should fail");

        assertFalse(SecurityUtil.isValidCsrf(okRequest, null), "null session should fail validation");
    }

    private static void testCsrfHiddenInputContainsEscapedToken() {
        MockSession session = new MockSession();
        String html = SecurityUtil.csrfHiddenInput(session);
        assertTrue(html.contains("type=\"hidden\""), "hidden input should include type hidden");
        assertTrue(html.contains("name=\"csrfToken\""), "hidden input should include csrfToken name");
        assertTrue(html.contains("value=\""), "hidden input should include value attribute");

        session.setAttribute("csrfToken", "abc\"def");
        String escapedHtml = SecurityUtil.csrfHiddenInput(session);
        assertTrue(escapedHtml.contains("abc&quot;def"), "csrf token in hidden input should be HTML escaped");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    private static final class MockSession implements HttpSession {
        private final Map<String, Object> attributes = new HashMap<String, Object>();

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
    }

    private static final class MockRequest implements HttpServletRequest {
        private final Map<String, String> parameters = new HashMap<String, String>();

        void setParameter(String name, String value) {
            parameters.put(name, value);
        }

        @Override
        public String getParameter(String name) {
            return parameters.get(name);
        }
    }
}
