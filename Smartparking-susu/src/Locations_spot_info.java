import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Locations_spot_info")
public class Locations_spot_info extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("Sign_in_customer.html");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String username = SecurityUtil.escapeHtml((String) session.getAttribute("username"));
        int lcode = Integer.parseInt(request.getParameter("action"));

        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body>");
        out.println("<div id=\"text\">Logged in as " + username);
        out.println("<form method=\"post\" action=\"LogoutServlet\" style=\"display:inline;float:right;\">"
                + SecurityUtil.csrfHiddenInput(session)
                + "<button type=\"submit\">Logout</button></form>");

        try (Connection con = DbUtil.getConnection();
                PreparedStatement locationPs = con
                        .prepareStatement("select location_name,number_of_parking_lots from locations where code=?");
                PreparedStatement lotsPs = con
                        .prepareStatement("select parking_lot_name,park_number from parking_lot_info where location_code=?")) {
            locationPs.setInt(1, lcode);
            try (ResultSet r = locationPs.executeQuery()) {
                if (r.next()) {
                    out.println("<br> number of parking lots in " + SecurityUtil.escapeHtml(r.getString(1)) + ":"
                            + SecurityUtil.escapeHtml(r.getString(2)));
                }
            }

            lotsPs.setInt(1, lcode);
            out.println("<form action=\"Parking_spot_info\" method=\"get\">");
            try (ResultSet r3 = lotsPs.executeQuery()) {
                while (r3.next()) {
                    String pname = SecurityUtil.escapeHtml(r3.getString(1));
                    String pnum = SecurityUtil.escapeHtml(r3.getString(2));
                    out.println("<button type=\"submit\" name=\"action\" value=\"" + pnum + "\">" + pname + " (" + pnum
                            + ")</button><br>");
                }
            }
            out.println("</form>");
        } catch (Exception e) {
            getServletContext().log("Unable to load location", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Unable to load location details.");
        }

        out.println("</div></body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
