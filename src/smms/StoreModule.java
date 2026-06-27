package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class StoreModule extends JFrame {
    JTextField txtStoreName, txtPhone, txtAddress;
    JTable table;
    DefaultTableModel model;
    private int selectedStoreId = -1; 

    // Suite Brand Colors
    Color primaryColor = new Color(41, 128, 185); // Blue
    Color sidebarColor = new Color(44, 62, 80);   // Dark Blue/Grey
    Color bgColor = new Color(236, 240, 241);    // Light Grey
    Color dangerColor = new Color(231, 76, 60);  // Red

    public StoreModule() {
        setTitle("SMMS - Store Management");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("SMMS STORES", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        // Sidebar Actions
        JButton addBtn = createSidebarButton("REGISTER NEW STORE", 120);
        sidebar.add(addBtn);

        JButton deleteBtn = createSidebarButton("DELETE SELECTED", 180);
        deleteBtn.setForeground(new Color(255, 100, 100));
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

        JLabel title = new JLabel("Store Management & Branches");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 400, 40);
        header.add(title);

        // --- STORE INFO CARD ---
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(30, 90, 790, 160);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(inputPanel);

        addCardLabel(inputPanel, "Store Name", 25, 20);
        txtStoreName = addCardField(inputPanel, 25, 45, 300);

        addCardLabel(inputPanel, "Contact Phone", 350, 20);
        txtPhone = addCardField(inputPanel, 350, 45, 200);

        addCardLabel(inputPanel, "Physical Address", 25, 95);
        txtAddress = addCardField(inputPanel, 25, 120, 525);

        JButton updateBtn = new JButton("UPDATE STORE INFO");
        updateBtn.setBounds(575, 120, 190, 35);
        updateBtn.setBackground(primaryColor);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        inputPanel.add(updateBtn);

        // --- TABLE SECTION ---
        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        model.setColumnIdentifiers(new String[]{"ID", "Store Name", "Phone", "Address"});
        
        table.setRowHeight(35);
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 280, 790, 400);
        sp.setBorder(BorderFactory.createEmptyBorder());
        mainContent.add(sp);

        // Logic & Actions
        loadTable();
        addBtn.addActionListener(e -> addStore());
        updateBtn.addActionListener(e -> updateStore());
        deleteBtn.addActionListener(e -> deleteStore());
        clearBtn.addActionListener(e -> clearFields());
        backBtn.addActionListener(e -> { this.dispose(); new AdminDashboard(); });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedStoreId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtStoreName.setText(model.getValueAt(row, 1).toString());
                    txtPhone.setText(model.getValueAt(row, 2).toString());
                    txtAddress.setText(model.getValueAt(row, 3).toString());
                }
            }
        });

        setVisible(true);
    }

    // UI Helpers
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

    // Database Operations
    void loadTable() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM S_STORE ORDER BY STORE_ID");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void addStore() {
        if (txtStoreName.getText().isEmpty()) return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO S_STORE (STORE_NAME, PHONE, ADDRESS) VALUES (?, ?, ?)");
            ps.setString(1, txtStoreName.getText());
            ps.setString(2, txtPhone.getText());
            ps.setString(3, txtAddress.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "New Store Registered!");
            loadTable();
            clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    void updateStore() {
        if (selectedStoreId == -1) {
            JOptionPane.showMessageDialog(this, "Select a store from the table first!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE S_STORE SET STORE_NAME=?, PHONE=?, ADDRESS=? WHERE STORE_ID=?");
            ps.setString(1, txtStoreName.getText());
            ps.setString(2, txtPhone.getText());
            ps.setString(3, txtAddress.getText());
            ps.setInt(4, selectedStoreId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Store Info Updated!");
            loadTable();
        } catch (Exception e) { e.printStackTrace(); }
    }

    void deleteStore() {
        if (selectedStoreId == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this store record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM S_STORE WHERE STORE_ID=?");
                ps.setInt(1, selectedStoreId);
                ps.executeUpdate();
                loadTable();
                clearFields();
            } catch (Exception e) { 
                JOptionPane.showMessageDialog(this, "Cannot delete! This store has linked employees or inventory.");
            }
        }
    }

    void clearFields() {
        selectedStoreId = -1;
        txtStoreName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
    }
}
