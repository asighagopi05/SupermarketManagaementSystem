package smms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;

public class LoginModule extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JPanel mainContainer;

    public LoginModule() {
        setTitle("Supermarket Pro | Sign In");
        setSize(600, 700); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // Dark Premium Canvas Background
        getContentPane().setBackground(new Color(15, 17, 26)); 
        getContentPane().setLayout(new BorderLayout());

        // Responsive Central Container Panel
        mainContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Centered Interactive Login Backdrop Area
                int cardWidth = Math.min(450, getWidth() - 80);
                int cardHeight = Math.min(520, getHeight() - 80);
                int x = (getWidth() - cardWidth) / 2;
                int y = (getHeight() - cardHeight) / 2;

                g2d.setColor(new Color(23, 26, 38));
                g2d.fillRoundRect(x, y, cardWidth, cardHeight, 12, 12);
                g2d.setColor(new Color(40, 44, 60));
                g2d.drawRoundRect(x, y, cardWidth, cardHeight, 12, 12);
            }
        };
        mainContainer.setBackground(new Color(15, 17, 26));
        mainContainer.setLayout(null);
        getContentPane().add(mainContainer, BorderLayout.CENTER);

        // Component layout rules updated smoothly via runtime layout listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayoutLayout();
            }
        });

        // Initialize UI Elements
        initUiElements();
        updateLayoutLayout();
        setVisible(true);
    }

    private JLabel brand, title, subTitle, userLabel, passLabel;
    private JCheckBox showPass;
    private JButton loginBtn;

    private void initUiElements() {
        brand = new JLabel("SUPERMARKET PRO", SwingConstants.CENTER);
        brand.setForeground(new Color(88, 101, 242)); 
        brand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainContainer.add(brand);

        title = new JLabel("Supermarket Pro", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mainContainer.add(title);

        subTitle = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subTitle.setForeground(new Color(114, 118, 125));
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainContainer.add(subTitle);

        userLabel = new JLabel("Username");
        userLabel.setForeground(new Color(185, 187, 190));
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainContainer.add(userLabel);

        userField = createDarkField(false);
        mainContainer.add(userField);

        passLabel = new JLabel("Password");
        passLabel.setForeground(new Color(185, 187, 190));
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainContainer.add(passLabel);

        passField = (JPasswordField) createDarkField(true);
        mainContainer.add(passField);

        showPass = new JCheckBox("Show password");
        showPass.setForeground(new Color(114, 118, 125));
        showPass.setBackground(new Color(23, 26, 38));
        showPass.setFocusPainted(false);
        showPass.addActionListener(e -> {
            if (showPass.isSelected()) passField.setEchoChar((char) 0);
            else passField.setEchoChar('•');
        });
        mainContainer.add(showPass);

        loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(0, 168, 232)); 
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBorder(null);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> performLogin());
        mainContainer.add(loginBtn);
    }

    private void updateLayoutLayout() {
        int cardWidth = Math.min(450, mainContainer.getWidth() - 80);
        int cardHeight = Math.min(520, mainContainer.getHeight() - 80);
        int cardX = (mainContainer.getWidth() - cardWidth) / 2;
        int cardY = (mainContainer.getHeight() - cardHeight) / 2;

        // Dynamic element boundaries computed from dynamic window canvas anchors
        brand.setBounds(cardX, cardY + 40, cardWidth, 20);
        title.setBounds(cardX, cardY + 70, cardWidth, 40);
        subTitle.setBounds(cardX, cardY + 115, cardWidth, 20);
        
        int fieldWidth = cardWidth - 80;
        int fieldX = cardX + 40;

        userLabel.setBounds(fieldX, cardY + 170, 100, 20);
        userField.setBounds(fieldX, cardY + 195, fieldWidth, 40);
        
        passLabel.setBounds(fieldX, cardY + 265, 100, 20);
        passField.setBounds(fieldX, cardY + 290, fieldWidth, 40);
        
        showPass.setBounds(fieldX, cardY + 345, 200, 20);
        showPass.setBackground(new Color(23, 26, 38));
        
        loginBtn.setBounds(fieldX, cardY + 390, fieldWidth, 45);
        mainContainer.repaint();
    }

    private JTextField createDarkField(boolean isPass) {
        JTextField f = isPass ? new JPasswordField() : new JTextField();
        f.setBackground(new Color(32, 34, 45)); 
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(45, 48, 65), 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        return f;
    }

    private void performLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT ROLE FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("ROLE").trim().toUpperCase();
                this.dispose();
                if (role.equals("ADMIN")) new AdminDashboard().setVisible(true);
                else new CashierDashboard().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        new LoginModule();
    }
}
