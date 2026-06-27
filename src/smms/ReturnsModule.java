package smms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReturnsModule extends JFrame {

    JComboBox<String> billBox;
    JTextField qtyField, reasonField;
    JTable table;
    DefaultTableModel model;

    public ReturnsModule() {
        setTitle("Returns Management - SMMS");
        setSize(950, 700);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));

        JLabel title = new JLabel("PROCESS CUSTOMER RETURNS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBounds(330, 10, 400, 30);
        add(title);

        addLabel("Select Bill ID:", 30, 70);
        billBox = new JComboBox<>();
        billBox.setBounds(180, 70, 350, 25);
        add(billBox);

        model = new DefaultTableModel();
        table = new JTable(model);
        model.setColumnIdentifiers(new String[]{"Bill ID", "Prod ID", "Product Name", "Qty Bought", "Price"});
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 110, 880, 250);
        add(sp);

        addLabel("Qty to Return:", 30, 380);
        qtyField = new JTextField();
        qtyField.setBounds(180, 380, 100, 25);
        add(qtyField);

        addLabel("Reason:", 30, 420);
        reasonField = new JTextField();
        reasonField.setBounds(180, 420, 350, 25);
        add(reasonField);

        JButton processBtn = new JButton("COMPLETE RETURN");
        processBtn.setBounds(180, 480, 180, 40);
        processBtn.setBackground(new Color(211, 47, 47));
        processBtn.setForeground(Color.WHITE);
        add(processBtn);

        JButton backBtn = new JButton("BACK");
        backBtn.setBounds(380, 480, 100, 40);
        add(backBtn);

        loadBills();
        billBox.addActionListener(e -> loadBillDetails());
        processBtn.addActionListener(e -> processReturn());
        backBtn.addActionListener(e -> this.dispose());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    void loadBills() {
        billBox.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT b.BILL_ID, c.CUST_NAME FROM S_BILL b JOIN S_CUSTOMER c ON b.CUST_ID = c.CUST_ID ORDER BY b.BILL_ID DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                billBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    void loadBillDetails() {
        if (billBox.getSelectedItem() == null) return;
        try (Connection con = DBConnection.getConnection()) {
            String selected = billBox.getSelectedItem().toString();
            String[] parts = selected.split(" - ");
            int billId = Integer.parseInt(parts[0]); 

            model.setRowCount(0); 
            String sql = "SELECT bi.BILL_ID, bi.PROD_ID, p.PROD_NAME, bi.QTY_SOLD, bi.SALE_PRICE " +
                         "FROM S_BILL_ITEMS bi JOIN S_PRODUCT p ON bi.PROD_ID = p.PROD_ID " +
                         "WHERE bi.BILL_ID = ?";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getDouble(5)});
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    void processReturn() {
        int row = table.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(this, "Select an item from the bill!"); 
            return; 
        }
        
        try (Connection con = DBConnection.getConnection()) {
            // REMOVE RETURN_ID and S_RETURN_SEQ from the query
            String sql = "INSERT INTO S_RETURN (BILL_ID, PROD_ID, QTY_RETURN, REASON, RETURN_DATE) " +
                         "VALUES (?, ?, ?, ?, SYSDATE)";
            
            PreparedStatement ps = con.prepareStatement(sql);
            
            // Parameter indexes shift by 1 because we removed the ID
            ps.setInt(1, (int) model.getValueAt(row, 0)); // BILL_ID
            ps.setInt(2, (int) model.getValueAt(row, 1)); // PROD_ID
            ps.setInt(3, Integer.parseInt(qtyField.getText())); // QTY_RETURN
            ps.setString(4, reasonField.getText()); // REASON
            
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Success! Item '" + model.getValueAt(row, 2) + "' restored to stock.");
            qtyField.setText(""); 
            reasonField.setText("");
            loadBillDetails(); 
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage()); 
        }
    }

    void addLabel(String t, int x, int y) { JLabel l = new JLabel(t); l.setBounds(x, y, 150, 25); add(l); }
}
