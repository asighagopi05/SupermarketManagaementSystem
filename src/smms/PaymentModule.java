package smms;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PaymentModule extends JFrame {
    JComboBox<String> billBox, methodBox;
    JLabel nameLabel, amtLabel;
    JTable table;
    DefaultTableModel model;

    // Brand Colors (Matching Dashboard and Billing)
    Color primaryColor = new Color(41, 128, 185); // Blue
    Color sidebarColor = new Color(44, 62, 80);   // Dark Blue/Grey
    Color bgColor = new Color(236, 240, 241);    // Light Grey
    Color successColor = new Color(46, 204, 113); // Green

    public PaymentModule() {
        setTitle("SMMS - Payment Settlement");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR PANEL ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(null);

        JLabel logo = new JLabel("SMMS PAY", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setBounds(0, 30, 250, 40);
        sidebar.add(logo);

        JButton backBtn = new JButton("BACK TO DASHBOARD");
        backBtn.setBounds(10, 630, 230, 45);
        backBtn.setBackground(new Color(52, 73, 94));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder());
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

        JLabel title = new JLabel("Bill Settlement & Payments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(30, 15, 400, 40);
        header.add(title);

        // --- SETTLEMENT CARD ---
        JPanel processPanel = new JPanel(null);
        processPanel.setBounds(30, 90, 790, 180);
        processPanel.setBackground(Color.WHITE);
        processPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainContent.add(processPanel);

        JLabel lb1 = new JLabel("Select Bill ID"); lb1.setBounds(25, 20, 150, 20); processPanel.add(lb1);
        billBox = new JComboBox<>(); billBox.setBounds(25, 45, 220, 35); processPanel.add(billBox);

        JLabel lb2 = new JLabel("Payment Method"); lb2.setBounds(25, 95, 150, 20); processPanel.add(lb2);
        methodBox = new JComboBox<>(new String[]{"CASH", "CARD", "UPI"}); 
        methodBox.setBounds(25, 120, 220, 35); processPanel.add(methodBox);

        // Preview Section within card
        nameLabel = new JLabel("Customer: ---");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setBounds(280, 45, 300, 30);
        processPanel.add(nameLabel);

        amtLabel = new JLabel("Amount Due: ₹ 0.00");
        amtLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        amtLabel.setForeground(sidebarColor);
        amtLabel.setBounds(280, 80, 300, 40);
        processPanel.add(amtLabel);

        JButton payBtn = new JButton("COMPLETE SETTLEMENT");
        payBtn.setBounds(550, 60, 215, 60);
        payBtn.setBackground(successColor);
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        payBtn.setFocusPainted(false);
        payBtn.setBorderPainted(false);
        processPanel.add(payBtn);

        // --- HISTORY TABLE SECTION ---
        JLabel historyTitle = new JLabel("Recent Transactions");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyTitle.setBounds(35, 290, 200, 25);
        mainContent.add(historyTitle);

        model = new DefaultTableModel();
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        model.setColumnIdentifiers(new String[]{"Pay ID", "Bill ID", "Customer", "Amount", "Method", "Date"});
        
        // Modern Table Styling
        table.setRowHeight(35);
        table.setGridColor(new Color(240, 240, 240));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(sidebarColor);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 320, 790, 360);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        mainContent.add(sp);

        // Initial Logic
        loadPendingBills(); 
        loadPayments();
        
        billBox.addActionListener(e -> updatePreview());
        payBtn.addActionListener(e -> processPayment());
        backBtn.addActionListener(e -> { this.dispose(); new AdminDashboard(); });

        setVisible(true);
    }

    // Database logic (kept consistent with your existing logic)
    void loadPendingBills() {
        billBox.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT b.BILL_ID, c.CUST_NAME FROM S_BILL b JOIN S_CUSTOMER c ON b.CUST_ID = c.CUST_ID WHERE b.STATUS = 'PENDING'");
            while (rs.next()) billBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    void updatePreview() {
        if (billBox.getSelectedItem() == null) return;
        int id = Integer.parseInt(billBox.getSelectedItem().toString().split(" - ")[0]);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT c.CUST_NAME, b.TOTAL_AMOUNT FROM S_BILL b JOIN S_CUSTOMER c ON b.CUST_ID = c.CUST_ID WHERE b.BILL_ID = ?");
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) { 
                nameLabel.setText("Customer: " + rs.getString(1)); 
                amtLabel.setText("Amount Due: ₹ " + String.format("%.2f", rs.getDouble(2))); 
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void processPayment() {
        if (billBox.getSelectedItem() == null) return;
        int id = Integer.parseInt(billBox.getSelectedItem().toString().split(" - ")[0]);
        // Extracting double value from the label text
        String amtStr = amtLabel.getText().replace("Amount Due: ₹ ", "").trim();
        double amt = Double.parseDouble(amtStr);
        
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement ps1 = con.prepareStatement("INSERT INTO S_PAYMENT (BILL_ID, AMOUNT, METHOD, PAY_DATE) VALUES (?, ?, ?, SYSDATE)");
            ps1.setInt(1, id); ps1.setDouble(2, amt); ps1.setString(3, methodBox.getSelectedItem().toString()); ps1.executeUpdate();
            
            PreparedStatement ps2 = con.prepareStatement("UPDATE S_BILL SET STATUS = 'PAID' WHERE BILL_ID = ?");
            ps2.setInt(1, id); ps2.executeUpdate();
            
            con.commit();
            JOptionPane.showMessageDialog(this, "Payment Settled Successfully!");
            loadPendingBills(); 
            loadPayments();
            nameLabel.setText("Customer: ---");
            amtLabel.setText("Amount Due: ₹ 0.00");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    void loadPayments() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT p.PAYMENT_ID, p.BILL_ID, c.CUST_NAME, p.AMOUNT, p.METHOD, p.PAY_DATE FROM S_PAYMENT p JOIN S_BILL b ON p.BILL_ID = b.BILL_ID JOIN S_CUSTOMER c ON b.CUST_ID = c.CUST_ID ORDER BY p.PAY_DATE DESC");
            while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4), rs.getString(5), rs.getTimestamp(6)});
        } catch (Exception e) { e.printStackTrace(); }
    }
}
