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

@WebServlet("/Sign_up_cust_servlet")
public class Sign_up_cust_servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("cust_name");
        String vehicleNumber = request.getParameter("V_number");
        String vehicleType = request.getParameter("V_type");
        String customerPassword = request.getParameter("cust_pwd");

        out.print("<!DOCTYPE html><html><head><title>that's my spot.com</title></head><body>");

        try {
            int vtype = Integer.parseInt(vehicleType);
            if (vtype != 0 && vtype != 1) {
                response.sendRedirect("Sign_up_customer.html");
                return;
            }

            String hashedPassword = PasswordUtil.hash(customerPassword);
            int customerId = -1;

            try (Connection con = DbUtil.getConnection();
                    PreparedStatement insert = con.prepareStatement(
                            "insert into customer_info values(null,?,?,?,?)");
                    PreparedStatement readId = con.prepareStatement("select max(customer_id) from customer_info")) {
                insert.setString(1, name);
                insert.setString(2, vehicleNumber);
                insert.setInt(3, vtype);
                insert.setString(4, hashedPassword);
                insert.executeUpdate();

                try (ResultSet rs = readId.executeQuery()) {
                    if (rs.next()) {
                        customerId = rs.getInt(1);
                    }
                }
            }

            String safeName = SecurityUtil.escapeHtml(name);
            out.println("<div id=\"text\">");
            out.println(safeName + " added! Your customer ID is <span style=\"color:blue;font-size:30px;font-weight:bold\">"
                    + customerId
                    + " </span>.Sign in with this customer ID as username and the entered password to login !<br><br><a style=\"color: orange;\" href=\"Sign_in_customer.html\">Login</a>");
            out.println("</div>");

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        } catch (Exception e) {
            getServletContext().log("Sign up failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Unable to sign up right now. Please try again later.");
        }
        out.println("</body></html>");
    }
}
