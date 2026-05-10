package ui;



import models.Models.*;
import services.AdminService;
import services.AuthService;
import services.EmployeeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDashboard extends JFrame {
    private final Employee employee;
    private JTabbedPane tabbedPane;
    private DefaultListModel<String> notifModel;
    private JComboBox<Customer> cbCustomers;
    private DefaultTableModel customerTableModel;

    public EmployeeDashboard(Employee employee) {
        this.employee = employee;
        setTitle("POS Terminal - " + employee.getName());
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton btnLogout = UIFactory.danger("Lock Terminal");
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JButton btnProfile = UIFactory.button("Edit My Profile", UIFactory.GREEN);
        btnProfile.addActionListener(e -> showEditProfileDialog());

        add(UIFactory.header("POS Terminal | Operator: " + employee.getName(), new Color(39, 174, 96), btnProfile, btnLogout), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("   Point of Sale (POS)   ", createPOSPanel());
        tabbedPane.addTab("   Customer CRM   ",         createCustomerPanel());
        tabbedPane.addTab("   Orders & Billing   ",     createOrdersPanel());
        tabbedPane.addTab("   Inbox   ",                createNotificationPanel());
        add(tabbedPane, BorderLayout.CENTER);

        checkNotifications();
    }

  

    private void showEditProfileDialog() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JLabel("User ID:")); p.add(new JLabel(String.valueOf(employee.getId())));
        p.add(new JLabel("Username:")); p.add(new JLabel(employee.getUsername()));
        JTextField txtUser = new JTextField(employee.getUsername());
        JTextField txtName = new JTextField(employee.getName());
        JPasswordField txtPass = new JPasswordField(), txtConfirm = new JPasswordField();
        p.add(new JLabel("New Username:")); p.add(txtUser);
        p.add(new JLabel("New Name:"));     p.add(txtName);
        p.add(new JLabel("New Password:")); p.add(txtPass);
        p.add(new JLabel("Confirm:"));      p.add(txtConfirm);

        if (JOptionPane.showConfirmDialog(this, p, "Edit Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String newUser = txtUser.getText().trim(), newName = txtName.getText().trim();
            String newPass = new String(txtPass.getPassword()), confirm = new String(txtConfirm.getPassword());
            if (newUser.isEmpty() || newName.isEmpty()) { JOptionPane.showMessageDialog(this, "Fields cannot be empty."); return; }
            if (!newPass.isEmpty() && !newPass.equals(confirm)) { JOptionPane.showMessageDialog(this, "Passwords don't match!"); return; }
            if (AuthService.updateFullProfile(employee.getId(), newUser, newName, newPass)) {
                employee.setUsername(newUser); employee.setName(newName);
                if (!newPass.isEmpty()) employee.setPassword(newPass);
                JOptionPane.showMessageDialog(this, "Profile updated!");
            } else JOptionPane.showMessageDialog(this, "Failed. Username may be taken.");
        }
    }



    private JPanel createCustomerPanel() {
        JPanel panel = UIFactory.borderPanel("Customer Relationship Management", 20);
        JTable table = UIFactory.table(new String[]{"ID", "Name", "Phone", "Total Spent", "Loyalty Pts", "Tier"});
        customerTableModel = (DefaultTableModel) table.getModel();
        refreshCustomerTable(customerTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextField txtName  = UIFactory.field("Full Name", 11);
        JTextField txtPhone = UIFactory.field("Phone #", 11);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { txtName.setText(customerTableModel.getValueAt(row, 1).toString()); txtPhone.setText(customerTableModel.getValueAt(row, 2).toString()); }
        });

        JButton btnAdd     = UIFactory.primary("Add Customer");
        JButton btnUpdate  = UIFactory.primary("Update");
        JButton btnDelete  = UIFactory.danger("Delete");
        JButton btnSearch  = UIFactory.primary("Search");
        JButton btnProfile = UIFactory.accent("View Profile");
        JButton btnLoyalty = UIFactory.accent("Register Loyalty");

        btnAdd.addActionListener(e -> {
            if (txtName.getText().isEmpty() || txtPhone.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill Name and Phone."); return; }
            if (EmployeeService.addCustomer(txtName.getText(), txtPhone.getText())) {
                refreshCustomerTable(customerTableModel); refreshCustomerDropdown(); txtName.setText(""); txtPhone.setText("");
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            if (txtName.getText().isEmpty() || txtPhone.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill Name and Phone."); return; }
            if (EmployeeService.updateCustomer((int) customerTableModel.getValueAt(row, 0), txtName.getText(), txtPhone.getText())) {
                refreshCustomerTable(customerTableModel); refreshCustomerDropdown(); txtName.setText(""); txtPhone.setText("");
                JOptionPane.showMessageDialog(this, "Updated!");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            if (EmployeeService.deleteCustomer((int) customerTableModel.getValueAt(row, 0))) {
                refreshCustomerTable(customerTableModel); refreshCustomerDropdown();
            }
        });

        btnSearch.addActionListener(e -> {
            String kw = JOptionPane.showInputDialog(this, "Search by ID, Name, or Phone:");
            if (kw == null) return;
            List<Customer> res = new ArrayList<>();
            try { int id = Integer.parseInt(kw.trim()); for (Customer c : EmployeeService.getAllCustomers()) if (c.getId() == id) { res.add(c); break; } }
            catch (NumberFormatException ex) { res = EmployeeService.searchCustomers(kw); }
            customerTableModel.setRowCount(0);
            if (res.isEmpty()) { JOptionPane.showMessageDialog(this, "Not found."); refreshCustomerTable(customerTableModel); }
            else for (Customer c : res) customerTableModel.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), "$" + String.format("%.2f", c.getTotalSpent()), c.getLoyaltyPoints(), c.getRewardTier()});
        });

        btnLoyalty.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            int id = (int) customerTableModel.getValueAt(row, 0);
            String name = (String) customerTableModel.getValueAt(row, 1);
            if (JOptionPane.showConfirmDialog(this, "Enroll " + name + " in loyalty program?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (EmployeeService.registerCustomerLoyalty(id)) { JOptionPane.showMessageDialog(this, name + " enrolled!"); refreshCustomerTable(customerTableModel); }
            }
        });

        btnProfile.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            showCustomerProfile(row);
        });

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Name:")); row1.add(txtName); row1.add(new JLabel("Phone:")); row1.add(txtPhone);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        for (JButton b : new JButton[]{btnAdd, btnUpdate, btnDelete, btnSearch, btnProfile, btnLoyalty}) row2.add(b);

        JPanel ctrl = new JPanel(new GridLayout(2, 1, 5, 5));
        ctrl.add(row1); ctrl.add(row2);
        panel.add(ctrl, BorderLayout.SOUTH);
        return panel;
    }

    private void showCustomerProfile(int row) {
        int custId = (int) customerTableModel.getValueAt(row, 0);
        String custName = (String) customerTableModel.getValueAt(row, 1), custPhone = (String) customerTableModel.getValueAt(row, 2);
        double totalSpent = Double.parseDouble(((String) customerTableModel.getValueAt(row, 3)).replace("$", ""));
        int loyaltyPts = (int) customerTableModel.getValueAt(row, 4);
        String tier = (String) customerTableModel.getValueAt(row, 5);

        JDialog dlg = new JDialog(this, "Profile: " + custName, true);
        dlg.setSize(660, 520); dlg.setLocationRelativeTo(this); dlg.setLayout(new BorderLayout(10, 10));

        JPanel info = new JPanel(new GridLayout(4, 2, 8, 8));
        info.setBorder(BorderFactory.createTitledBorder("Customer Information"));
        info.add(new JLabel("  Name:")); info.add(new JLabel(custName));
        info.add(new JLabel("  Phone:")); info.add(new JLabel(custPhone));
        info.add(new JLabel("  Tier:")); info.add(new JLabel(tier));
        info.add(new JLabel("  Points:")); info.add(new JLabel(String.valueOf(loyaltyPts)));

        JTable ordTable = UIFactory.table(new String[]{"Order ID", "Total", "Status", "Date"});
        DefaultTableModel ordModel = (DefaultTableModel) ordTable.getModel();
        for (Order o : EmployeeService.getCustomerOrders(custId))
            ordModel.addRow(new Object[]{o.getId(), "$" + String.format("%.2f", o.getTotalAmount()), o.getStatus(), o.getOrderDate()});

        double milestone = 500.0; int gifts = (int)(totalSpent / milestone); double remaining = milestone - (totalSpent % milestone);
        JPanel summary = new JPanel(new GridLayout(3, 1, 5, 5));
        summary.setBorder(BorderFactory.createTitledBorder("Spending & Rewards Summary"));
        summary.add(new JLabel("  Total Spent: $" + String.format("%.2f", totalSpent)));
        summary.add(new JLabel("  $" + String.format("%.2f", remaining) + " more to next gift!"));
        summary.add(new JLabel("  Gifts Unlocked: " + gifts));

        dlg.add(info, BorderLayout.NORTH);
        dlg.add(new JScrollPane(ordTable), BorderLayout.CENTER);
        dlg.add(summary, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void refreshCustomerTable(DefaultTableModel m) {
        m.setRowCount(0);
        for (Customer c : EmployeeService.getAllCustomers())
            m.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), "$" + String.format("%.2f", c.getTotalSpent()), c.getLoyaltyPoints(), c.getRewardTier()});
    }

    private void refreshCustomerDropdown() {
        if (cbCustomers == null) return;
        cbCustomers.removeAllItems();
        cbCustomers.addItem(new Customer(0, "-- Select a Customer --", "", 0, 0, ""));
        for (Customer c : EmployeeService.getAllCustomers()) cbCustomers.addItem(c);
    }



    private JPanel createPOSPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Available Menu"));
        DefaultListModel<Meal> mealModel = new DefaultListModel<>();
        JList<Meal> listMeals = new JList<>(mealModel);
        listMeals.setFont(new Font("Segoe UI", Font.PLAIN, 16)); listMeals.setFixedCellHeight(40);
        for (Meal m : AdminService.getAllMeals()) mealModel.addElement(m);
        menuPanel.add(new JScrollPane(listMeals), BorderLayout.CENTER);

        
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Active Order Cart"));
        DefaultListModel<String> cartModel = new DefaultListModel<>();
        JList<String> listCart = new JList<>(cartModel);
        listCart.setFont(new Font("Segoe UI", Font.PLAIN, 16)); listCart.setFixedCellHeight(40);
        cartPanel.add(new JScrollPane(listCart), BorderLayout.CENTER);

        List<Integer> mealIds = new ArrayList<>(), qtys = new ArrayList<>();
        final double[] total = {0.0};
        JLabel lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblTotal.setForeground(new Color(39, 174, 96));

        JButton btnAddCart = UIFactory.primary("Add to Order >>");
        btnAddCart.setPreferredSize(new Dimension(150, 40));
        btnAddCart.addActionListener(e -> {
            Meal m = listMeals.getSelectedValue(); if (m == null) return;
            String qtyStr = JOptionPane.showInputDialog("Quantity for " + m.getName() + ":"); if (qtyStr == null) return;
            try {
                int qty = Integer.parseInt(qtyStr); if (qty <= 0) { JOptionPane.showMessageDialog(this, "Quantity must be > 0."); return; }
                mealIds.add(m.getId()); qtys.add(qty); total[0] += m.getPrice() * qty;
                cartModel.addElement(qty + "x " + m.getName() + "  -  $" + String.format("%.2f", m.getPrice() * qty));
                lblTotal.setText(String.format("Total: $%.2f", total[0]));
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Enter a valid number."); }
        });
        menuPanel.add(btnAddCart, BorderLayout.SOUTH);
        cartPanel.add(lblTotal, BorderLayout.SOUTH);

        
        JPanel checkoutPanel = new JPanel(new GridBagLayout());
        checkoutPanel.setBorder(BorderFactory.createTitledBorder("Checkout"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10,10,10,10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0;

        cbCustomers = new JComboBox<>();
        cbCustomers.addItem(new Customer(0, "-- Select a Customer --", "", 0, 0, ""));
        for (Customer c : EmployeeService.getAllCustomers()) cbCustomers.addItem(c);
        cbCustomers.setPreferredSize(new Dimension(200, 40));

        JTextField txtDiscount = new JTextField("0"); txtDiscount.setPreferredSize(new Dimension(200, 40));

        gbc.gridy = 0; checkoutPanel.add(new JLabel("Assign to Customer:"), gbc);
        gbc.gridy = 1; checkoutPanel.add(cbCustomers, gbc);
        gbc.gridy = 2; checkoutPanel.add(new JLabel("Discount (%):"), gbc);
        gbc.gridy = 3; checkoutPanel.add(txtDiscount, gbc);

        JButton btnCheckout = UIFactory.accent("Complete Transaction"); btnCheckout.setPreferredSize(new Dimension(200, 50));
        btnCheckout.addActionListener(e -> {
            if (mealIds.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty!"); return; }
            double discount = 0;
            try { discount = Double.parseDouble(txtDiscount.getText()); if (discount < 0 || discount > 100) throw new Exception(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid discount (0-100)."); return; }
            Customer c = (Customer) cbCustomers.getSelectedItem();
            if (c == null || c.getId() == 0) { JOptionPane.showMessageDialog(this, "Select a customer first!"); return; }
            if (EmployeeService.createOrder(employee.getId(), c.getId(), total[0] * (1 - discount / 100.0), mealIds, qtys)) {
                JOptionPane.showMessageDialog(this, "Order Completed!");
                cartModel.clear(); mealIds.clear(); qtys.clear(); total[0] = 0; lblTotal.setText("Total: $0.00"); txtDiscount.setText("0");
                checkNotifications();
                mealModel.clear(); for (Meal meal : AdminService.getAllMeals()) mealModel.addElement(meal);
            }
        });
        gbc.gridy = 4; checkoutPanel.add(btnCheckout, gbc);

        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, cartPanel); split1.setDividerLocation(300);
        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split1, checkoutPanel); split2.setDividerLocation(650);
        panel.add(split2, BorderLayout.CENTER);
        return panel;
    }



    private JPanel createOrdersPanel() {
        JPanel panel = UIFactory.borderPanel("Orders Management", 20);
        JTable table = UIFactory.table(new String[]{"Order ID", "Employee ID", "Customer ID", "Total", "Status", "Date"});
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Order o : AdminService.getAllOrders()) {
                if ("Cancelled".equalsIgnoreCase(o.getStatus())) continue;
                model.addRow(new Object[]{o.getId(), o.getEmployeeId(), o.getCustomerId() == 0 ? "Walk-in" : o.getCustomerId(), "$" + String.format("%.2f", o.getTotalAmount()), o.getStatus(), o.getOrderDate()});
            }
        };
        refresh.run();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnCancel  = UIFactory.danger("Cancel Selected Order");
        JButton btnRefresh = UIFactory.primary("Refresh List");

        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
            if ("Cancelled".equals(model.getValueAt(row, 4))) { JOptionPane.showMessageDialog(this, "Already cancelled."); return; }
            if (EmployeeService.cancelOrder((int) model.getValueAt(row, 0))) {
                JOptionPane.showMessageDialog(this, "Order cancelled."); refresh.run();
                if (customerTableModel != null) refreshCustomerTable(customerTableModel);
            }
        });
        btnRefresh.addActionListener(e -> refresh.run());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRefresh); bottom.add(btnCancel);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    

    private JPanel createNotificationPanel() {
        JPanel panel = UIFactory.borderPanel("System Inbox", 20);
        notifModel = new DefaultListModel<>();
        JList<String> list = new JList<>(notifModel);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 16)); list.setFixedCellHeight(40);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        JButton btnRefresh = UIFactory.primary("Refresh Inbox");
        btnRefresh.addActionListener(e -> checkNotifications());
        panel.add(btnRefresh, BorderLayout.SOUTH);
        return panel;
    }

    private void checkNotifications() {
        List<String> notifs = EmployeeService.getUnreadNotifications(employee.getId());
        if (!notifs.isEmpty()) {
            for (String n : notifs) notifModel.addElement("⭐ " + n);
            JOptionPane.showMessageDialog(this, "You have " + notifs.size() + " new notification(s)!");
            tabbedPane.setSelectedIndex(3);
        }
    }
}
