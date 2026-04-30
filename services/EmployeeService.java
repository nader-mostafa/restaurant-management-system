package services;

import db.DatabaseManager;
import models.Models.Customer;
import models.Models.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {

    public static boolean addCustomer(String name, String phone) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO customers (name, phone) VALUES (?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone, total_spent, loyalty_points, reward_tier FROM customers")) {
            while (rs.next()) {
                list.add(new Customer(
                    rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
                    rs.getDouble("total_spent"), rs.getInt("loyalty_points"), rs.getString("reward_tier")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean registerCustomerLoyalty(int customerId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET loyalty_points = 100, reward_tier = 'Silver' WHERE id = ?")) {
            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createOrder(int employeeId, int customerId, double totalAmount, List<Integer> mealIds, List<Integer> quantities) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            int orderId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO orders (employee_id, customer_id, total_amount) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, employeeId);
                if (customerId > 0) pstmt.setInt(2, customerId);
                else pstmt.setNull(2, java.sql.Types.INTEGER);
                pstmt.setDouble(3, totalAmount);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) orderId = rs.getInt(1);
            }

            if (orderId == -1) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO order_items (order_id, meal_id, quantity, price) VALUES (?, ?, ?, ?)");
                 PreparedStatement priceStmt = conn.prepareStatement("SELECT price FROM meals WHERE id = ?")) {
                for (int i = 0; i < mealIds.size(); i++) {
                    int mealId = mealIds.get(i);
                    int qty = quantities.get(i);
                    
                    double actualPrice = 0.0;
                    priceStmt.setInt(1, mealId);
                    ResultSet priceRs = priceStmt.executeQuery();
                    if (priceRs.next()) {
                        actualPrice = priceRs.getDouble("price");
                    }
                    
                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, mealId);
                    pstmt.setInt(3, qty);
                    pstmt.setDouble(4, actualPrice);
                    pstmt.executeUpdate();
                }
            }

            if (customerId > 0) {
                double previousSpent = 0;
                String custName = "";
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT total_spent, name FROM customers WHERE id = ?")) {
                    pstmt.setInt(1, customerId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        previousSpent = rs.getDouble("total_spent");
                        custName = rs.getString("name");
                    }
                }

                double newTotal = previousSpent + totalAmount;
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET total_spent = ? WHERE id = ?")) {
                    pstmt.setDouble(1, newTotal);
                    pstmt.setInt(2, customerId);
                    pstmt.executeUpdate();
                }

                if (previousSpent < 500 && newTotal >= 500) {
                    try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
                        pstmt.setInt(1, employeeId);
                        pstmt.setString(2, "Gift unlocked for customer: " + custName + "! (Reached $500 milestone)");
                        pstmt.executeUpdate();
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

    public static List<String> getUnreadNotifications(int userId) {
        List<String> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, message FROM notifications WHERE user_id = ? AND is_read = 0")) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            List<Integer> idsToUpdate = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("message"));
                idsToUpdate.add(rs.getInt("id"));
            }
            
            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE notifications SET is_read = 1 WHERE id = ?")) {
                for (int id : idsToUpdate) {
                    updateStmt.setInt(1, id);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean updateCustomer(int id, String name, String phone) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET name = ?, phone = ? WHERE id = ?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteCustomer(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Customer> searchCustomers(String keyword) {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, name, phone, total_spent, loyalty_points, reward_tier FROM customers WHERE name LIKE ? OR phone LIKE ?")) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Customer(
                    rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
                    rs.getDouble("total_spent"), rs.getInt("loyalty_points"), rs.getString("reward_tier")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean cancelOrder(int orderId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double amount = 0;
                int customerId = 0;
                String status = "";
                try (PreparedStatement sel = conn.prepareStatement("SELECT customer_id, total_amount, status FROM orders WHERE id = ?")) {
                    sel.setInt(1, orderId);
                    ResultSet rs = sel.executeQuery();
                    if (rs.next()) {
                        customerId = rs.getInt("customer_id");
                        amount = rs.getDouble("total_amount");
                        status = rs.getString("status");
                    }
                }

                if ("Cancelled".equals(status)) return false;

                try (PreparedStatement upd = conn.prepareStatement("UPDATE orders SET status = 'Cancelled' WHERE id = ?")) {
                    upd.setInt(1, orderId);
                    upd.executeUpdate();
                }

                if (customerId > 0) {
                    try (PreparedStatement updCust = conn.prepareStatement(
                            "UPDATE customers SET total_spent = total_spent - ? WHERE id = ?")) {
                        updCust.setDouble(1, amount);
                        updCust.setInt(2, customerId);
                        updCust.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Order> getCustomerOrders(int customerId) {
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT id, employee_id, customer_id, total_amount, status, order_date FROM orders WHERE customer_id = ? ORDER BY order_date DESC")) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Order(rs.getInt("id"), rs.getInt("employee_id"), rs.getInt("customer_id"),
                        rs.getDouble("total_amount"), rs.getString("status"), rs.getTimestamp("order_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
