package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class EmployeeModule extends JFrame {
    JTextField nameField, salaryField, phoneField, deptIdField;
    JComboBox<String> roleBox;
    JTable table;
    DefaultTableModel model;
    private int selectedEmpId = -1; 
    private String currentUserRole;

    // Theme Colors
    Color primaryColor = new Color(41, 128, 185); 
    Color sidebarColor = new Color(44, 62, 80);   
    Color bgColor = new Color(236, 240, 241);    

    public EmployeeModule(String role) {
        this.currentUserRole = role;
        setTitle("SMMS - Staff Management");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("SMMS STAFF", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        sidebar.add(createSidebarButton("REGISTER NEW STAFF", 120, e -> addEmployee()));
        sidebar.add(createSidebarButton("DELETE SELECTED", 180, e -> deleteEmployee()));
        sidebar.add(createSidebarButton("CLEAR FIELDS", 240, e -> clearFields()));
        
        // --- MANAGE CREDENTIALS BUTTON REMOVED ---

        JButton backBtn = createSidebarButton("BACK TO DASHBOARD", 630, e -> {
            this.dispose();
            if ("CASHIER".equalsIgnoreCase(currentUserRole)) new CashierDashboard();
            else new AdminDashboard();
        });
        sidebar.add(backBtn);
        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT AREA ---
        JPanel mainContent = new JPanel(null);
        mainContent.setBackground(bgColor);
        add(mainContent, BorderLayout.CENTER);

        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 850, 70);
        header.setBackground(Color.WHITE);
        mainContent.add(header);

        JLabel title = new JLabel("Staff Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 400, 40);
        header.add(title);

        // --- INPUT CARD ---
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(30, 90, 790, 200);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(inputPanel);

        addCardLabel(inputPanel, "Full Name", 25, 20);
        nameField = addCardField(inputPanel, 25, 45, 250); 

        addCardLabel(inputPanel, "Role", 300, 20);
        roleBox = new JComboBox<>(new String[]{"ADMIN", "CASHIER", "MANAGER", "SUPERVISOR", "STOCK MANAGER", "SALES EXECUTIVE"});
        roleBox.setBounds(300, 45, 200, 35);
        inputPanel.add(roleBox);

        addCardLabel(inputPanel, "Salary (₹)", 530, 20);
        salaryField = addCardField(inputPanel, 530, 45, 220); 

        addCardLabel(inputPanel, "Phone Number", 25, 105);
        phoneField = addCardField(inputPanel, 25, 130, 250); 

        addCardLabel(inputPanel, "Dept ID", 300, 105);
        deptIdField = addCardField(inputPanel, 300, 130, 200); 

        JButton updateBtn = new JButton("UPDATE RECORD");
        updateBtn.setBounds(530, 130, 220, 35);
        updateBtn.setBackground(primaryColor);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.addActionListener(e -> updateEmployee());
        inputPanel.add(updateBtn);

        // --- TABLE ---
        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        model.setColumnIdentifiers(new String[]{"S.No", "Name", "Role", "Salary", "Phone", "Dept ID", "DB_ID"});
        table.setRowHeight(35);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 320, 790, 360);
        mainContent.add(sp);
        table.removeColumn(table.getColumnModel().getColumn(6));

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedEmpId = Integer.parseInt(model.getValueAt(row, 6).toString());
                    nameField.setText(getVal(row, 1));
                    roleBox.setSelectedItem(getVal(row, 2));
                    salaryField.setText(getVal(row, 3));
                    phoneField.setText(getVal(row, 4));
                    deptIdField.setText(getVal(row, 5));
                }
            }
        });

        loadEmployees();
        setVisible(true);
    }

    // --- DATABASE OPERATIONS ---
    void addEmployee() {
        if (nameField.getText().isEmpty() || salaryField.getText().isEmpty() || deptIdField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Name, Salary, and Dept ID!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            double salary = Double.parseDouble(salaryField.getText().trim());
            int dept = Integer.parseInt(deptIdField.getText().trim());

            String sql = "INSERT INTO S_EMPLOYEES (EMP_NAME, ROLE, SALARY, PHONE, DEPT_ID) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, roleBox.getSelectedItem().toString());
            ps.setDouble(3, salary);
            ps.setString(4, phoneField.getText().trim());
            ps.setInt(5, dept);
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Staff Registered!");
            loadEmployees();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    void loadEmployees() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM S_EMPLOYEES ORDER BY EMP_ID");
            int sNo = 1;
            while (rs.next()) {
                model.addRow(new Object[]{ sNo++, rs.getString("EMP_NAME"), rs.getString("ROLE"),
                    rs.getDouble("SALARY"), rs.getString("PHONE"), rs.getInt("DEPT_ID"), rs.getInt("EMP_ID") });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void updateEmployee() {
        if (selectedEmpId == -1) { JOptionPane.showMessageDialog(this, "Select staff first!"); return; }
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE S_EMPLOYEES SET EMP_NAME=?, ROLE=?, SALARY=?, PHONE=?, DEPT_ID=? WHERE EMP_ID=?");
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, roleBox.getSelectedItem().toString());
            ps.setDouble(3, Double.parseDouble(salaryField.getText().trim()));
            ps.setString(4, phoneField.getText().trim());
            ps.setInt(5, Integer.parseInt(deptIdField.getText().trim()));
            ps.setInt(6, selectedEmpId);
            ps.executeUpdate();
            loadEmployees();
            JOptionPane.showMessageDialog(this, "Record Updated!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void deleteEmployee() {
        if (selectedEmpId == -1) return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM S_EMPLOYEES WHERE EMP_ID=?");
            ps.setInt(1, selectedEmpId);
            ps.executeUpdate();
            loadEmployees();
            clearFields();
        } catch (Exception e) { e.printStackTrace(); }
    }

    void clearFields() {
        selectedEmpId = -1;
        nameField.setText(""); salaryField.setText(""); phoneField.setText(""); deptIdField.setText("");
        roleBox.setSelectedIndex(0);
    }

    // --- UI HELPERS ---
    private JButton createSidebarButton(String text, int y, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text); btn.setBounds(10, y, 230, 45);
        btn.setBackground(new Color(52, 73, 94)); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11)); btn.addActionListener(al); return btn;
    }

    private void addCardLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text); l.setBounds(x, y, 150, 20);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12)); p.add(l);
    }

    private JTextField addCardField(JPanel p, int x, int y, int w) {
        JTextField f = new JTextField(); f.setBounds(x, y, w, 35); p.add(f); return f;
    }

    private String getVal(int r, int c) { Object v = model.getValueAt(r, c); return (v == null) ? "" : v.toString(); }
}
