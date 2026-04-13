import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Leave_Customer")
public class Leave_Customer extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("Sign_in_customer.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (session == null || session.getAttribute("username") == null || !SecurityUtil.isValidCsrf(request, session)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("Invalid request.");
            return;
        }

        String username = (String) session.getAttribute("username");
        String parkAction = request.getParameter("park");

        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body>");

        try (Connection con = DbUtil.getConnection()) {
            con.setAutoCommit(false);

            String pnum = null;
            String snum = null;
            String bookDate = null;
            String bookInTime = null;
            String vtype = null;

            try (PreparedStatement find = con.prepareStatement(
                    "select park_num,spot_number,book_date,book_in_time,vehicle_type from parking_spot_info where cid=?");
                    PreparedStatement delete = con.prepareStatement("delete from parking_spot_info where cid=?")) {
                find.setInt(1, Integer.parseInt(username));
                try (ResultSet r = find.executeQuery()) {
                    if (r.next()) {
                        pnum = r.getString(1);
                        snum = r.getString(2);
                        bookDate = r.getString(3);
                        bookInTime = r.getString(4);
                        vtype = r.getString(5);
                    }
                }

                delete.setInt(1, Integer.parseInt(username));
                delete.executeUpdate();
            }

            if (pnum == null || snum == null) {
                con.rollback();
                out.println("No active booking found.");
                out.println("</body></html>");
                return;
            }

            if ("Cancel Booking".equals(parkAction)) {
                con.commit();
                out.println("<h3>" + SecurityUtil.escapeHtml(snum) + " is now empty for park number "
                        + SecurityUtil.escapeHtml(pnum) + ". Your booking has been successfully cancelled!</h3>");
                out.println("<h3><a href=\"Sign_in_customer.html\">Sign In</a></h3>");
                out.println("</body></html>");
                return;
            }

            float cost = resolveCost(con, pnum, vtype);
            LocalDate bookedDate = LocalDate.parse(bookDate);
            LocalTime bookedTime = LocalTime.parse(bookInTime);
            LocalDateTime bookedAt = LocalDateTime.of(bookedDate, bookedTime);
            long hours = Math.max(0, Duration.between(bookedAt, LocalDateTime.now()).toHours());
            float bill = hours * cost;

            con.commit();
            out.println("<h3>You've to now pay " + bill + "<br><hr></h3>");
            out.println("<h3>" + SecurityUtil.escapeHtml(snum) + " is now empty for park number "
                    + SecurityUtil.escapeHtml(pnum) + "<br><hr></h3>");
            out.println("<h3><a href=\"Sign_in_customer.html\">Sign In</a></h3>");
        } catch (Exception e) {
            getServletContext().log("Leave customer failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Unable to complete leave operation right now.");
        }

        out.println("</body></html>");
    }

    private float resolveCost(Connection con, String pnum, String vtype) throws Exception {
        String query = "1".equals(vtype) ? "select cost_of_car_parkings from parking_lot_info where park_number=?"
                : "select cost_of_bike_parkings from parking_lot_info where park_number=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, Integer.parseInt(pnum));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Float.parseFloat(rs.getString(1));
                }
            }
        }
        return 0f;
    }
}
