package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class UserModule extends JFrame {
    JTextField userField;
    JPasswordField passField;
    JComboBox<String> roleBox;
    JTable table;
    DefaultTableModel model;
    String selectedUser = "";

    // Brand Colors (Aligned with Admin/Staff Modules)
    Color primaryColor = new Color(41, 128, 185); 
    Color sidebarColor = new Color(44, 62, 80);   
    Color bgColor = new Color(236, 240, 241);    

    public UserModule() {
        setTitle("SMMS - User Access Control");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 700));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("USER ACCESS", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        sidebar.add(createSideBtn("CREATE/UPDATE USER", 120, e -> saveUser()));
        sidebar.add(createSideBtn("REMOVE ACCESS", 180, e -> deleteUser()));
        
        JButton backBtn = createSideBtn("BACK TO DASHBOARD", 600, e -> {
            this.dispose();
            new AdminDashboard();
        });
        sidebar.add(backBtn);
        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT AREA ---
        JPanel main = new JPanel(null);
        main.setBackground(bgColor);
        add(main, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 800, 70);
        header.setBackground(Color.WHITE);
        mainContentHeader(main, "System Credentials Management");

        // Input Card
        JPanel card = new JPanel(null);
        card.setBounds(30, 90, 730, 130);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        main.add(card);

        addLabel(card, "Username / Login ID", 25, 20);
        userField = new JTextField(); userField.setBounds(25, 45, 200, 35); card.add(userField);

        addLabel(card, "New Password", 250, 20);
        passField = new JPasswordField(); passField.setBounds(250, 45, 200, 35); card.add(passField);

        addLabel(card, "Assign Role", 475, 20);
        roleBox = new JComboBox<>(new String[]{"ADMIN", "CASHIER"});
        roleBox.setBounds(475, 45, 200, 35); card.add(roleBox);

        // Table Section
        model = new DefaultTableModel(new String[]{"Username", "Role"}, 0);
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        table.setRowHeight(35);
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 240, 730, 380);
        sp.setBorder(BorderFactory.createEmptyBorder());
        main.add(sp);

        loadUsers();

        // Row Selection Logic
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedUser = model.getValueAt(row, 0).toString();
                    userField.setText(selectedUser);
                    roleBox.setSelectedItem(model.getValueAt(row, 1).toString());
                }
            }
        });

        setVisible(true);
    }

    // --- REUSABLE UI HELPERS ---
    private void mainContentHeader(JPanel main, String text) {
        JPanel h = new JPanel(null);
        h.setBounds(0, 0, 800, 70);
        h.setBackground(Color.WHITE);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setBounds(30, 15, 400, 40);
        h.add(l);
        main.add(h);
    }

    private void addLabel(JPanel p, String t, int x, int y) {
        JLabel l = new JLabel(t); l.setBounds(x, y, 150, 20); 
        l.setFont(new Font("Segoe UI", Font.BOLD, 12)); p.add(l);
    }

    private JButton createSideBtn(String t, int y, java.awt.event.ActionListener al) {
        JButton b = new JButton(t); b.setBounds(10, y, 230, 45);
        b.setBackground(new Color(52, 73, 94)); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11)); b.setFocusPainted(false);
        b.addActionListener(al); return b;
    }

    // --- DATABASE LOGIC (USING "USERS" TABLE) ---
    void loadUsers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT USERNAME, ROLE FROM USERS");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2)});
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage()); 
        }
    }

    void saveUser() {
        if(userField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            // MERGE logic specifically for your USERS table
            String sql = "MERGE INTO USERS l USING (SELECT ? as un FROM dual) d ON (l.USERNAME = d.un) " +
                         "WHEN MATCHED THEN UPDATE SET PASSWORD = ?, ROLE = ? " +
                         "WHEN NOT MATCHED THEN INSERT (USERNAME, PASSWORD, ROLE) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, userField.getText().trim()); 
            ps.setString(2, new String(passField.getPassword()));
            ps.setString(3, roleBox.getSelectedItem().toString()); 
            ps.setString(4, userField.getText().trim());
            ps.setString(5, new String(passField.getPassword())); 
            ps.setString(6, roleBox.getSelectedItem().toString());
            
            ps.executeUpdate();
            loadUsers();
            JOptionPane.showMessageDialog(this, "User Credentials Saved!");
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Save Error: " + e.getMessage()); 
        }
    }

    void deleteUser() {
        if (selectedUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Revoke access for " + selectedUser + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM USERS WHERE USERNAME = ?");
                ps.setString(1, selectedUser);
                ps.executeUpdate();
                loadUsers();
                userField.setText(""); passField.setText("");
                selectedUser = "";
            } catch (Exception e) { 
                JOptionPane.showMessageDialog(this, "Delete Error: " + e.getMessage()); 
            }
        }
    }
}
