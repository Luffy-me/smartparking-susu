package javax.servlet.http;

public interface HttpSession {
    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    default void setMaxInactiveInterval(int interval) {
    }

    default void invalidate() {
    }
}
