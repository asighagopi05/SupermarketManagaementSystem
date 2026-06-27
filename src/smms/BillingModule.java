package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class BillingModule extends JFrame {
    JComboBox<String> custBox, prodBox;
    JTextField qtyField;
    JTable table;
    DefaultTableModel model;
    JLabel totalLabel;
    int currentBillId = -1;

    // Brand Colors (Matching Dashboard Style)
    Color primaryColor = new Color(41, 128, 185); // Blue
    Color sidebarColor = new Color(44, 62, 80);   // Dark Blue/Grey
    Color bgColor = new Color(236, 240, 241);    // Light Grey

    public BillingModule() {
        setTitle("SMMS - Point of Sale");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(null);

        JLabel logo = new JLabel("SMMS POS", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

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

        JLabel title = new JLabel("Create New Invoice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 300, 40);
        header.add(title);

        // --- INPUT SECTION (CARDS) ---
        JPanel inputPanel = new JPanel(null);
        inputPanel.setBounds(30, 90, 790, 130);
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        mainContent.add(inputPanel);

        JLabel l1 = new JLabel("Select Customer"); l1.setBounds(20, 20, 150, 20); inputPanel.add(l1);
        custBox = new JComboBox<>(); custBox.setBounds(20, 45, 250, 35); inputPanel.add(custBox);

        JLabel l2 = new JLabel("Select Product"); l2.setBounds(300, 20, 150, 20); inputPanel.add(l2);
        prodBox = new JComboBox<>(); prodBox.setBounds(300, 45, 250, 35); inputPanel.add(prodBox);

        JLabel l3 = new JLabel("Qty"); l3.setBounds(570, 20, 50, 20); inputPanel.add(l3);
        qtyField = new JTextField(); qtyField.setBounds(570, 45, 60, 35); inputPanel.add(qtyField);

        JButton addBtn = new JButton("ADD ITEM");
        addBtn.setBounds(650, 45, 120, 35);
        addBtn.setBackground(primaryColor);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        inputPanel.add(addBtn);

        // --- TABLE SECTION ---
        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        model.setColumnIdentifiers(new String[]{"ID", "Product Name", "Price", "Qty", "Subtotal"});
        
        // Table Styling
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 240, 790, 330);
        sp.setBorder(BorderFactory.createEmptyBorder());
        mainContent.add(sp);

        // --- BOTTOM BAR ---
        totalLabel = new JLabel("TOTAL: ₹ 0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalLabel.setForeground(sidebarColor);
        totalLabel.setBounds(470, 580, 350, 50);
        mainContent.add(totalLabel);

        JButton completeBtn = new JButton("PROCEED TO PAYMENT");
        completeBtn.setBounds(30, 640, 790, 50);
        completeBtn.setBackground(new Color(46, 204, 113)); // Green
        completeBtn.setForeground(Color.WHITE);
        completeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        completeBtn.setFocusPainted(false);
        completeBtn.setBorderPainted(false);
        mainContent.add(completeBtn);

        // Logic
        loadData();
        addBtn.addActionListener(e -> addToCart());
        completeBtn.addActionListener(e -> { 
            if(currentBillId != -1) { this.dispose(); new PaymentModule(); }
            else JOptionPane.showMessageDialog(this, "Your cart is empty!");
        });
        backBtn.addActionListener(e -> { this.dispose(); new AdminDashboard(); });

        setVisible(true);
    }

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

    // Existing database logic remains the same
    void loadData() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs1 = con.createStatement().executeQuery("SELECT CUST_ID, CUST_NAME FROM S_CUSTOMER");
            while (rs1.next()) custBox.addItem(rs1.getInt(1) + " - " + rs1.getString(2));
            ResultSet rs2 = con.createStatement().executeQuery("SELECT PROD_ID, PROD_NAME FROM S_PRODUCT");
            while (rs2.next()) prodBox.addItem(rs2.getInt(1) + " - " + rs2.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    void createNewBill() {
        int custId = Integer.parseInt(custBox.getSelectedItem().toString().split(" - ")[0]);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO S_BILL (CUST_ID, BILL_DATE, STATUS, TOTAL_AMOUNT) VALUES (?, SYSDATE, 'PENDING', 0)";
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"BILL_ID"});
            ps.setInt(1, custId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) currentBillId = rs.getInt(1);
            custBox.setEnabled(false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    void addToCart() {
        if (qtyField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Quantity!");
            return;
        }
        if (currentBillId == -1) createNewBill();
        int prodId = Integer.parseInt(prodBox.getSelectedItem().toString().split(" - ")[0]);
        int qty = Integer.parseInt(qtyField.getText());
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement psPrice = con.prepareStatement("SELECT BASE_PRICE FROM S_PRODUCT WHERE PROD_ID = ?");
            psPrice.setInt(1, prodId);
            ResultSet rs = psPrice.executeQuery();
            if(!rs.next()) return;
            double basePrice = rs.getDouble(1);

            PreparedStatement psCheck = con.prepareStatement("SELECT QTY_SOLD FROM S_BILL_ITEMS WHERE BILL_ID=? AND PROD_ID=?");
            psCheck.setInt(1, currentBillId); psCheck.setInt(2, prodId);
            ResultSet rsCheck = psCheck.executeQuery();

            if (rsCheck.next()) {
                PreparedStatement psUpd = con.prepareStatement("UPDATE S_BILL_ITEMS SET QTY_SOLD = QTY_SOLD + ? WHERE BILL_ID=? AND PROD_ID=?");
                psUpd.setInt(1, qty); psUpd.setInt(2, currentBillId); psUpd.setInt(3, prodId);
                psUpd.executeUpdate();
            } else {
                PreparedStatement ps = con.prepareStatement("INSERT INTO S_BILL_ITEMS (BILL_ID, PROD_ID, QTY_SOLD, SALE_PRICE) VALUES (?, ?, ?, ?)");
                ps.setInt(1, currentBillId); ps.setInt(2, prodId); ps.setInt(3, qty); ps.setDouble(4, basePrice);
                ps.executeUpdate();
            }
            refreshTable(); updateTotal(); qtyField.setText("");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    void refreshTable() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT bi.PROD_ID, p.PROD_NAME, bi.SALE_PRICE, bi.QTY_SOLD, (bi.QTY_SOLD * bi.SALE_PRICE) FROM S_BILL_ITEMS bi JOIN S_PRODUCT p ON bi.PROD_ID = p.PROD_ID WHERE bi.BILL_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql); ps.setInt(1, currentBillId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4), rs.getDouble(5)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    void updateTotal() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT TOTAL_AMOUNT FROM S_BILL WHERE BILL_ID = ?");
            ps.setInt(1, currentBillId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalLabel.setText("TOTAL: ₹ " + String.format("%.2f", rs.getDouble(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
