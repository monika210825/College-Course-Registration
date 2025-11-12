package Course_register;

import java.sql.*;
import java.util.*;
import java.io.*;

public class CourseRegistration {

    private static String url;
    private static String user;
    private static String pass;

    public static void main(String[] args) {
        loadDBConfig();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("\n=== COLLEGE COURSE REGISTRATION SYSTEM ===");
                System.out.println("1. Student Login");
                System.out.println("2. Student Register");
                System.out.println("3. Admin Login");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                case 1:
                    studentLogin(conn, sc);
                    break;
                case 2:
                    studentRegister(conn, sc);
                    break;
                case 3:
                    adminLogin(conn, sc);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load DB properties
    private static void loadDBConfig() {
        try (InputStream input = CourseRegistration.class.getResourceAsStream("/db.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            pass = prop.getProperty("db.pass");

        } catch (Exception e) {
            System.out.println("❌ Cannot load DB config file.");
        }
    }

    // Student Register
    private static void studentRegister(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n--- Student Register ---");
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Set Password: ");
        String pass = sc.nextLine();

        String sql = "INSERT INTO students (name, email, password) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, pass);

        ps.executeUpdate();
        System.out.println("✅ Registration Successful!\n");
    }

    // Student Login
    private static void studentLogin(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n--- Student Login ---");
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        String sql = "SELECT * FROM students WHERE email=? AND password=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, pass);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("\n✅ Login Successful! Welcome, " + rs.getString("name"));
            studentDashboard(conn, sc, rs.getInt("id"));
        } else {
            System.out.println("❌ Invalid credentials!\n");
        }
    }

    // Student Dashboard
    private static void studentDashboard(Connection conn, Scanner sc, int studentId) throws SQLException {
        while (true) {
            System.out.println("\n--- Student Dashboard ---");
            System.out.println("1. View Available Courses");
            System.out.println("2. Register a Course");
            System.out.println("3. View My Course");
            System.out.println("4. Logout");
            System.out.print("Choose: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
            case 1:
                viewCourses(conn);
                break;
            case 2:
                registerToCourse(conn, sc, studentId);
                break;
            case 3:
                showStudentCourse(conn, studentId);
                break;
            case 4:
                System.out.println("Logged out.");
                return;
            default:
                System.out.println("Invalid choice");
            }
        }
    }

    // View courses
    private static void viewCourses(Connection conn) throws SQLException {
        String sql = "SELECT * FROM courses";
        ResultSet rs = conn.prepareStatement(sql).executeQuery();

        System.out.println("\nAvailable Courses:");
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ". " + rs.getString("course_name"));
        }
    }

    // Register to course
    private static void registerToCourse(Connection conn, Scanner sc, int studentId) throws SQLException {
        viewCourses(conn);

        System.out.print("Enter Course ID to register: ");
        int courseId = sc.nextInt();
        sc.nextLine();

        String sql = "UPDATE students SET course = (SELECT course_name FROM courses WHERE id=?) WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, courseId);
        ps.setInt(2, studentId);
        ps.executeUpdate();

        System.out.println("✅ Course Registered Successfully!");
    }

    // View student's course
    private static void showStudentCourse(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT course FROM students WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            System.out.println("Your Course: " + rs.getString("course"));
        else
            System.out.println("No course registered.");
    }

    // Admin login
    private static void adminLogin(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Enter Username: ");
        String uname = sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        String sql = "SELECT * FROM admin WHERE username=? AND password=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, uname);
        ps.setString(2, pass);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("✅ Admin Logged In!");
            adminDashboard(conn, sc);
        } else {
            System.out.println("❌ Invalid credentials!");
        }
    }

    // Admin dashboard
    private static void adminDashboard(Connection conn, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\n--- Admin Dashboard ---");
            System.out.println("1. View All Students");
            System.out.println("2. Logout");
            System.out.print("Choose: ");
            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
            case 1:
                viewAllStudents(conn);
                break;
            case 2:
                System.out.println("Admin logged out.");
                return;
            default:
                System.out.println("Invalid choice");
            }
        }
    }

    // View all students
    private static void viewAllStudents(Connection conn) throws SQLException {
        String sql = "SELECT * FROM students";
        ResultSet rs = conn.prepareStatement(sql).executeQuery();

        System.out.println("\nRegistered Students:");
        System.out.printf("%-5s %-15s %-25s %-20s\n", "ID", "NAME", "EMAIL", "COURSE");

        while (rs.next()) {
            System.out.printf("%-5d %-15s %-25s %-20s\n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("course"));
        }
    }
}
