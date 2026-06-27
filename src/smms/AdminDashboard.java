package smms;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    // Dark Theme Colors (Matching your image)
    Color darkBg = new Color(13, 15, 23);       // Deep Dark Background
    Color tileBg = new Color(28, 32, 45);       // Dark Blue-Grey Tile
    Color accentColor = new Color(0, 174, 239); // Bright Blue for Exit button

    public AdminDashboard() {
        setTitle("SMMS - ADMINISTRATIVE CONTROL CENTER");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(darkBg);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel header = new JPanel();
        header.setPreferredSize(new Dimension(1200, 100));
        header.setBackground(new Color(20, 24, 35));
        header.setLayout(new GridBagLayout());
        
        JLabel title = new JLabel("ADMINISTRATIVE CONTROL CENTER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // --- GRID CONTENT (4 Columns x 3 Rows) ---
        JPanel gridPanel = new JPanel(new GridLayout(3, 4, 30, 30)); 
        gridPanel.setBackground(darkBg);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));

        // Adding the 10 Admin Buttons
        gridPanel.add(createTileButton("STAFF MANAGEMENT", e -> { this.dispose(); new EmployeeModule("ADMIN"); }));
        gridPanel.add(createTileButton("USER ACCESS CONTROL", e -> { this.dispose(); new UserModule(); }));
        gridPanel.add(createTileButton("PRODUCT CATALOG", e -> { this.dispose(); new ProductModule(); }));
        gridPanel.add(createTileButton("STOCK INVENTORY", e -> { this.dispose(); new InventoryModule(); }));
        
        gridPanel.add(createTileButton("NEW BILLING (POS)", e -> { this.dispose(); new BillingModule(); }));
        gridPanel.add(createTileButton("SALES ANALYTICS", e -> { this.dispose(); new ReportModule(); }));
        gridPanel.add(createTileButton("STORE MANAGEMENT", e -> { this.dispose(); new StoreModule(); }));
        gridPanel.add(createTileButton("CUSTOMER RECORDS", e -> { this.dispose(); new CustomerModule(); }));
        
        gridPanel.add(createTileButton("PAYMENT SETTLEMENT", e -> { this.dispose(); new PaymentModule(); }));
        
        // MODIFIED LINE: Now launches the DeptModule class
        gridPanel.add(createTileButton("DEPT. MANAGEMENT", e -> { this.dispose(); new DeptModule(); }));
        
        // Empty spacers to keep the grid aligned
        gridPanel.add(new JLabel("")); 
        gridPanel.add(new JLabel(""));

        add(gridPanel, BorderLayout.CENTER);

        // --- FOOTER (Exit Button) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        footer.setBackground(darkBg);
        footer.setPreferredSize(new Dimension(1200, 150));

        JButton exitBtn = new JButton("EXIT SYSTEM");
        exitBtn.setPreferredSize(new Dimension(400, 60));
        exitBtn.setBackground(accentColor);
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        exitBtn.setFocusPainted(false);
        exitBtn.setBorder(null);
        exitBtn.addActionListener(e -> {
            this.dispose();
            new LoginModule();
        });
        footer.add(exitBtn);
        
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createTileButton(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton("<html><center>" + text + "</center></html>");
        btn.setBackground(tileBg);
        btn.setForeground(new Color(180, 190, 210));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(45, 50, 70), 1));
        btn.addActionListener(al);

        // Hover Effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(40, 45, 65));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(tileBg);
                btn.setForeground(new Color(180, 190, 210));
            }
        });

        return btn;
    }
}
