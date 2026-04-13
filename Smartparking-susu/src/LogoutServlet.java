import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("Sign_in_customer.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        if (!SecurityUtil.isValidCsrf(request, session)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("Invalid request.");
            return;
        }

        String username = session == null ? "" : SecurityUtil.escapeHtml((String) session.getAttribute("username"));
        if (session != null) {
            session.invalidate();
        }

        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body>");
        out.println("<h3>Username " + username + " successfully logged out! Please login again.</h3>");
        out.println("<h3><a href=\"Sign_in_customer.html\">Sign In</a></h3>");
        out.println("</body></html>");
    }
}
