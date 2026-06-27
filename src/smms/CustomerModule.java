package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerModule extends JFrame {

    JTextField nameField, phoneField, emailField, addressField;
    JTable table;
    DefaultTableModel model;

    // Suite Brand Colors
    Color primaryColor = new Color(41, 128, 185); // Blue
    Color sidebarColor = new Color(44, 62, 80);   // Dark Blue/Grey
    Color bgColor = new Color(236, 240, 241);    // Light Grey
    Color successColor = new Color(46, 204, 113); // Green
    Color dangerColor = new Color(231, 76, 60);  // Red

    public CustomerModule() {
        setTitle("SMMS - Customer Management");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("SMMS CRM", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        // Sidebar Actions
        JButton regBtn = createSidebarButton("REGISTER CUSTOMER", 120);
        regBtn.setBackground(successColor); // Highlight Register in Green
        sidebar.add(regBtn);

        JButton deleteBtn = createSidebarButton("DELETE RECORD", 180);
        deleteBtn.setForeground(new Color(255, 100, 100)); // Light red for danger
        sidebar.add(deleteBtn);

        JButton clearBtn = createSidebarButton("CLEAR FIELDS", 240);
        sidebar.add(clearBtn);

        JButton backBtn = createSidebarButton("BACK TO DASHBOARD", 630);
        sidebar.add(backBtn);
        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT AREA ---
        JPanel mainContent = new JPanel(null);
        mainContent.setBackground(bgColor);
        add(mainContent, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 850, 70);
        header.setBackground(Color.WHITE);
        mainContent.add(header);

        JLabel title = new JLabel("Customer Registration & Loyalty");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 450, 40);
        header.add(title);

        // --- CUSTOMER DATA CARD ---
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(30, 90, 790, 180);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(inputPanel);

        addCardLabel(inputPanel, "Full Name", 25, 20);
        nameField = addCardField(inputPanel, 25, 45, 250);

        addCardLabel(inputPanel, "Phone Number", 300, 20);
        phoneField = addCardField(inputPanel, 300, 45, 200);

        addCardLabel(inputPanel, "Email Address", 525, 20);
        emailField = addCardField(inputPanel, 525, 45, 235);

        addCardLabel(inputPanel, "Home Address", 25, 100);
        addressField = addCardField(inputPanel, 25, 125, 475);

        JButton updateBtn = new JButton("UPDATE PROFILE");
        updateBtn.setBounds(525, 125, 235, 35);
        updateBtn.setBackground(primaryColor);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        inputPanel.add(updateBtn);

        // --- TABLE SECTION ---
        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Phone", "Email", "Address", "Membership"});
        
        table.setRowHeight(35);
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 290, 790, 390);
        sp.setBorder(BorderFactory.createEmptyBorder());
        mainContent.add(sp);

        // --- LOGIC & LISTENERS ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    nameField.setText(model.getValueAt(row, 1).toString());
                    phoneField.setText(model.getValueAt(row, 2).toString());
                    emailField.setText(model.getValueAt(row, 3).toString());
                    addressField.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        regBtn.addActionListener(e -> addCustomer());
        updateBtn.addActionListener(e -> updateCustomer());
        deleteBtn.addActionListener(e -> deleteCustomer());
        clearBtn.addActionListener(e -> clearFields());
        backBtn.addActionListener(e -> { this.dispose(); new AdminDashboard(); });

        loadCustomers();
        setVisible(true);
    }

    // --- UI HELPERS ---
    private JButton createSidebarButton(String text, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(10, y, 230, 45);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        return btn;
    }

    private void addCardLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 150, 20);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l);
    }

    private JTextField addCardField(JPanel p, int x, int y, int w) {
        JTextField f = new JTextField();
        f.setBounds(x, y, w, 35);
        p.add(f);
        return f;
    }

    // --- DATABASE OPERATIONS ---
    void loadCustomers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM S_CUSTOMER ORDER BY CUST_ID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{ rs.getInt("CUST_ID"), rs.getString("CUST_NAME"), 
                    rs.getString("PHONE"), rs.getString("EMAIL"), rs.getString("ADDRESS"),
                    rs.getString("MEMBERSHIP_LEVEL") });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void addCustomer() {
        if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Phone are required!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO S_CUSTOMER (CUST_NAME, PHONE, EMAIL, ADDRESS, MEMBERSHIP_LEVEL) VALUES (?, ?, ?, ?, 'BRONZE')";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText());
            ps.setString(2, phoneField.getText());
            ps.setString(3, emailField.getText());
            ps.setString(4, addressField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer Registered Successfully!");
            loadCustomers();
            clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void updateCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer from the table to update!");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE S_CUSTOMER SET CUST_NAME=?, PHONE=?, EMAIL=?, ADDRESS=? WHERE CUST_ID=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText());
            ps.setString(2, phoneField.getText());
            ps.setString(3, emailField.getText());
            ps.setString(4, addressField.getText());
            ps.setInt(5, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer Profile Updated!");
            loadCustomers();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Update Failed: " + e.getMessage()); }
    }

    void deleteCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this record? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) model.getValueAt(row, 0);
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM S_CUSTOMER WHERE CUST_ID=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                loadCustomers();
                clearFields();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Cannot delete! Customer has associated transaction history."); }
        }
    }

    void clearFields() {
        nameField.setText(""); phoneField.setText(""); emailField.setText(""); addressField.setText("");
        table.clearSelection();
    }
}
