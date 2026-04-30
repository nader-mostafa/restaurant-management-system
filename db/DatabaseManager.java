package db;

// ده الملف المسؤول عن قاعدة البيانات (Database).
// قمنا بتنظيمه ليكون "Modular" بحيث يكون لكل جدول وظيفة إنشاء خاصة به لسهولة التعديل.

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:restaurant.db";

    /**
     * الحصول على اتصال بقاعدة البيانات
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
        }
        return DriverManager.getConnection(URL);
    }

    /**
     * تهيئة قاعدة البيانات وإنشاء الجداول إذا لم تكن موجودة
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            System.out.println("Connecting to database and initializing tables...");

            createUsersTable(stmt);
            createCustomersTable(stmt);
            createMealsTable(stmt);
            createOrdersTable(stmt);
            createOrderItemsTable(stmt);
            createOffersTable(stmt);
            createNotificationsTable(stmt);

            checkAndCreateDefaultAdmin(conn, stmt);

            System.out.println("Database initialization complete.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createUsersTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "name TEXT NOT NULL)");
    }

    private static void createCustomersTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "phone TEXT UNIQUE NOT NULL, " +
                "total_spent REAL DEFAULT 0, " +
                "loyalty_points INTEGER DEFAULT 0, " +
                "reward_tier TEXT DEFAULT 'Standard')");
    }

    private static void createMealsTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS meals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT, " +
                "price REAL NOT NULL, " +
                "stock INTEGER DEFAULT 0)");
    }

    private static void createOrdersTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employee_id INTEGER NOT NULL, " +
                "customer_id INTEGER, " +
                "total_amount REAL NOT NULL, " +
                "status TEXT DEFAULT 'Completed', " +
                "order_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (employee_id) REFERENCES users (id), " +
                "FOREIGN KEY (customer_id) REFERENCES customers (id))");
    }

    private static void createOrderItemsTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER NOT NULL, " +
                "meal_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "FOREIGN KEY (order_id) REFERENCES orders (id), " +
                "FOREIGN KEY (meal_id) REFERENCES meals (id))");
    }

    private static void createOffersTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS offers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "discount_percent REAL NOT NULL, " +
                "date_added DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    private static void createNotificationsTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "message TEXT NOT NULL, " +
                "is_read INTEGER DEFAULT 0, " +
                "date_added DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users (id))");
    }

    private static void checkAndCreateDefaultAdmin(Connection conn, Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next() && rs.getInt(1) == 0) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role, name) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin123");
                pstmt.setString(3, "admin");
                pstmt.setString(4, "System Administrator");
                pstmt.executeUpdate();
                System.out.println(">>> Created default Admin account: admin / admin123");
            }
        }
    }
}
