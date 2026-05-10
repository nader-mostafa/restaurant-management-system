package ui;

import models.Models.*;
import services.AdminService;
import services.AuthService;
import services.EmployeeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class AdminDashboard extends JFrame {
    private final Admin admin;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        setTitle("Restaurant Management System - Admin");
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton btnLogout = UIFactory.danger("Logout");
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JButton btnProfile = UIFactory.button("Edit My Profile", UIFactory.GREEN);
        btnProfile.addActionListener(e -> showEditProfileDialog());

        add(UIFactory.header("Welcome, " + admin.getName(), new Color(41, 128, 185), btnProfile, btnLogout), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("  Employees  ",      createEmployeePanel());
        tabs.addTab("  Meals Menu  ",     createMealPanel());
        tabs.addTab("  Offers  ",         createOffersPanel());
        tabs.addTab("  Loyalty  ",        createLoyaltyPanel());
        tabs.addTab("  Sales History  ",  createSalesHistoryPanel());
        tabs.addTab("  Export Reports  ", createReportsPanel());
        add(tabs, BorderLayout.CENTER);
    }


    private void showEditProfileDialog() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JLabel("User ID:")); p.add(new JLabel(String.valueOf(admin.getId())));
        p.add(new JLabel("Username:")); p.add(new JLabel(admin.getUsername()));
        JTextField txtUser = new JTextField(admin.getUsername());
        JTextField txtName = new JTextField(admin.getName());
        JPasswordField txtPass = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();
        p.add(new JLabel("New Username:")); p.add(txtUser);
        p.add(new JLabel("New Name:"));     p.add(txtName);
        p.add(new JLabel("New Password:")); p.add(txtPass);
        p.add(new JLabel("Confirm:"));      p.add(txtConfirm);

        if (JOptionPane.showConfirmDialog(this, p, "Edit Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String newUser = txtUser.getText().trim(), newName = txtName.getText().trim();
            String newPass = new String(txtPass.getPassword()), confirm = new String(txtConfirm.getPassword());
            if (newUser.isEmpty() || newName.isEmpty()) { JOptionPane.showMessageDialog(this, "Fields cannot be empty."); return; }
            if (!newPass.isEmpty() && !newPass.equals(confirm)) { JOptionPane.showMessageDialog(this, "Passwords don't match!"); return; }
            if (AuthService.updateFullProfile(admin.getId(), newUser, newName, newPass)) {
                admin.setUsername(newUser); admin.setName(newName);
                if (!newPass.isEmpty()) admin.setPassword(newPass);
                JOptionPane.showMessageDialog(this, "Profile updated!");
            } else JOptionPane.showMessageDialog(this, "Update failed. Username may be taken.");
        }
    }


    private JPanel createEmployeePanel() {
        JPanel panel = UIFactory.borderPanel("Employee Management", 20);
        JTable table = UIFactory.table(new String[]{"ID", "Username", "Name"});
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        refreshEmployeeTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextField txtUser = UIFactory.field("Username", 12);
        JPasswordField txtPass = new JPasswordField(12); txtPass.putClientProperty("JTextField.placeholderText", "Password");
        JTextField txtName = UIFactory.field("Full Name", 12);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { txtUser.setText(model.getValueAt(row, 1).toString()); txtName.setText(model.getValueAt(row, 2).toString()); }
        });

        JButton btnAdd    = UIFactory.primary("Add");
        JButton btnUpdate = UIFactory.primary("Update");
        JButton btnDelete = UIFactory.danger("Delete");
        JButton btnSearch = UIFactory.primary("Search");

        btnAdd.addActionListener(e -> {
            if (txtUser.getText().isEmpty() || txtName.getText().isEmpty() || txtPass.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Please fill all fields."); return;
            }
            if (!AdminService.addEmployee(txtUser.getText(), new String(txtPass.getPassword()), txtName.getText()))
                JOptionPane.showMessageDialog(this, "Failed. Username may exist.");
            else { refreshEmployeeTable(model); txtUser.setText(""); txtPass.setText(""); txtName.setText(""); }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee first."); return; }
            if (AdminService.deleteEmployee((int) model.getValueAt(row, 0))) refreshEmployeeTable(model);
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee first."); return; }
            if (txtUser.getText().isEmpty() || txtName.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill Username and Name."); return; }
            if (AdminService.updateEmployee((int) model.getValueAt(row, 0), txtUser.getText(), txtName.getText(), new String(txtPass.getPassword()))) {
                refreshEmployeeTable(model); txtUser.setText(""); txtPass.setText(""); txtName.setText("");
                JOptionPane.showMessageDialog(this, "Updated!");
            }
        });

        btnSearch.addActionListener(e -> {
            String kw = JOptionPane.showInputDialog(this, "Search by ID, Username, or Name:");
            if (kw == null) return;
            java.util.List<Employee> res = new ArrayList<>();
            try { int id = Integer.parseInt(kw.trim()); for (Employee emp : AdminService.getAllEmployees()) if (emp.getId() == id) { res.add(emp); break; } }
            catch (NumberFormatException ex) { res = AdminService.searchEmployees(kw); }
            model.setRowCount(0);
            if (res.isEmpty()) { JOptionPane.showMessageDialog(this, "Not found."); refreshEmployeeTable(model); }
            else for (Employee emp : res) model.addRow(new Object[]{emp.getId(), emp.getUsername(), emp.getName()});
        });

        panel.add(buildCtrlPanel(
            new JComponent[]{new JLabel("Username:"), txtUser, new JLabel("Password:"), txtPass, new JLabel("Name:"), txtName},
            new JButton[]{btnAdd, btnUpdate, btnDelete, btnSearch}
        ), BorderLayout.SOUTH);
        return panel;
    }

    private void refreshEmployeeTable(DefaultTableModel m) {
        m.setRowCount(0);
        for (Employee e : AdminService.getAllEmployees()) m.addRow(new Object[]{e.getId(), e.getUsername(), e.getName()});
    }

    private JPanel createMealPanel() {
        JPanel panel = UIFactory.borderPanel("Meals Management", 20);
        JTable table = UIFactory.table(new String[]{"ID", "Name", "Category", "Price ($)"});
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        refreshMealTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextField txtName  = UIFactory.field("Meal Name", 10);
        JTextField txtCat   = UIFactory.field("Category", 10);
        JTextField txtPrice = UIFactory.field("Price", 6);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { txtName.setText(model.getValueAt(row, 1).toString()); txtCat.setText(model.getValueAt(row, 2).toString()); txtPrice.setText(model.getValueAt(row, 3).toString()); }
        });

        JButton btnAdd    = UIFactory.primary("Add Meal");
        JButton btnUpdate = UIFactory.primary("Update");
        JButton btnDelete = UIFactory.danger("Delete");
        JButton btnSearch = UIFactory.primary("Search");

        btnAdd.addActionListener(e -> {
            if (txtName.getText().isEmpty() || txtCat.getText().isEmpty() || txtPrice.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill all fields."); return; }
            try {
                double price = Double.parseDouble(txtPrice.getText());
                if (price < 0) { JOptionPane.showMessageDialog(this, "Price cannot be negative."); return; }
                if (AdminService.addMeal(txtName.getText(), txtCat.getText(), price)) { refreshMealTable(model); txtName.setText(""); txtCat.setText(""); txtPrice.setText(""); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid price."); }
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a meal first."); return; }
            try {
                double price = Double.parseDouble(txtPrice.getText());
                if (AdminService.updateMeal((int) model.getValueAt(row, 0), txtName.getText(), txtCat.getText(), price)) {
                    refreshMealTable(model); txtName.setText(""); txtCat.setText(""); txtPrice.setText("");
                    JOptionPane.showMessageDialog(this, "Meal updated!");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid price."); }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a meal first."); return; }
            if (AdminService.deleteMeal((int) model.getValueAt(row, 0))) refreshMealTable(model);
        });

        btnSearch.addActionListener(e -> {
            String kw = JOptionPane.showInputDialog(this, "Search meals:");
            if (kw == null) return;
            java.util.List<Meal> res = new ArrayList<>();
            try { int id = Integer.parseInt(kw.trim()); for (Meal m : AdminService.getAllMeals()) if (m.getId() == id) { res.add(m); break; } }
            catch (NumberFormatException ex) { res = AdminService.searchMeals(kw); }
            model.setRowCount(0);
            if (res.isEmpty()) { JOptionPane.showMessageDialog(this, "Not found."); refreshMealTable(model); }
            else for (Meal m : res) model.addRow(new Object[]{m.getId(), m.getName(), m.getCategory(), m.getPrice()});
        });

        panel.add(buildCtrlPanel(
            new JComponent[]{new JLabel("Name:"), txtName, new JLabel("Category:"), txtCat, new JLabel("Price:"), txtPrice},
            new JButton[]{btnAdd, btnUpdate, btnDelete, btnSearch}
        ), BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMealTable(DefaultTableModel m) {
        m.setRowCount(0);
        for (Meal meal : AdminService.getAllMeals()) m.addRow(new Object[]{meal.getId(), meal.getName(), meal.getCategory(), meal.getPrice()});
    }

   
    private JPanel createOffersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Broadcast New Offer");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        JTextField txtName = new JTextField(20); JTextArea txtDesc = new JTextArea(4, 20); txtDesc.setLineWrap(true); JTextField txtDisc = new JTextField(20);

        gbc.gridy = 1; gbc.gridx = 0; panel.add(new JLabel("Offer Title:"), gbc); gbc.gridx = 1; panel.add(txtName, gbc);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(new JLabel("Description:"), gbc); gbc.gridx = 1; panel.add(new JScrollPane(txtDesc), gbc);
        gbc.gridy = 3; gbc.gridx = 0; panel.add(new JLabel("Discount %:"), gbc); gbc.gridx = 1; panel.add(txtDisc, gbc);

        JButton btnAdd = UIFactory.primary("Launch Offer & Notify Employees");
        btnAdd.addActionListener(e -> {
            try {
                if (AdminService.addOffer(txtName.getText(), txtDesc.getText(), Double.parseDouble(txtDisc.getText()))) {
                    JOptionPane.showMessageDialog(this, "Offer broadcasted!"); txtName.setText(""); txtDesc.setText(""); txtDisc.setText("");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid discount."); }
        });
        gbc.gridy = 4; gbc.gridx = 1; panel.add(btnAdd, gbc);
        return panel;
    }

   

    private JPanel createSalesHistoryPanel() {
        JPanel panel = UIFactory.borderPanel("Sales History", 20);
        JTable table = UIFactory.table(new String[]{"Order ID", "Employee ID", "Customer ID", "Total", "Status", "Date"});
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Order o : AdminService.getAllOrders())
                model.addRow(new Object[]{o.getId(), o.getEmployeeId(), o.getCustomerId() == 0 ? "Walk-in" : o.getCustomerId(), "$" + String.format("%.2f", o.getTotalAmount()), o.getStatus(), o.getOrderDate()});
        };
        refresh.run();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = UIFactory.primary("Refresh");
        btnRefresh.addActionListener(e -> refresh.run());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRefresh);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    

    private JPanel createLoyaltyPanel() {
        JPanel panel = UIFactory.borderPanel("Loyalty & Marketing Management", 20);
        JTable table = UIFactory.table(new String[]{"ID", "Name", "Phone", "Tier", "Points", "Total Spent"});
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        refreshLoyaltyTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextField txtPoints = UIFactory.field("Points", 8);
        String[] tiers = {"Bronze", "Silver", "Gold", "Platinum"};
        JComboBox<String> cbTiers = new JComboBox<>(tiers);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { txtPoints.setText(model.getValueAt(row, 4).toString()); cbTiers.setSelectedItem(model.getValueAt(row, 3).toString()); }
        });

        JButton btnUpdate = UIFactory.primary("Update Tier/Points");
        JButton btnSearch = UIFactory.primary("Search Customer");

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            try {
                int id = (int) model.getValueAt(row, 0), pts = Integer.parseInt(txtPoints.getText());
                String tier = cbTiers.getSelectedItem().toString();
                try (java.sql.Connection conn = db.DatabaseManager.getConnection();
                     java.sql.PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET loyalty_points = ?, reward_tier = ? WHERE id = ?")) {
                    pstmt.setInt(1, pts); pstmt.setString(2, tier); pstmt.setInt(3, id);
                    if (pstmt.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Updated!"); refreshLoyaltyTable(model); }
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid points value."); }
        });

        btnSearch.addActionListener(e -> {
            String kw = JOptionPane.showInputDialog(this, "Search by Name or Phone:");
            if (kw == null) return;
            model.setRowCount(0);
            if (kw.trim().isEmpty()) refreshLoyaltyTable(model);
            else for (Customer c : EmployeeService.searchCustomers(kw))
                model.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getRewardTier(), c.getLoyaltyPoints(), "$" + c.getTotalSpent()});
        });

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        ctrl.add(new JLabel("Points:")); ctrl.add(txtPoints); ctrl.add(new JLabel("Tier:")); ctrl.add(cbTiers); ctrl.add(btnUpdate); ctrl.add(btnSearch);
        panel.add(ctrl, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshLoyaltyTable(DefaultTableModel m) {
        m.setRowCount(0);
        for (Customer c : EmployeeService.getAllCustomers())
            m.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getRewardTier(), c.getLoyaltyPoints(), "$" + c.getTotalSpent()});
    }



    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel lbl = new JLabel("Data Export & Reports"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lbl, gbc); gbc.gridwidth = 1;

        JButton btnCust = UIFactory.primary("Generate Customers Report (.CSV)"); btnCust.setPreferredSize(new Dimension(300, 50));
        JButton btnEmp  = UIFactory.primary("Generate Employees Report (.CSV)"); btnEmp.setPreferredSize(new Dimension(300, 50));

        btnCust.addActionListener(e -> { if (AdminService.generateCustomerReport("customers_report.csv")) JOptionPane.showMessageDialog(this, "customers_report.csv exported!"); });
        btnEmp.addActionListener(e  -> { if (AdminService.generateEmployeeReport("employees_report.csv")) JOptionPane.showMessageDialog(this, "employees_report.csv exported!"); });

        gbc.gridy = 1; panel.add(btnCust, gbc);
        gbc.gridy = 2; panel.add(btnEmp, gbc);
        return panel;
    }


    private JPanel buildCtrlPanel(JComponent[] row1Items, JButton[] row2Buttons) {
        JPanel ctrl = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        for (JComponent c : row1Items) row1.add(c);
        for (JButton b : row2Buttons) row2.add(b);
        ctrl.add(row1); ctrl.add(row2);
        return ctrl;
    }
}
