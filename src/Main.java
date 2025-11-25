import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting app...");

        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("✅ Connected to database!");
            System.out.println("Current catalog (DB): " + conn.getCatalog());
        } catch (SQLException e) {
            System.out.println("❌ Failed to connect to database.");
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace(); // optional, for debugging
        }
    }
}
