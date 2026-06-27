package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ReportModule extends JFrame {
    JTable table;
    DefaultTableModel model;
    JLabel totalRevenueLabel;
    JTextField searchField;

    // Theme Colors
    Color primaryColor = new Color(41, 128, 185); 
    Color sidebarColor = new Color(44, 62, 80);   
    Color bgColor = new Color(236, 240, 241);    
    Color accentColor = new Color(39, 174, 96);   

    public ReportModule() {
        setTitle("SMMS - Sales & Revenue Analytics");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(250, 800));
        sidebar.setBackground(sidebarColor);

        JLabel logo = new JLabel("REPORT CENTER", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        // Functional Sidebar Buttons
        sidebar.add(createSidebarButton("DAILY SALES", 120, e -> loadDailySales("")));
        sidebar.add(createSidebarButton("PRODUCT SALES", 180, e -> loadProductSales()));
        sidebar.add(createSidebarButton("LOW STOCK", 240, e -> { this.dispose(); new InventoryModule(); }));
        sidebar.add(createSidebarButton("CUSTOMER STATS", 300, e -> loadCustomerStats()));
        
        JButton backBtn = createSidebarButton("BACK TO DASHBOARD", 680, e -> { this.dispose(); new AdminDashboard(); });
        sidebar.add(backBtn);
        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);
        add(mainPanel, BorderLayout.CENTER);

        // Header & Search
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 100));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Sales & Revenue Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBox.setOpaque(false);
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 35));
        JButton searchBtn = new JButton("SEARCH");
        searchBtn.setBackground(primaryColor);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> loadDailySales(searchField.getText().trim()));
        
        searchBox.add(new JLabel("Filter by Customer: "));
        searchBox.add(searchField);
        searchBox.add(searchBtn);
        headerPanel.add(searchBox, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- TABLE ---
        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Global Stripe Renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(242, 244, 246));
                return comp;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        sp.getViewport().setBackground(bgColor);
        mainPanel.add(sp, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setPreferredSize(new Dimension(0, 80));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        totalRevenueLabel = new JLabel("TOTAL REVENUE: ₹ 0.00");
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        totalRevenueLabel.setForeground(accentColor);
        footerPanel.add(totalRevenueLabel, BorderLayout.WEST);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        loadDailySales(""); // Default view
        setVisible(true);
    }

    // --- 1. DAILY SALES LOGIC ---
    void loadDailySales(String filter) {
        model.setColumnIdentifiers(new String[]{"Bill ID", "Transaction Date", "Customer Name", "Amount (₹)"});
        model.setRowCount(0);
        double total = 0;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT b.BILL_ID, NVL(TO_CHAR(b.BILL_DATE, 'YYYY-MM-DD HH24:MI'), 'N/A') as BDATE, " +
                         "c.CUST_NAME, b.TOTAL_AMOUNT FROM S_BILL b JOIN S_CUSTOMER c ON b.CUST_ID = c.CUST_ID " +
                         "WHERE b.STATUS = 'PAID' ";
            if (!filter.isEmpty()) sql += "AND UPPER(c.CUST_NAME) LIKE '%" + filter.toUpperCase() + "%' ";
            sql += "ORDER BY b.BILL_DATE DESC NULLS LAST, b.BILL_ID DESC";

            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                double amt = rs.getDouble("TOTAL_AMOUNT");
                total += amt;
                model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), String.format("%.2f", amt) });
            }
            applyAlignment(3); // Align Amount column
            totalRevenueLabel.setText("TOTAL REVENUE: ₹ " + String.format("%.2f", total));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 2. PRODUCT SALES LOGIC ---
    void loadProductSales() {
        model.setColumnIdentifiers(new String[]{"Product Name", "Total Quantity Sold", "Total Revenue (₹)"});
        model.setRowCount(0);
        double total = 0;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT p.PROD_NAME, SUM(bi.QTY_SOLD), SUM(bi.QTY_SOLD * bi.SALE_PRICE) as REVENUE " +
                         "FROM S_BILL_ITEMS bi JOIN S_PRODUCT p ON bi.PROD_ID = p.PROD_ID " +
                         "GROUP BY p.PROD_NAME ORDER BY REVENUE DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                double rev = rs.getDouble(3);
                total += rev;
                model.addRow(new Object[]{ rs.getString(1), rs.getInt(2), String.format("%.2f", rev) });
            }
            applyAlignment(2); // Align Revenue column
            totalRevenueLabel.setText("TOTAL PRODUCT SALES: ₹ " + String.format("%.2f", total));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 3. CUSTOMER STATS LOGIC ---
    void loadCustomerStats() {
        model.setColumnIdentifiers(new String[]{"Customer Name", "Total Visits", "Lifetime Spending (₹)", "Loyalty Level"});
        model.setRowCount(0);
        double total = 0;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT c.CUST_NAME, COUNT(b.BILL_ID), SUM(b.TOTAL_AMOUNT) as SPENT, c.MEMBERSHIP_LEVEL " +
                         "FROM S_CUSTOMER c LEFT JOIN S_BILL b ON c.CUST_ID = b.CUST_ID " +
                         "WHERE b.STATUS = 'PAID' OR b.STATUS IS NULL " +
                         "GROUP BY c.CUST_NAME, c.MEMBERSHIP_LEVEL ORDER BY SPENT DESC NULLS LAST";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                double spent = rs.getDouble(3);
                total += spent;
                model.addRow(new Object[]{ rs.getString(1), rs.getInt(2), String.format("%.2f", spent), rs.getString(4) });
            }
            applyAlignment(2); // Align Spending column
            totalRevenueLabel.setText("CUMULATIVE CUSTOMER VALUE: ₹ " + String.format("%.2f", total));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UTILITIES ---
    private void applyAlignment(int currencyCol) {
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(currencyCol).setCellRenderer(right);
    }

    private JButton createSidebarButton(String text, int y, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBounds(10, y, 230, 45);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.addActionListener(al);
        return btn;
    }
}
