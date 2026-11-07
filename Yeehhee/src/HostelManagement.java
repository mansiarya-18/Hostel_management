
import java.sql.*;
import java.util.Scanner;

public class HostelManagement {

    static final String URL =
        "jdbc:mysql://localhost:3306/hostel_db";
    static final String USER = "root";
    static final String PASS = "Mansi@18";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Database Connected!");

            int choice;

            do {
                System.out.println("\n--- HOSTEL MANAGEMENT ---");
                System.out.println("1. Insert 3 Students");
                System.out.println("2. Retrieve Students by Hostel");
                System.out.println("3. Update Hostel Fee");
                System.out.println("4. Search and Update Room");
                System.out.println("5. Total Fee and Delete Student");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");
                choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        insertStudents(con);
                        break;

                    case 2:
                        retrieveStudents(con, sc);
                        break;

                    case 3:
                        updateFee(con, sc);
                        break;

                    case 4:
                        updateRoom(con, sc);
                        break;

                    case 5:
                        totalFeeAndDelete(con, sc);
                        break;

                    case 6:
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

            con.close();

        } catch (Exception e) {
            System.out.println(e);
        }

        sc.close();
    }

    // (a) INSERT THREE STUDENTS
    static void insertStudents(Connection con) throws SQLException {

        String sql = "INSERT INTO HostelStudent VALUES(?,?,?,?,?,?)";

        PreparedStatement ps = con.prepareStatement(sql);

        Object[][] students = {
            {101, "Aman", "101CSE", "Boys Hostel", "A101", 60000.0},
            {102, "Riya", "102CSE", "Girls Hostel", "G102", 65000.0},
            {103, "Rahul", "103CSE", "Boys Hostel", "A103", 45000.0}
        };

        for (Object[] s : students) {
            try {
                ps.setInt(1, (Integer) s[0]);
                ps.setString(2, (String) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setString(4, (String) s[3]);
                ps.setString(5, (String) s[4]);
                ps.setDouble(6, (Double) s[5]);

                ps.executeUpdate();
                System.out.println("Student inserted: " + s[0]);

            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Duplicate student ID: " + s[0]);
            }
        }

        ps.close();
    }

    // (b) RETRIEVE STUDENTS BY HOSTEL AND FEE
    static void retrieveStudents(Connection con, Scanner sc)
            throws SQLException {

        System.out.print("Enter hostel name: ");
        sc.nextLine();
        String hostel = sc.nextLine();

        String sql = "SELECT * FROM HostelStudent "
                   + "WHERE hostel_name=? AND fee>50000 "
                   + "ORDER BY fee DESC";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, hostel);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.println(
                rs.getInt("student_id") + " " +
                rs.getString("student_name") + " " +
                rs.getString("hostel_name") + " " +
                rs.getString("room_number") + " " +
                rs.getDouble("fee")
            );
        }

        rs.close();
        ps.close();
    }

    // (c) UPDATE HOSTEL FEE
    static void updateFee(Connection con, Scanner sc)
            throws SQLException {

        System.out.print("Enter student ID: ");
        int id = sc.nextInt();

        System.out.print("Enter new fee: ");
        double fee = sc.nextDouble();

        String sql = "UPDATE HostelStudent SET fee=? WHERE student_id=?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setDouble(1, fee);
        ps.setInt(2, id);

        int rows = ps.executeUpdate();

        if (rows > 0)
            System.out.println("Fee updated successfully!");
        else
            System.out.println("Student does not exist!");

        ps.close();
    }

    // (d) SEARCH STUDENT AND UPDATE ROOM
    static void updateRoom(Connection con, Scanner sc)
            throws SQLException {

        System.out.print("Enter student ID: ");
        int id = sc.nextInt();

        String search = "SELECT * FROM HostelStudent WHERE student_id=?";

        PreparedStatement ps = con.prepareStatement(search);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("Student Name: " +
                               rs.getString("student_name"));
            System.out.println("Old Room: " +
                               rs.getString("room_number"));

            System.out.print("Enter new room number: ");
            sc.nextLine();
            String room = sc.nextLine();

            String update = "UPDATE HostelStudent "
                         + "SET room_number=? WHERE student_id=?";

            PreparedStatement ps2 = con.prepareStatement(update);
            ps2.setString(1, room);
            ps2.setInt(2, id);
            ps2.executeUpdate();

            System.out.println("Updated Student Details:");

            String show = "SELECT * FROM HostelStudent WHERE student_id=?";
            PreparedStatement ps3 = con.prepareStatement(show);
            ps3.setInt(1, id);

            ResultSet rs2 = ps3.executeQuery();

            if (rs2.next()) {
                System.out.println(
                    rs2.getInt("student_id") + " " +
                    rs2.getString("student_name") + " " +
                    rs2.getString("roll_number") + " " +
                    rs2.getString("hostel_name") + " " +
                    rs2.getString("room_number") + " " +
                    rs2.getDouble("fee")
                );
            }

            rs2.close();
            ps3.close();
            ps2.close();

        } else {
            System.out.println("Student not found!");
        }

        rs.close();
        ps.close();
    }

    // (e) SUM FEE AND CONDITIONAL DELETE
    static void totalFeeAndDelete(Connection con, Scanner sc)
            throws SQLException {

        String sum = "SELECT SUM(fee) FROM HostelStudent";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sum);

        if (rs.next())
            System.out.println("Total Fee Collected: " +
                               rs.getDouble(1));

        System.out.print("Enter student ID to delete: ");
        int id = sc.nextInt();

        String check = "SELECT fee FROM HostelStudent WHERE student_id=?";

        PreparedStatement ps = con.prepareStatement(check);
        ps.setInt(1, id);

        ResultSet rs2 = ps.executeQuery();

        if (rs2.next()) {

            double fee = rs2.getDouble("fee");

            if (fee > 0) {

                String delete = "DELETE FROM HostelStudent "
                             + "WHERE student_id=?";

                PreparedStatement ps2 = con.prepareStatement(delete);
                ps2.setInt(1, id);
                ps2.executeUpdate();

                System.out.println("Student deleted successfully!");
                ps2.close();

            } else {
                System.out.println("Fee not fully paid!");
            }

        } else {
            System.out.println("Student not found!");
        }

        rs2.close();
        ps.close();
        rs.close();
        st.close();
    }
}
