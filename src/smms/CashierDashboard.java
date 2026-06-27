package smms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CashierDashboard extends JFrame {

    public CashierDashboard() {
        setTitle("Cashier Terminal - Supermarket Pro");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        // --- 1. DEEP NIGHT BACKGROUND CANVAS ---
        getContentPane().setBackground(new Color(11, 14, 21)); 
        getContentPane().setLayout(new BorderLayout());

        // --- 2. GLASS-STYLE HEADER ---
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(28, 33, 46), 0, getHeight(), new Color(18, 22, 31));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(1100, 90));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 52, 71)));
        headerPanel.setLayout(new GridBagLayout()); 
        
        JLabel title = new JLabel("CASHIER SALES TERMINAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(230, 230, 240)); 
        headerPanel.add(title);
        
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        // --- 3. CONSTRAINED CENTER PANEL HUB ---
        // Uses GridBagLayout to hold a balanced inner dashboard wrapper dead-center
        JPanel layoutWrapper = new JPanel(new GridBagLayout());
        layoutWrapper.setBackground(new Color(11, 14, 21));
        
        // Balanced Internal Display Wrapper Card Container
        JPanel innerContent = new JPanel(new BorderLayout(0, 35));
        innerContent.setBackground(new Color(11, 14, 21));
        
        // FIX: Hardcoded dimensional boundaries hold design balance intact during maximization
        innerContent.setPreferredSize(new Dimension(960, 480));

        // Refined 2x3 Grid System
        JPanel cardGrid = new JPanel(new GridLayout(2, 3, 24, 24));
        cardGrid.setBackground(new Color(11, 14, 21));

        cardGrid.add(createMenuButton("NEW BILLING (POS)", e -> new BillingModule()));
        cardGrid.add(createMenuButton("STOCK INVENTORY", e -> new InventoryModule()));
        cardGrid.add(createMenuButton("CUSTOMER RECORDS", e -> new CustomerModule()));
        cardGrid.add(createMenuButton("PAYMENT SETTLEMENT", e -> new PaymentModule()));
        cardGrid.add(createMenuButton("PRODUCT CATALOG", e -> new ProductModule()));
        cardGrid.add(createMenuButton("DAILY SALES VIEW", e -> new ReportModule()));

        innerContent.add(cardGrid, BorderLayout.CENTER);

        // --- 4. CONSTRAINED SYSTEM EXIT BAR ---
        JButton logoutBtn = new JButton("EXIT SYSTEM");
        logoutBtn.setPreferredSize(new Dimension(960, 52)); // Matches the width of the grid perfectly
        logoutBtn.setBackground(new Color(0, 174, 239)); 
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(null);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        logoutBtn.addActionListener(e -> { 
            this.dispose(); 
            new LoginModule(); 
        });

        innerContent.add(logoutBtn, BorderLayout.SOUTH);

        // Center the wrapper inside the main screen layout
        layoutWrapper.add(innerContent, new GridBagConstraints());
        getContentPane().add(layoutWrapper, BorderLayout.CENTER);
        
        setVisible(true);
    }

    private JButton createMenuButton(String text, java.awt.event.ActionListener al) {
        // FIX: Utilizes clean HTML styling formatting code tags to force crisp text wrap distribution
        JButton b = new JButton("<html><center>" + text + "</center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(35, 41, 56), 0, getHeight(), new Color(22, 27, 38));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        
        b.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        b.setForeground(new Color(175, 185, 205));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 90), 1));
        
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBorder(BorderFactory.createLineBorder(new Color(0, 174, 239), 2));
                b.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 90), 1));
                b.setForeground(new Color(175, 185, 205));
            }
        });
        b.addActionListener(al);
        return b;
    }
}
