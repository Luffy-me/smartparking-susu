import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/Parking_spot_info")
public class Parking_spot_info extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("Sign_in_customer.html");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        int pnum = Integer.parseInt(request.getParameter("action"));
        session.setAttribute("park_num", String.valueOf(pnum));

        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title>"
                + "<style>.booked{ background-color:orange; }.parked{ background-color:red; }.notbooked{ background-color:green; }</style>"
                + "</head><body>");

        try (Connection con = DbUtil.getConnection();
                PreparedStatement lotPs = con.prepareStatement("select * from parking_lot_info where park_number=?");
                PreparedStatement carBusyPs = con.prepareStatement(
                        "select spot_number,status from parking_spot_info where park_num=? and vehicle_type=1 order by spot_number");
                PreparedStatement bikeBusyPs = con.prepareStatement(
                        "select spot_number,status from parking_spot_info where park_num=? and vehicle_type=0 order by spot_number")) {
            lotPs.setInt(1, pnum);
            String[] psi = new String[10];
            int carCap = 0;
            int bikeCap = 0;
            try (ResultSet r = lotPs.executeQuery()) {
                if (r.next()) {
                    for (int i = 1; i <= 9; i++) {
                        psi[i] = r.getString(i);
                    }
                    carCap = Integer.parseInt(psi[4]);
                    bikeCap = Integer.parseInt(psi[5]);
                }
            }

            out.println("<div>Car Parkings:</div>");
            out.println("<form action=\"Select_spot_car\" method=\"get\">");
            renderSpotButtons(out, carBusyPs, pnum, carCap);
            out.println("</form>");

            out.println("<div>Bike Parkings:</div>");
            out.println("<form action=\"Select_spot_bike\" method=\"get\">");
            renderSpotButtons(out, bikeBusyPs, pnum, bikeCap);
            out.println("</form>");
        } catch (Exception e) {
            getServletContext().log("Unable to load parking spots", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Unable to load parking spots.");
        }

        out.println("</body></html>");
    }

    private void renderSpotButtons(PrintWriter out, PreparedStatement ps, int pnum, int cap) throws Exception {
        Set<Integer> booked = new HashSet<Integer>();
        Set<Integer> parked = new HashSet<Integer>();
        ps.setInt(1, pnum);
        try (ResultSet r2 = ps.executeQuery()) {
            while (r2.next()) {
                int spot = r2.getInt(1);
                if ("0".equals(r2.getString(2))) {
                    booked.add(spot);
                } else {
                    parked.add(spot);
                }
            }
        }

        for (int i = 1; i <= cap; i++) {
            if (booked.contains(i)) {
                out.println("<button class=\"booked\" type=\"submit\" name=\"park\" value=\"" + i + "\" disabled>" + i
                        + "</button>");
            } else if (parked.contains(i)) {
                out.println("<button class=\"parked\" type=\"submit\" name=\"park\" value=\"" + i + "\" disabled>" + i
                        + "</button>");
            } else {
                out.println("<button class=\"notbooked\" type=\"submit\" name=\"park\" value=\"" + i + "\">" + i
                        + "</button>");
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
