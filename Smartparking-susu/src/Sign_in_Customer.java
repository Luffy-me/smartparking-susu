import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Sign_in_Customer")
public class Sign_in_Customer extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DUMMY_HASH = "PBKDF2$65536$wQhM4+0Y9YtZHhVfGmWQAA==$cybxzSN8VUPoI6Pq4Jsh6M6w1jTzP1P0v6VNE2pSE+8=";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("Sign_in_customer.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String custUname = request.getParameter("uname");
        String custPwd = request.getParameter("pwd");

        try (Connection con = DbUtil.getConnection();
                PreparedStatement ps = con.prepareStatement("select * from customer_info where customer_id=?")) {

            ps.setInt(1, Integer.parseInt(custUname));
            try (ResultSet r = ps.executeQuery()) {
                boolean found = r.next();
                String storedHash = found ? r.getString(5) : DUMMY_HASH;
                boolean valid = PasswordUtil.verify(custPwd, storedHash);
                if (!found || !valid) {
                    response.sendRedirect("Sign_in_customer.html?error=invalid-credentials");
                    return;
                }

                HttpSession session = request.getSession();
                String customerId = r.getString(1);
                session.setAttribute("username", r.getString(1));
                session.setAttribute("customerId", customerId);
                session.setMaxInactiveInterval(30 * 60);
                SecurityUtil.ensureCsrfToken(session);

                String customerName = SecurityUtil.escapeHtml(r.getString(2));
                String vehicleNumber = SecurityUtil.escapeHtml(r.getString(3));
                String vehicleType = "0".equals(r.getString(4)) ? "Bike" : "Car";

                out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body>");
                out.println("<div id=\"text\">");
                out.println("Logged in as " + SecurityUtil.escapeHtml(customerId));
                out.println("<form method=\"post\" action=\"LogoutServlet\" style=\"display:inline;float:right;\">"
                        + SecurityUtil.csrfHiddenInput(session)
                        + "<button type=\"submit\">Logout</button></form>");
                out.println(" <br><hr> Customer Name : " + customerName + "<br>Vehicle Number : " + vehicleNumber
                        + "<br>Vehicle type : " + vehicleType + "<br><hr>");

                renderBookingOrLocation(con, out, session, r.getString(1));
                out.println("</div>");
            }
        } catch (Exception e) {
            getServletContext().log("Sign in failed", e);
            if (!response.isCommitted()) {
                response.sendRedirect("Sign_in_customer.html?error="
                        + URLEncoder.encode("server", StandardCharsets.UTF_8.name()));
            } else {
                out.println("<div role=\"status\" style=\"margin-top:12px;color:#fca5a5;font-weight:600;\">"
                        + "Unable to sign in right now. Please try again later."
                        + "</div>");
            }
            return;
        }

        out.println("</body></html>");
    }

    private void renderBookingOrLocation(Connection con, PrintWriter out, HttpSession session, String customerId)
            throws Exception {
        try (PreparedStatement spot = con.prepareStatement("select * from parking_spot_info where cid=?")) {
            spot.setInt(1, Integer.parseInt(customerId));
            try (ResultSet r2 = spot.executeQuery()) {
                if (!r2.next()) {
                    out.println("You've not booked or parked in a parking lot yet.Select from the parking lots listed below : <br><hr>");
                    out.println("<form action=\"Locations_spot_info\" method=\"get\">");
                    try (PreparedStatement locations = con.prepareStatement("select location_name,code from locations");
                            ResultSet r3 = locations.executeQuery()) {
                        while (r3.next()) {
                            String lname = SecurityUtil.escapeHtml(r3.getString(1));
                            String lcode = r3.getString(2);
                            out.println("<button type=\"submit\" name=\"action\" value=\"" + SecurityUtil.escapeHtml(lcode)
                                    + "\">" + lname + "</button><br>");
                        }
                    }
                    out.println("</form>");
                    return;
                }

                String booked = "0".equals(r2.getString(7)) ? "booked" : "parked";
                String vtype = "0".equals(r2.getString(3)) ? "Bike" : "Car";
                out.println("You've " + booked + " at<br><hr> Spot_number : "
                        + SecurityUtil.escapeHtml(r2.getString(1)) + "<br>Park number : "
                        + SecurityUtil.escapeHtml(r2.getString(2)) + "<br>Book date : "
                        + SecurityUtil.escapeHtml(r2.getString(5)) + "<br>Book in time : "
                        + SecurityUtil.escapeHtml(r2.getString(6)) + "<br>Vehicle Type : " + vtype);
                String buttonText = "0".equals(r2.getString(7)) ? "Cancel Booking" : "Leave now!";
                out.println("<form method=\"post\" action=\"Leave_Customer\">"
                        + SecurityUtil.csrfHiddenInput(session)
                        + "<input type=\"submit\" name=\"park\" value=\"" + SecurityUtil.escapeHtml(buttonText)
                        + "\"></form>");
            }
        }
    }
}
