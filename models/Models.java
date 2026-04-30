package models;

// ده الملف المسؤول عن النماذج الأساسية في المشروع (Models) زي المستخدمين والوجبات والطلبات. 
// اتجمعوا في ملف واحد عشان الكود يكون أنظف وأسهل للفهم وكل كلاس يعتبر قسم لوحده.

import java.util.Date;

public class Models {

    // ==========================================
    // قسم المستخدمين (Users)
    // ==========================================
    public static abstract class User {
        protected int id;
        protected String username;
        protected String password;
        protected String role;
        protected String name;

        public User(int id, String username, String password, String role, String name) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
            this.name = name;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class Admin extends User {
        public Admin(int id, String username, String password, String name) {
            super(id, username, password, "admin", name);
        }
    }

    public static class Employee extends User {
        public Employee(int id, String username, String password, String name) {
            super(id, username, password, "employee", name);
        }
    }

    // ==========================================
    // قسم العملاء (Customers)
    // ==========================================
    public static class Customer {
        private int id;
        private String name;
        private String phone;
        private double totalSpent;
        private int loyaltyPoints;
        private String rewardTier;

        public Customer(int id, String name, String phone, double totalSpent, int loyaltyPoints, String rewardTier) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.totalSpent = totalSpent;
            this.loyaltyPoints = loyaltyPoints;
            this.rewardTier = rewardTier;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
        public int getLoyaltyPoints() { return loyaltyPoints; }
        public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
        public String getRewardTier() { return rewardTier; }
        public void setRewardTier(String rewardTier) { this.rewardTier = rewardTier; }
        
        @Override
        public String toString() {
            return name + " (" + phone + ")";
        }
    }

    // ==========================================
    // قسم الوجبات (Meals)
    // ==========================================
    public static class Meal {
        private int id;
        private String name;
        private String category;
        private double price;

        public Meal(int id, String name, String category, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        @Override
        public String toString() {
            return name + " - $" + price;
        }
    }

    // ==========================================
    // قسم العروض (Offers)
    // ==========================================
    public static class Offer {
        private int id;
        private String name;
        private String description;
        private double discountPercent;

        public Offer(int id, String name, String description, double discountPercent) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.discountPercent = discountPercent;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getDiscountPercent() { return discountPercent; }
    }

    // ==========================================
    // قسم الطلبات (Orders)
    // ==========================================
    public static class Order {
        private int id;
        private int employeeId;
        private int customerId;
        private double totalAmount;
        private String status;
        private Date orderDate;

        public Order(int id, int employeeId, int customerId, double totalAmount, String status, Date orderDate) {
            this.id = id;
            this.employeeId = employeeId;
            this.customerId = customerId;
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
        }

        public int getId() { return id; }
        public int getEmployeeId() { return employeeId; }
        public int getCustomerId() { return customerId; }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public Date getOrderDate() { return orderDate; }
    }
}
