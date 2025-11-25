import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final Properties props = new Properties();

    static {
        try (InputStream input =
                     DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Cannot find db.properties in resources folder.");
            }
            props.load(input);

            // Load MySQL driver (optional in newer JDBC, but safe)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}
