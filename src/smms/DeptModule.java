package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DeptModule extends JFrame {
    JTextField txtDeptName, txtLocation;
    JComboBox<String> storeBox;
    JTable table;
    DefaultTableModel model;
    private int selectedDeptId = -1;

    // Brand Colors
    Color primaryColor = new Color(41, 128, 185); 
    Color sidebarColor = new Color(44, 62, 80);   
    Color bgColor = new Color(236, 240, 241);    

    public DeptModule() {
        setTitle("SMMS - Department Management");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("SMMS DEPTS", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        sidebar.add(createSidebarButton("REGISTER NEW DEPT", 120, e -> addDept()));
        sidebar.add(createSidebarButton("DELETE SELECTED", 180, e -> deleteDept()));
        sidebar.add(createSidebarButton("CLEAR FIELDS", 240, e -> clearFields()));

        JButton backBtn = createSidebarButton("BACK TO DASHBOARD", 630, e -> {
            this.dispose();
            new AdminDashboard();
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

        JLabel titleLabel = new JLabel("Department & Floor Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBounds(30, 15, 450, 40);
        header.add(titleLabel);

        // --- INPUT CARD ---
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(30, 90, 790, 150);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(inputPanel);

        addCardLabel(inputPanel, "Department Name", 25, 20);
        txtDeptName = addCardField(inputPanel, 25, 45, 250);

        addCardLabel(inputPanel, "Floor / Location", 300, 20);
        txtLocation = addCardField(inputPanel, 300, 45, 200);

        addCardLabel(inputPanel, "Assigned Store", 25, 95);
        storeBox = new JComboBox<>();
        storeBox.setBounds(25, 115, 475, 30);
        inputPanel.add(storeBox);

        JButton updateBtn = new JButton("UPDATE DEPARTMENT");
        updateBtn.setBounds(530, 115, 230, 30);
        updateBtn.setBackground(primaryColor);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.addActionListener(e -> updateDept());
        inputPanel.add(updateBtn);

        // --- TABLE ---
        model = new DefaultTableModel(new String[]{"ID", "Dept Name", "Floor/Loc", "Store ID"}, 0);
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        table.setRowHeight(35);
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 270, 790, 410);
        mainContent.add(sp);

        loadStores();
        loadTable();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedDeptId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtDeptName.setText(model.getValueAt(row, 1).toString());
                    txtLocation.setText(model.getValueAt(row, 2).toString());
                    
                    String storeIdStr = model.getValueAt(row, 3).toString();
                    for (int i = 0; i < storeBox.getItemCount(); i++) {
                        if (storeBox.getItemAt(i).startsWith(storeIdStr + " -")) {
                            storeBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });

        setVisible(true);
    }

    // --- RECTIFIED LOGIC ---
    void addDept() {
        if (txtDeptName.getText().trim().isEmpty() || storeBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String selected = storeBox.getSelectedItem().toString();
            int storeId = Integer.parseInt(selected.split(" - ")[0]); // FIXED: Added [0]

            String sql = "INSERT INTO S_DEPARTMENT (DEPT_NAME, LOCATION, STORE_ID) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtDeptName.getText().trim());
            ps.setString(2, txtLocation.getText().trim());
            ps.setInt(3, storeId);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Department Added!");
            loadTable();
            clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void updateDept() {
        if (selectedDeptId == -1) {
            JOptionPane.showMessageDialog(this, "Select a department from the table first.");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String selected = storeBox.getSelectedItem().toString();
            int storeId = Integer.parseInt(selected.split(" - ")[0]); // FIXED: Added [0]

            String sql = "UPDATE S_DEPARTMENT SET DEPT_NAME=?, LOCATION=?, STORE_ID=? WHERE DEPT_ID=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtDeptName.getText().trim());
            ps.setString(2, txtLocation.getText().trim());
            ps.setInt(3, storeId);
            ps.setInt(4, selectedDeptId);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Update Successful!");
            loadTable();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void loadStores() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT STORE_ID, STORE_NAME FROM S_STORE ORDER BY STORE_ID");
            storeBox.removeAllItems();
            while (rs.next()) storeBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    void loadTable() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM S_DEPARTMENT ORDER BY DEPT_ID");
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    void deleteDept() {
        if (selectedDeptId == -1) return;
        if (JOptionPane.showConfirmDialog(this, "Delete department?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM S_DEPARTMENT WHERE DEPT_ID=?");
                ps.setInt(1, selectedDeptId);
                ps.executeUpdate();
                loadTable();
                clearFields();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: Linked records exist."); }
        }
    }

    void clearFields() {
        selectedDeptId = -1;
        txtDeptName.setText("");
        txtLocation.setText("");
        if (storeBox.getItemCount() > 0) storeBox.setSelectedIndex(0);
    }

    private JButton createSidebarButton(String text, int y, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBounds(10, y, 230, 45);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.addActionListener(al);
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
        f.setBounds(x, y, w, 30);
        p.add(f);
        return f;
    }
}

