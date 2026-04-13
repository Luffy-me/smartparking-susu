import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbUtil {
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/parking_system_db";
    private static final String DEFAULT_DB_USER = "parking_app";
    private static final String DEFAULT_DB_PASSWORD = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private DbUtil() {
    }

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = getEnv("DB_URL", DEFAULT_DB_URL);
        String user = getEnv("DB_USER", DEFAULT_DB_USER);
        String password = getEnv("DB_PASSWORD", DEFAULT_DB_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }
}
