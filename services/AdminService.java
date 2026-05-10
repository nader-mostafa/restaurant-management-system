package services;

import db.DatabaseManager;
import models.Models.Employee;
import models.Models.Meal;
import models.Models.Order;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    
    public static boolean addEmployee(String username, String password, String name) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password, role, name) VALUES (?, ?, 'employee', ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteEmployee(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ? AND role = 'employee'")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateEmployee(int id, String username, String name, String password) {
        String query = (password != null && !password.trim().isEmpty())
            ? "UPDATE users SET username = ?, name = ?, password = ? WHERE id = ? AND role = 'employee'"
            : "UPDATE users SET username = ?, name = ? WHERE id = ? AND role = 'employee'";
            
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, name);
            if (password != null && !password.trim().isEmpty()) {
                pstmt.setString(3, password);
                pstmt.setInt(4, id);
            } else {
                pstmt.setInt(3, id);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Employee> searchEmployees(String keyword) {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, username, password, name FROM users WHERE role = 'employee' AND (name LIKE ? OR username LIKE ?)")) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Employee(rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, password, name FROM users WHERE role = 'employee'")) {
            while (rs.next()) {
                list.add(new Employee(rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean addMeal(String name, String category, double price) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO meals (name, category, price) VALUES (?, ?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Meal> getAllMeals() {
        List<Meal> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, category, price FROM meals")) {
            while (rs.next()) {
                list.add(new Meal(rs.getInt("id"), rs.getString("name"), rs.getString("category"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean deleteMeal(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM meals WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateMeal(int id, String name, String category, double price) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE meals SET name = ?, category = ?, price = ? WHERE id = ?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Meal> searchMeals(String keyword) {
        List<Meal> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, name, category, price FROM meals WHERE name LIKE ? OR category LIKE ?")) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Meal(rs.getInt("id"), rs.getString("name"), rs.getString("category"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean addOffer(String name, String description, double discountPercent) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO offers (name, description, discount_percent) VALUES (?, ?, ?)")) {
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setDouble(3, discountPercent);
                pstmt.executeUpdate();
            }
            
            String msg = "New Offer: " + name + " - " + discountPercent + "% off!";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE role = 'employee'")) {
                try (PreparedStatement notifyStmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
                    while (rs.next()) {
                        notifyStmt.setInt(1, rs.getInt("id"));
                        notifyStmt.setString(2, msg);
                        notifyStmt.executeUpdate();
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean generateCustomerReport(String filepath) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone, total_spent, loyalty_points, reward_tier FROM customers");
             BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
             
            writer.write("ID,Name,Phone,Total Spent,Loyalty Points,Reward Tier\n");
            while(rs.next()) {
                writer.write(String.format("%d,%s,%s,%.2f,%d,%s\n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getDouble("total_spent"),
                    rs.getInt("loyalty_points"),
                    rs.getString("reward_tier")
                ));
            }
            return true;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean generateEmployeeReport(String filepath) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, name FROM users WHERE role = 'employee'");
             BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
             
            writer.write("ID,Username,Name\n");
            while(rs.next()) {
                writer.write(String.format("%d,%s,%s\n",
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("name")
                ));
            }
            return true;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, employee_id, customer_id, total_amount, status, order_date FROM orders ORDER BY order_date DESC")) {
            while (rs.next()) {
                list.add(new Order(
                    rs.getInt("id"),
                    rs.getInt("employee_id"),
                    rs.getInt("customer_id"),
                    rs.getDouble("total_amount"),
                    rs.getString("status"),
                    rs.getTimestamp("order_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
