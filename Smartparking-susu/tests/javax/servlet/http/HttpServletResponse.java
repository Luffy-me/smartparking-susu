package javax.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;

public interface HttpServletResponse {
    int SC_FORBIDDEN = 403;
    int SC_INTERNAL_SERVER_ERROR = 500;

    void sendRedirect(String location) throws IOException;

    void setContentType(String type);

    PrintWriter getWriter() throws IOException;

    void setStatus(int status);

    boolean isCommitted();
}
