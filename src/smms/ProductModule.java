package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ProductModule extends JFrame {
    JTextField nameField, priceField, catIdField, brandIdField;
    JTable table;
    DefaultTableModel model;
    private int selectedProdId = -1;

    // Brand Colors
    Color primaryColor = new Color(41, 128, 185); // Blue
    Color sidebarColor = new Color(44, 62, 80);   // Dark Blue/Grey
    Color bgColor = new Color(236, 240, 241);    // Light Grey
    Color dangerColor = new Color(231, 76, 60);  // Red

    public ProductModule() {
        setTitle("SMMS - Product Catalog Management");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("SMMS CATALOG", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        // Sidebar Actions
        JButton addBtn = createSidebarButton("ADD NEW PRODUCT", 120);
        sidebar.add(addBtn);

        JButton deleteBtn = createSidebarButton("DELETE SELECTED", 180);
        deleteBtn.setForeground(new Color(255, 100, 100)); // Light red for danger
        sidebar.add(deleteBtn);

        JButton refreshBtn = createSidebarButton("REFRESH LIST", 240);
        sidebar.add(refreshBtn);

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

        JLabel title = new JLabel("Manage Product Catalog");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 400, 40);
        header.add(title);

        // --- INPUT CARD (Management Area) ---
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(30, 90, 790, 180);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(inputPanel);

        addCardLabel(inputPanel, "Product Name", 25, 20);
        nameField = addCardField(inputPanel, 25, 45, 300);

        addCardLabel(inputPanel, "Base Price (₹)", 350, 20);
        priceField = addCardField(inputPanel, 350, 45, 150);

        addCardLabel(inputPanel, "Category ID", 25, 95);
        catIdField = addCardField(inputPanel, 25, 120, 140);

        addCardLabel(inputPanel, "Brand ID", 185, 95);
        brandIdField = addCardField(inputPanel, 185, 120, 140);

        JButton updateBtn = new JButton("SAVE CHANGES / UPDATE");
        updateBtn.setBounds(550, 120, 215, 35);
        updateBtn.setBackground(primaryColor);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        inputPanel.add(updateBtn);

        // --- TABLE SECTION ---
        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        model.setColumnIdentifiers(new String[]{"S.No", "Product Name", "Price", "Cat ID", "Brand ID", "DB_ID"});
        
        table.setRowHeight(35);
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 290, 790, 390);
        sp.setBorder(BorderFactory.createEmptyBorder());
        mainContent.add(sp);
        
        table.removeColumn(table.getColumnModel().getColumn(5));

        // Logic & Events
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedProdId = Integer.parseInt(model.getValueAt(row, 5).toString());
                    nameField.setText(model.getValueAt(row, 1).toString());
                    priceField.setText(model.getValueAt(row, 2).toString());
                    catIdField.setText(model.getValueAt(row, 3).toString());
                    brandIdField.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        refreshBtn.addActionListener(e -> { clearFields(); loadProducts(); });
        backBtn.addActionListener(e -> { this.dispose(); new AdminDashboard(); });

        loadProducts();
        setVisible(true);
    }

    // --- UI HELPERS ---
    private JButton createSidebarButton(String text, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(10, y, 230, 45);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
    void loadProducts() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT PROD_ID, PROD_NAME, BASE_PRICE, CAT_ID, BRAND_ID FROM S_PRODUCT ORDER BY PROD_ID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            int sNo = 1;
            while (rs.next()) {
                model.addRow(new Object[]{ sNo++, rs.getString("PROD_NAME"), rs.getDouble("BASE_PRICE"), 
                    rs.getInt("CAT_ID"), rs.getInt("BRAND_ID"), rs.getInt("PROD_ID") });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void addProduct() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Name is required!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO S_PRODUCT (PROD_NAME, BASE_PRICE, CAT_ID, BRAND_ID) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText().trim());
            ps.setDouble(2, Double.parseDouble(priceField.getText()));
            ps.setInt(3, Integer.parseInt(catIdField.getText()));
            ps.setInt(4, Integer.parseInt(brandIdField.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product Added!");
            loadProducts();
            clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    void updateProduct() {
        if (selectedProdId == -1) {
            JOptionPane.showMessageDialog(this, "Select a product from the table to update.");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE S_PRODUCT SET PROD_NAME=?, BASE_PRICE=?, CAT_ID=?, BRAND_ID=? WHERE PROD_ID=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nameField.getText().trim());
            ps.setDouble(2, Double.parseDouble(priceField.getText()));
            ps.setInt(3, Integer.parseInt(catIdField.getText()));
            ps.setInt(4, Integer.parseInt(brandIdField.getText()));
            ps.setInt(5, selectedProdId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product Updated Successfully!");
            loadProducts();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Update Error: " + e.getMessage()); }
    }

    void deleteProduct() {
        if (selectedProdId == -1) {
            JOptionPane.showMessageDialog(this, "Select a product from the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this product? It may affect inventory records.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM S_PRODUCT WHERE PROD_ID=?");
                ps.setInt(1, selectedProdId);
                ps.executeUpdate();
                loadProducts();
                clearFields();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Cannot delete! Linked to other records."); }
        }
    }

    void clearFields() {
        selectedProdId = -1;
        nameField.setText("");
        priceField.setText("");
        catIdField.setText("");
        brandIdField.setText("");
    }
}
