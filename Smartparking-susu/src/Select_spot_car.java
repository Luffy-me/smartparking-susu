import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Select_spot_car")
public class Select_spot_car extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("Sign_in_customer.html");
            return;
        }

        String snum = request.getParameter("park");
        String pnum = (String) session.getAttribute("park_num");
        session.setAttribute("park", snum);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body>");
        out.println("<div id=\"text\">Logged in as " + SecurityUtil.escapeHtml((String) session.getAttribute("username")));
        out.println("<form method=\"post\" action=\"LogoutServlet\" style=\"display:inline;float:right;\">" + SecurityUtil.csrfHiddenInput(session)
                + "<button type=\"submit\">Logout</button></form><br>");
        out.println("<br> Spot selected successfully.");

        out.println("<h3>Park Now</h3>");
        out.println("<form method=\"post\" action=\"Final_Car_Book\">" + SecurityUtil.csrfHiddenInput(session)
                + "<input type=\"submit\" name=\"parking\" value=\"park\"></form>");

        out.println("<h3>Advanced Booking</h3>");
        out.println("<form method=\"post\" action=\"Final_Car_Book\">" + SecurityUtil.csrfHiddenInput(session));
        int day = LocalDate.now().getDayOfMonth();
        out.println("Date : <select name=\"date\"><option value=\"" + day + "\">" + day + "</option><option value=\""
                + (day + 1) + "\">" + (day + 1) + "</option></select><br>");
        out.println("Hour : <select name=\"hour\">");
        for (int i = 0; i <= 23; i++) {
            out.println("<option value=\"" + i + "\">" + i + "</option>");
        }
        out.println("</select><br><input type=\"submit\" name=\"parking\" value=\"book\"></form>");
        out.print("</div></body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
