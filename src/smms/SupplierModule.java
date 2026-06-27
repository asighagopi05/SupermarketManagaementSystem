package smms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SupplierModule extends JFrame {

    JTextField nameField, phoneField, emailField, addressField;
    JTable table;
    DefaultTableModel model;
    private int selectedSuppId = -1; // Stores the actual Database ID

    public SupplierModule() {
        setTitle("Supplier Management - SMMS");
        setSize(900, 600);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));

        JLabel title = new JLabel("VENDOR & SUPPLIER MANAGEMENT");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(250, 10, 450, 30);
        add(title);

        // --- UI INPUTS ---
        addLabel("Vendor Name:", 30, 70);
        nameField = addField(140, 70);
        addLabel("Phone:", 30, 110);
        phoneField = addField(140, 110);
        addLabel("Email:", 30, 150);
        emailField = addField(140, 150);
        addLabel("Address:", 30, 190);
        addressField = addField(140, 190);

        // --- BUTTONS ---
        addButton("ADD", 380, 70, e -> addSupplier());
        addButton("UPDATE", 380, 110, e -> updateSupplier());
        addButton("DELETE", 380, 150, e -> deleteSupplier());
        addButton("CLEAR ALL", 380, 190, e -> clearFields());
        
        JButton backBtn = new JButton("BACK");
        backBtn.setBounds(380, 230, 130, 35);
        backBtn.addActionListener(e -> this.dispose());
        add(backBtn);

        // --- TABLE SETUP ---
        model = new DefaultTableModel();
        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        
        // Define 6 columns: Index 0 is S.No, Index 5 is the Hidden Database ID
        model.setColumnIdentifiers(new String[]{"S.No", "Vendor Name", "Phone", "Email", "Address", "DB_ID"});
        
        // Hide the DB_ID column (index 5) from the user's view
        table.removeColumn(table.getColumnModel().getColumn(5));

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 300, 820, 230);
        add(sp);

        // --- CLICK LISTENER ---
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    // Pull the hidden DB_ID from index 5 of the model
                    selectedSuppId = Integer.parseInt(model.getValueAt(row, 5).toString());
                    
                    nameField.setText(getVal(row, 1));
                    phoneField.setText(getVal(row, 2));
                    emailField.setText(getVal(row, 3));
                    addressField.setText(getVal(row, 4));
                }
            }
        });

        loadSuppliers();
        setVisible(true);
    }

    // Safely gets values from the table (Prevents NullPointerException)
    private String getVal(int row, int col) {
        Object val = model.getValueAt(row, col);
        return (val == null) ? "" : val.toString();
    }

    // --- DATABASE OPERATIONS ---

    void loadSuppliers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            // Sorting by SUPP_ID ensures consistent ordering
            String sql = "SELECT * FROM S_SUPPLIER ORDER BY SUPP_ID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            int serialNo = 1; 
            while (rs.next()) {
                model.addRow(new Object[]{
                    serialNo++,               // UI Column: 1, 2, 3...
                    rs.getString("SUPP_NAME"),
                    rs.getString("PHONE"),
                    rs.getString("EMAIL"),
                    rs.getString("ADDRESS"),  // Fetches the Address column from DB
                    rs.getInt("SUPP_ID")      // Hidden Column for updates/deletes
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void addSupplier() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vendor Name is required!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO S_SUPPLIER (SUPP_NAME, PHONE, EMAIL, ADDRESS) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, phoneField.getText().trim());
            ps.setString(3, emailField.getText().trim());
            ps.setString(4, addressField.getText().trim());
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "New Vendor Added!");
            loadSuppliers();
            clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void updateSupplier() {
        if (selectedSuppId == -1) {
            JOptionPane.showMessageDialog(this, "Select a vendor from the table to update!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE S_SUPPLIER SET SUPP_NAME=?, PHONE=?, EMAIL=?, ADDRESS=? WHERE SUPP_ID=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, phoneField.getText().trim());
            ps.setString(3, emailField.getText().trim());
            ps.setString(4, addressField.getText().trim());
            ps.setInt(5, selectedSuppId);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vendor Updated!");
            loadSuppliers();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void deleteSupplier() {
        if (selectedSuppId == -1) {
            JOptionPane.showMessageDialog(this, "Select a vendor from the table to delete!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this vendor?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                String sql = "DELETE FROM S_SUPPLIER WHERE SUPP_ID=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, selectedSuppId);
                ps.executeUpdate();
                loadSuppliers();
                clearFields();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        }
    }

    void clearFields() {
        selectedSuppId = -1;
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
    }

    // --- UI HELPERS ---
    void addLabel(String text, int x, int y) {
        JLabel l = new JLabel(text); l.setBounds(x, y, 100, 25); add(l);
    }
    JTextField addField(int x, int y) {
        JTextField f = new JTextField(); f.setBounds(x, y, 220, 25); add(f); return f;
    }
    void addButton(String text, int x, int y, java.awt.event.ActionListener al) {
        JButton b = new JButton(text); b.setBounds(x, y, 130, 35); b.addActionListener(al); add(b);
    }
}
