import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final Properties props = new Properties();

    static {
        try (InputStream input =
                     DBUtil.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new RuntimeException("Cannot find app.properties in resources folder.");
            }
            props.load(input);

            // Load MySQL driver (optional in newer JDBC, but safe)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("app.url");
        String user = props.getProperty("app.user");
        String password = props.getProperty("app.password");

        return DriverManager.getConnection(url, user, password);
    }
}
