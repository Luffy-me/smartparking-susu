package javax.servlet;

public class ServletContext {
    public void log(String message) {
        System.err.println(message);
    }

    public void log(String message, Throwable throwable) {
        System.err.println(message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}
