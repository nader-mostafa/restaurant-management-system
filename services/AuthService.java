package services;

import db.DatabaseManager;
import models.Models.Admin;
import models.Models.Employee;
import models.Models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    public static User login(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, username, password, role, name FROM users WHERE username = ? AND password = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String name = rs.getString("name");
                if ("admin".equals(role)) {
                    return new Admin(id, username, password, name);
                } else if ("employee".equals(role)) {
                    return new Employee(id, username, password, name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateUserInfo(int userId, String newName, String newPassword) {
        String query = newPassword != null && !newPassword.isEmpty() 
            ? "UPDATE users SET name = ?, password = ? WHERE id = ?"
            : "UPDATE users SET name = ? WHERE id = ?";
            
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newName);
            if (newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(2, newPassword);
                pstmt.setInt(3, userId);
            } else {
                pstmt.setInt(2, userId);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateFullProfile(int userId, String newUsername, String newName, String newPassword) {
        try {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement chk = conn.prepareStatement("SELECT id FROM users WHERE username = ? AND id != ?")) {
                chk.setString(1, newUsername);
                chk.setInt(2, userId);
                if (chk.executeQuery().next()) return false;
            }
            String query = (newPassword != null && !newPassword.isEmpty())
                ? "UPDATE users SET username = ?, name = ?, password = ? WHERE id = ?"
                : "UPDATE users SET username = ?, name = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newUsername);
                pstmt.setString(2, newName);
                if (newPassword != null && !newPassword.isEmpty()) {
                    pstmt.setString(3, newPassword);
                    pstmt.setInt(4, userId);
                } else {
                    pstmt.setInt(3, userId);
                }
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
