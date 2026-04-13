import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class SecurityUtil {
    private static final String CSRF_TOKEN_KEY = "csrfToken";

    private SecurityUtil() {
    }

    public static String ensureCsrfToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_KEY);
        if (token == null || token.isEmpty()) {
            token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_TOKEN_KEY, token);
        }
        return token;
    }

    public static boolean isValidCsrf(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(CSRF_TOKEN_KEY);
        String actual = request.getParameter("csrfToken");
        return expected != null && expected.equals(actual);
    }

    public static String csrfHiddenInput(HttpSession session) {
        return "<input type=\"hidden\" name=\"csrfToken\" value=\"" + escapeHtml(ensureCsrfToken(session)) + "\">";
    }

    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#x27;");
                    break;
                case '/':
                    out.append("&#x2F;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }
}
