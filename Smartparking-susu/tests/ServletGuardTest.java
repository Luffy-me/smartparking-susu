import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ServletGuardTest {
    public static void main(String[] args) throws Exception {
        testSignInGetRedirectsToContextPath();
        testSignUpGetRedirectsToContextPath();
        testSignUpRejectsInvalidVehicleType();
        testSelectSpotCarRedirectsWithoutSession();
        testSelectSpotBikeRedirectsWithoutSession();
        testLocationsSpotInfoRedirectsWithoutSession();
        testParkingSpotInfoRedirectsWithoutSession();
        testFinalCarBookRejectsInvalidCsrf();
        testFinalBikeBookRejectsInvalidCsrf();
        testLeaveCustomerRejectsInvalidCsrf();
        testLogoutRejectsInvalidCsrf();
        System.out.println("ServletGuardTest: all tests passed");
    }

    private static void testSignInGetRedirectsToContextPath() throws Exception {
        Sign_in_Customer servlet = new Sign_in_Customer();
        MockRequest request = new MockRequest();
        request.setContextPath("/app");
        MockResponse response = new MockResponse();
        servlet.doGet(request, response);
        assertEquals("/app/Sign_in_customer.html", response.getRedirect(), "sign-in GET should redirect to context path");
    }

    private static void testSignUpGetRedirectsToContextPath() throws Exception {
        Sign_up_cust_servlet servlet = new Sign_up_cust_servlet();
        MockRequest request = new MockRequest();
        request.setContextPath("/portal");
        MockResponse response = new MockResponse();
        servlet.doGet(request, response);
        assertEquals("/portal/Sign_up_customer.html", response.getRedirect(), "sign-up GET should redirect to context path");
    }

    private static void testSignUpRejectsInvalidVehicleType() throws Exception {
        Sign_up_cust_servlet servlet = new Sign_up_cust_servlet();
        MockRequest request = new MockRequest();
        request.setParameter("cust_name", "Casey");
        request.setParameter("V_number", "ABC-123");
        request.setParameter("V_type", "2");
        request.setParameter("cust_pwd", "password");
        MockResponse response = new MockResponse();
        servlet.doPost(request, response);
        assertEquals("Sign_up_customer.html?error=invalid-vehicle-type", response.getRedirect(),
                "invalid vehicle type should redirect with error");
    }

    private static void testSelectSpotCarRedirectsWithoutSession() throws Exception {
        Select_spot_car servlet = new Select_spot_car();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        servlet.doGet(request, response);
        assertEquals("Sign_in_customer.html", response.getRedirect(), "car selection without session should redirect");
    }

    private static void testSelectSpotBikeRedirectsWithoutSession() throws Exception {
        Select_spot_bike servlet = new Select_spot_bike();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        servlet.doGet(request, response);
        assertEquals("Sign_in_customer.html", response.getRedirect(), "bike selection without session should redirect");
    }

    private static void testLocationsSpotInfoRedirectsWithoutSession() throws Exception {
        Locations_spot_info servlet = new Locations_spot_info();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        servlet.doGet(request, response);
        assertEquals("Sign_in_customer.html", response.getRedirect(), "locations without session should redirect");
    }

    private static void testParkingSpotInfoRedirectsWithoutSession() throws Exception {
        Parking_spot_info servlet = new Parking_spot_info();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        servlet.doGet(request, response);
        assertEquals("Sign_in_customer.html", response.getRedirect(), "parking spot info without session should redirect");
    }

    private static void testFinalCarBookRejectsInvalidCsrf() throws Exception {
        Final_Car_Book servlet = new Final_Car_Book();
        MockSession session = new MockSession();
        session.setAttribute("username", "7");
        session.setAttribute("csrfToken", "expected");
        MockRequest request = new MockRequest();
        request.setSession(session);
        request.setParameter("csrfToken", "wrong");
        MockResponse response = new MockResponse();
        servlet.doPost(request, response);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus(), "car booking should reject invalid csrf");
        assertContains(response.getBody(), "Invalid request.", "car booking should return invalid request message");
    }

    private static void testFinalBikeBookRejectsInvalidCsrf() throws Exception {
        Final_Bike_Book servlet = new Final_Bike_Book();
        MockSession session = new MockSession();
        session.setAttribute("username", "8");
        session.setAttribute("csrfToken", "expected");
        MockRequest request = new MockRequest();
        request.setSession(session);
        request.setParameter("csrfToken", "wrong");
        MockResponse response = new MockResponse();
        servlet.doPost(request, response);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus(), "bike booking should reject invalid csrf");
        assertContains(response.getBody(), "Invalid request.", "bike booking should return invalid request message");
    }

    private static void testLeaveCustomerRejectsInvalidCsrf() throws Exception {
        Leave_Customer servlet = new Leave_Customer();
        MockSession session = new MockSession();
        session.setAttribute("username", "9");
        session.setAttribute("csrfToken", "expected");
        MockRequest request = new MockRequest();
        request.setSession(session);
        request.setParameter("csrfToken", "wrong");
        MockResponse response = new MockResponse();
        servlet.doPost(request, response);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus(), "leave should reject invalid csrf");
        assertContains(response.getBody(), "Invalid request.", "leave should return invalid request message");
    }

    private static void testLogoutRejectsInvalidCsrf() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        servlet.doPost(request, response);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus(), "logout should reject invalid csrf");
        assertContains(response.getBody(), "Invalid request.", "logout should return invalid request message");
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    private static void assertContains(String actual, String expected, String message) {
        if (actual == null || !actual.contains(expected)) {
            throw new AssertionError(message + " (expected to contain=" + expected + ", actual=" + actual + ")");
        }
    }

    private static final class MockSession implements HttpSession {
        private final Map<String, Object> attributes = new HashMap<>();

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
        }

        @Override
        public void invalidate() {
        }
    }

    private static final class MockRequest implements HttpServletRequest {
        private final Map<String, String> parameters = new HashMap<>();
        private HttpSession session;
        private String contextPath = "";

        void setParameter(String name, String value) {
            parameters.put(name, value);
        }

        void setSession(HttpSession session) {
            this.session = session;
        }

        void setContextPath(String contextPath) {
            this.contextPath = contextPath == null ? "" : contextPath;
        }

        @Override
        public String getParameter(String name) {
            return parameters.get(name);
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (session == null && create) {
                session = new MockSession();
            }
            return session;
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }
    }

    private static final class MockResponse implements HttpServletResponse {
        private final StringWriter body = new StringWriter();
        private final PrintWriter writer = new PrintWriter(body);
        private String redirect;
        private int status = 200;
        private boolean committed;

        @Override
        public void sendRedirect(String location) {
            this.redirect = location;
            this.committed = true;
        }

        @Override
        public void setContentType(String type) {
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public boolean isCommitted() {
            return committed;
        }

        String getRedirect() {
            return redirect;
        }

        int getStatus() {
            return status;
        }

        String getBody() {
            writer.flush();
            return body.toString();
        }
    }
}
