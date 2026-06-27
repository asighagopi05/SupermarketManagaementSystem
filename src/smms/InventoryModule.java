package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class InventoryModule extends JFrame {
    JTextField prodIdField, qtyField, minLevelField;
    JTable table;
    DefaultTableModel model;
    private int selectedInvId = -1;

    // Brand Colors
    Color primaryColor = new Color(41, 128, 185); // Blue
    Color sidebarColor = new Color(44, 62, 80);   // Dark Blue/Grey
    Color bgColor = new Color(236, 240, 241);    // Light Grey
    Color accentColor = new Color(231, 76, 60);   // Red for Low Stock

    public InventoryModule() {
        setTitle("SMMS - Inventory Tracking");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("SMMS STOCK", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        // Sidebar Actions
        JButton refreshBtn = createSidebarButton("REFRESH LIST", 120);
        sidebar.add(refreshBtn);

        JButton lowStockBtn = createSidebarButton("LOW STOCK ALERT", 180);
        lowStockBtn.setForeground(new Color(255, 100, 100));
        sidebar.add(lowStockBtn);

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

        JLabel title = new JLabel("Real-Time Stock Tracking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 400, 40);
        header.add(title);

        // --- UPDATE CARD ---
        JPanel updatePanel = new JPanel(null);
        updatePanel.setBounds(30, 90, 790, 130);
        updatePanel.setBackground(Color.WHITE);
        updatePanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(updatePanel);

        addCardLabel(updatePanel, "Product ID", 25, 20);
        prodIdField = addCardField(updatePanel, 25, 45, 120);
        prodIdField.setEditable(false);
        prodIdField.setBackground(new Color(245, 245, 245));

        addCardLabel(updatePanel, "Current Quantity", 170, 20);
        qtyField = addCardField(updatePanel, 170, 45, 150);

        addCardLabel(updatePanel, "Min. Level", 345, 20);
        minLevelField = addCardField(updatePanel, 345, 45, 150);

        JButton updateBtn = new JButton("UPDATE STOCK");
        updateBtn.setBounds(550, 45, 215, 35);
        updateBtn.setBackground(primaryColor);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        updatePanel.add(updateBtn);

        // --- TABLE SECTION ---
        model = new DefaultTableModel();
        table = new JTable(model) { 
            public boolean isCellEditable(int r, int c) { return false; } 
        };
        model.setColumnIdentifiers(new String[]{"S.No", "Prod ID", "Product Name", "Current Qty", "Min Level", "DB_ID"});
        
        table.setRowHeight(35);
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 240, 790, 440);
        sp.setBorder(BorderFactory.createEmptyBorder());
        mainContent.add(sp);
        
        table.removeColumn(table.getColumnModel().getColumn(5));

        // Logic & Events
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedInvId = Integer.parseInt(model.getValueAt(row, 5).toString());
                    prodIdField.setText(model.getValueAt(row, 1).toString());
                    qtyField.setText(model.getValueAt(row, 3).toString());
                    minLevelField.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        updateBtn.addActionListener(e -> updateStock());
        refreshBtn.addActionListener(e -> { clearFields(); loadStock(false); });
        lowStockBtn.addActionListener(e -> { clearFields(); loadStock(true); });
        backBtn.addActionListener(e -> { this.dispose(); new AdminDashboard(); });

        loadStock(false);
        setVisible(true);
    }

    // UI Helper Methods
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

    // Existing Logic
    void loadStock(boolean onlyLow) {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT i.INV_ID, i.PROD_ID, p.PROD_NAME, i.QTY, i.MIN_STOCK " +
                         "FROM S_INVENTORY i JOIN S_PRODUCT p ON i.PROD_ID = p.PROD_ID ";
            if (onlyLow) sql += "WHERE i.QTY < i.MIN_STOCK ";
            sql += "ORDER BY i.INV_ID";

            ResultSet rs = con.createStatement().executeQuery(sql);
            int sNo = 1;
            while (rs.next()) {
                model.addRow(new Object[]{ sNo++, rs.getInt("PROD_ID"), rs.getString("PROD_NAME"),
                    rs.getInt("QTY"), rs.getInt("MIN_STOCK"), rs.getInt("INV_ID") });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void updateStock() {
        if (selectedInvId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table first.");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE S_INVENTORY SET QTY = ?, MIN_STOCK = ? WHERE INV_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(qtyField.getText()));
            ps.setInt(2, Integer.parseInt(minLevelField.getText()));
            ps.setInt(3, selectedInvId);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Inventory Updated successfully.");
            loadStock(false);
            clearFields();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Input Error: " + e.getMessage()); }
    }

    void clearFields() {
        selectedInvId = -1;
        prodIdField.setText("");
        qtyField.setText("");
        minLevelField.setText("");
    }
}
