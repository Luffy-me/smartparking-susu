package javax.servlet.http;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public abstract class HttpServlet {
    private final ServletContext servletContext = new ServletContext();

    protected ServletContext getServletContext() {
        return servletContext;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}
