import java.io.IOException;
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

@WebServlet("/Sign_up_cust_servlet")
public class Sign_up_cust_servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/Sign_up_customer.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        String name = request.getParameter("cust_name");
        String vehicleNumber = request.getParameter("V_number");
        String vehicleType = request.getParameter("V_type");
        String customerPassword = request.getParameter("cust_pwd");

        try {
            int vtype = Integer.parseInt(vehicleType);
            if (vtype != 0 && vtype != 1) {
                response.sendRedirect("Sign_up_customer.html?error=invalid-vehicle-type");
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

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("Sign_up_customer.html?success=account-created#customerId="
                    + URLEncoder.encode(String.valueOf(customerId), StandardCharsets.UTF_8.name()));
            return;
        } catch (Exception e) {
            getServletContext().log("Sign up failed", e);
            response.sendRedirect("Sign_up_customer.html?error="
                    + URLEncoder.encode("server", StandardCharsets.UTF_8.name()));
            return;
        }
    }
}
