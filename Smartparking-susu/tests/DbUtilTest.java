import java.sql.SQLException;

public class DbUtilTest {

    public static void main(String[] args) {
        testGetConnectionFailsWithoutPassword();
        System.out.println("DbUtilTest: all tests passed");
    }

    private static void testGetConnectionFailsWithoutPassword() {
        try {
            DbUtil.getConnection();
            throw new AssertionError("Expected DbUtil.getConnection to throw SQLException");
        } catch (SQLException e) {
            String envPassword = System.getenv("DB_PASSWORD");
            if (envPassword == null || envPassword.trim().isEmpty()) {
                assertTrue(e.getMessage() != null && e.getMessage().contains("DB_PASSWORD"),
                        "expected missing password message");
            }
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
