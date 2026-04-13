import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Final_Car_Book")
public class Final_Car_Book extends HttpServlet {
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
        String park = request.getParameter("parking");
        String snum = (String) session.getAttribute("park");
        String pnum = (String) session.getAttribute("park_num");

        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body><div id=\"text\">");

        try (Connection con = DbUtil.getConnection()) {
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            if (!isSpotAvailable(con, pnum, snum, 1)) {
                con.rollback();
                out.println("Spot already booked or parked. Please choose another spot.");
                out.println("</div></body></html>");
                return;
            }

            if ("park".equals(park)) {
                try (PreparedStatement s = con.prepareStatement(
                        "insert into parking_spot_info values(?,?,?,?,now(),curtime(),1)")) {
                    s.setInt(1, Integer.parseInt(snum));
                    s.setInt(2, Integer.parseInt(pnum));
                    s.setInt(3, 1);
                    s.setInt(4, Integer.parseInt(username));
                    s.executeUpdate();
                }
            } else {
                String date = request.getParameter("date");
                String hour = request.getParameter("hour");
                String dateValue;
                try {
                    // Parse for validation and normalize to ISO date persisted in DB.
                    dateValue = LocalDate.parse(date).toString();
                } catch (Exception ex) {
                    con.rollback();
                    out.println("Invalid booking date.");
                    out.println("</div></body></html>");
                    return;
                }
                try (PreparedStatement s = con.prepareStatement(
                        "insert into parking_spot_info values(?,?,?,?,?,?,0)")) {
                    s.setInt(1, Integer.parseInt(snum));
                    s.setInt(2, Integer.parseInt(pnum));
                    s.setInt(3, 1);
                    s.setInt(4, Integer.parseInt(username));
                    s.setString(5, dateValue);
                    s.setString(6, hour + ":00:00");
                    s.executeUpdate();
                }
            }
            con.commit();

            try (PreparedStatement details = con.prepareStatement("select * from parking_spot_info where cid=?")) {
                details.setInt(1, Integer.parseInt(username));
                try (ResultSet rs = details.executeQuery()) {
                    if (rs.next()) {
                        String booked = "0".equals(rs.getString(7)) ? "booked" : "parked";
                        out.println("You've " + booked + " at<br><hr> Spot_number : " + SecurityUtil.escapeHtml(rs.getString(1))
                                + "<br>Park number : " + SecurityUtil.escapeHtml(rs.getString(2)) + "<br>Book date : "
                                + SecurityUtil.escapeHtml(rs.getString(5)) + "<br>Book in time : "
                                + SecurityUtil.escapeHtml(rs.getString(6)) + "<br>Vehicle Type : Car");
                    }
                }
            }

            out.println("<br><hr>Login again when you want to leave the parking spot!");
            session.invalidate();
            out.println("<a href=\"Sign_in_customer.html\">Sign in</a>");
        } catch (Exception e) {
            getServletContext().log("Car booking failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Unable to complete booking right now.");
        }

        out.println("</div></body></html>");
    }

    private boolean isSpotAvailable(Connection con, String pnum, String snum, int vehicleType) throws SQLException {
        try (PreparedStatement check = con.prepareStatement(
                "select count(*) from parking_spot_info where park_num=? and spot_number=? and vehicle_type=?")) {
            check.setInt(1, Integer.parseInt(pnum));
            check.setInt(2, Integer.parseInt(snum));
            check.setInt(3, vehicleType);
            try (ResultSet rs = check.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }
}
