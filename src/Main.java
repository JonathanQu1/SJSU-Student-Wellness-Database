import java.sql.*;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== SJSU Student Wellness Console App ===");

        while (true) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> viewStudents();
                case 2 -> viewCounselors();
                case 3 -> viewAppointments();
                case 4 -> updateAppointmentStatus();
                case 5 -> transactionalSelfAssessmentAndReferral();
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // ---------------- MENU ----------------

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View all students");
        System.out.println("2. View all counselors");
        System.out.println("3. View all appointments");
        System.out.println("4. Update appointment status");
        System.out.println("5. Run transaction: add SelfAssessment + Referral");
        System.out.println("0. Exit");
    }

    // ---------------- INPUT HELPERS ----------------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value < min || value > max) {
                System.out.println("Value must be between " + min + " and " + max + ".");
            } else {
                return value;
            }
        }
    }

    // ---------------- OPTION 1: VIEW STUDENTS ----------------

    private static void viewStudents() {
        String sql = """
            SELECT s.StudentID,
                   p.Name,
                   p.ContactInfo,
                   s.Major,
                   s.Year
            FROM Student s
            JOIN Person p ON p.PersonID = s.PersonID
            ORDER BY s.StudentID
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nStudentID | Name             | Email                    | Major | Year");
            System.out.println("---------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("StudentID");
                String name = rs.getString("Name");
                String email = rs.getString("ContactInfo");
                String major = rs.getString("Major");
                String year = rs.getString("Year");

                System.out.printf("%9d | %-16s | %-23s | %-5s | %s%n",
                        id, name, email, major, year);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing students: " + e.getMessage());
        }
    }

    // ---------------- OPTION 2: VIEW COUNSELORS ----------------

    private static void viewCounselors() {
        String sql = """
            SELECT c.CounselorID,
                   p.Name,
                   p.ContactInfo,
                   c.Credentials,
                   c.Specializations,
                   c.Availability
            FROM Counselor c
            JOIN Person p ON p.PersonID = c.PersonID
            ORDER BY c.CounselorID
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nCounselorID | Name           | Email                    | Credentials | Specializations | Availability");
            System.out.println("---------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("CounselorID");
                String name = rs.getString("Name");
                String email = rs.getString("ContactInfo");
                String creds = rs.getString("Credentials");
                String specs = rs.getString("Specializations");
                String avail = rs.getString("Availability");

                System.out.printf("%11d | %-14s | %-23s | %-11s | %-16s | %s%n",
                        id, name, email, creds, specs, avail);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing counselors: " + e.getMessage());
        }
    }

    // ---------------- OPTION 3: VIEW APPOINTMENTS ----------------

    private static void viewAppointments() {
        String sql = """
            SELECT a.AppointmentID,
                   a.DateTime,
                   a.Status,
                   a.Mode,
                   s.StudentID,
                   sp.Name      AS StudentName,
                   c.CounselorID,
                   cp.Name      AS CounselorName
            FROM Appointment a
            JOIN Student   s  ON s.StudentID = a.StudentID
            JOIN Person    sp ON sp.PersonID = s.PersonID
            JOIN Counselor c  ON c.CounselorID = a.CounselorID
            JOIN Person    cp ON cp.PersonID = c.PersonID
            ORDER BY a.AppointmentID
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nApptID | DateTime           | Status    | Mode       | StudentID | StudentName     | CounselorID | CounselorName");
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int apptId = rs.getInt("AppointmentID");
                Timestamp dt = rs.getTimestamp("DateTime");
                String status = rs.getString("Status");
                String mode = rs.getString("Mode");
                int studentId = rs.getInt("StudentID");
                String studentName = rs.getString("StudentName");
                int counselorId = rs.getInt("CounselorID");
                String counselorName = rs.getString("CounselorName");

                System.out.printf("%6d | %-19s | %-9s | %-10s | %9d | %-15s | %11d | %s%n",
                        apptId, dt, status, mode, studentId, studentName, counselorId, counselorName);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing appointments: " + e.getMessage());
        }
    }

    // ---------------- OPTION 4: UPDATE APPOINTMENT STATUS ----------------

    private static void updateAppointmentStatus() {
        int apptId = readInt("Enter AppointmentID to update: ");

        System.out.println("Valid statuses: Scheduled, Completed, Cancelled, NoShow");
        String newStatus = readNonEmpty("Enter new status: ");

        String sql = "UPDATE Appointment SET Status = ? WHERE AppointmentID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, apptId);

            int rows = ps.executeUpdate();
            if (rows == 1) {
                System.out.println("Appointment updated successfully.");
            } else {
                System.out.println("No appointment found with that ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating appointment: " + e.getMessage());
        }
    }

    // ---------------- OPTION 5: TRANSACTION (SelfAssessment + Referral) ----------------
    //
    // This demonstrates:
    // - setAutoCommit(false)
    // - INSERT into SelfAssessment
    // - INSERT into Referral using the new AssessmentID
    // - COMMIT on success, ROLLBACK on failure
    //
    // Tables touched in one transaction: SelfAssessment, Referral

    private static void transactionalSelfAssessmentAndReferral() {
        System.out.println("\n=== New Self-Assessment + Referral (Transactional) ===");

        int studentId = readInt("Enter StudentID: ");
        int counselorId = readInt("Enter CounselorID: ");
        String date = readNonEmpty("Enter assessment date (YYYY-MM-DD): ");

        int anxiety = readIntInRange("Anxiety score (0-10): ", 0, 10);
        int depression = readIntInRange("Depression score (0-10): ", 0, 10);
        int stress = readIntInRange("Stress score (0-10): ", 0, 10);

        // For ReferralDate we can reuse the same date string
        String referralStatus = "Pending";

        String insertAssessmentSql = """
            INSERT INTO SelfAssessment (StudentID, Date, AnxietyScore, DepressionScore, StressScore)
            VALUES (?, ?, ?, ?, ?)
            """;

        String insertReferralSql = """
            INSERT INTO Referral (AssessmentID, CounselorID, ReferralDate, Status)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // start transaction

            try (PreparedStatement psAssess = conn.prepareStatement(
                    insertAssessmentSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psRef = conn.prepareStatement(insertReferralSql)) {

                // Insert into SelfAssessment
                psAssess.setInt(1, studentId);
                psAssess.setString(2, date);       // MySQL can parse 'YYYY-MM-DD'
                psAssess.setInt(3, anxiety);
                psAssess.setInt(4, depression);
                psAssess.setInt(5, stress);

                int rows1 = psAssess.executeUpdate();
                if (rows1 != 1) {
                    throw new SQLException("SelfAssessment insert failed (no row inserted).");
                }

                // Get generated AssessmentID
                int assessmentId;
                try (ResultSet keys = psAssess.getGeneratedKeys()) {
                    if (keys.next()) {
                        assessmentId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated AssessmentID.");
                    }
                }

                // Insert into Referral using the new AssessmentID
                psRef.setInt(1, assessmentId);
                psRef.setInt(2, counselorId);
                psRef.setString(3, date);
                psRef.setString(4, referralStatus);

                int rows2 = psRef.executeUpdate();
                if (rows2 != 1) {
                    throw new SQLException("Referral insert failed (no row inserted).");
                }

                // Everything succeeded → COMMIT
                conn.commit();
                System.out.println("✅ Transaction committed: SelfAssessment and Referral created.");
                System.out.println("   New AssessmentID = " + assessmentId);

            } catch (SQLException e) {
                // Something failed → ROLLBACK
                conn.rollback();
                System.out.println("❌ Transaction rolled back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error during transaction: " + e.getMessage());
        }
    }
}
