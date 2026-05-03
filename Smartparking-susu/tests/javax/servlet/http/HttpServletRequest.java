package javax.servlet.http;

public interface HttpServletRequest {
    String getParameter(String name);

    default HttpSession getSession() {
        return getSession(true);
    }

    default HttpSession getSession(boolean create) {
        return null;
    }

    default String getContextPath() {
        return "";
    }
}
