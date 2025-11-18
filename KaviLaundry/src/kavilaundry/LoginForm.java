package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public LoginForm() {
        initComponents();
        setLocationRelativeTo(null);
        updateWindowShape();
    }

    private void initComponents() {
        // 🎨 Warna tema
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color textSub = Color.decode("#555555");
        Color accent = Color.decode("#3fc1d3");

        // 🪟 Frame utama tanpa border default
        setUndecorated(true);
        setSize(420, 400);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // =================== PANEL UTAMA DENGAN ROUNDED CORNERS ===================
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false); // Agar background bisa di-custom

        // =================== macOS TITLE BAR ===================
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor); // Same as main panel
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        titleBar.setPreferredSize(new Dimension(420, 40));
        titleBar.setOpaque(false); // Agar tidak override background rounded

        JButton btnClose = createMacOSButton(new Color(0xFF5F57));
        JButton btnMinimize = createMacOSButton(new Color(0xFFBD2E));
        JButton btnMaximize = createMacOSButton(new Color(0x28CA42));

        btnClose.addActionListener(e -> System.exit(0));
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMaximize.addActionListener(e -> toggleMaximize());

        titleBar.add(btnClose);
        titleBar.add(btnMinimize);
        titleBar.add(btnMaximize);

        // Tambahkan title bar ke panel utama
        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== CONTENT LOGIN ===================
        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        contentPanel.setOpaque(false); // Transparan agar background rounded terlihat

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("KAVILAUNDRY", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(textMain);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 30, 0);
        contentPanel.add(lblTitle, gbc);

        // 👤 Username
        ImageIcon userIconRaw = new ImageIcon(getClass().getResource("/icons/user.png"));
        JLabel iconUser = new JLabel(resizeIcon(userIconRaw, 20, 20));
        txtUsername = new JTextField(15);
        styleTextField(txtUsername, "Username", textSub, textMain);

        JPanel userPanel = createInputPanel(iconUser, txtUsername, bgColor);
        gbc.gridy = 1; gbc.insets = new Insets(5, 0, 15, 0);
        contentPanel.add(userPanel, gbc);

        // 🔒 Password
        ImageIcon lockIconRaw = new ImageIcon(getClass().getResource("/icons/lock.png"));
        JLabel iconLock = new JLabel(resizeIcon(lockIconRaw, 20, 20));
        txtPassword = new JPasswordField(15);
        stylePasswordField(txtPassword, "Password", textSub, textMain);

        JPanel passPanel = createInputPanel(iconLock, txtPassword, bgColor);
        gbc.gridy = 2; gbc.insets = new Insets(5, 0, 20, 0);
        contentPanel.add(passPanel, gbc);

        // 🔘 Tombol login
        btnLogin = new JButton("LOG IN");
        btnLogin.setBackground(accent);
        btnLogin.setForeground(Color.black);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> login());
        gbc.gridy = 3; gbc.insets = new Insets(10, 60, 10, 60);
        contentPanel.add(btnLogin, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Enable drag window from title bar only
        addWindowDrag(titleBar);
        normalBounds = getBounds();
    }

    // =================== UTILITAS ===================

    private JButton createMacOSButton(Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());

                if (getModel().isRollover()) {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1.2f));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    if (color.equals(new Color(0xFF5F57))) { // Close
                        g2.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                        g2.drawLine(cx + 3, cy - 3, cx - 3, cy + 3);
                    } else if (color.equals(new Color(0xFFBD2E))) { // Minimize
                        g2.drawLine(cx - 3, cy, cx + 3, cy);
                    } else if (color.equals(new Color(0x28CA42))) { // Maximize/Restore
                        if (isMaximized) {
                            g2.drawRect(cx - 2, cy - 1, 3, 3);
                            g2.drawRect(cx - 1, cy - 2, 3, 3);
                        } else {
                            g2.drawRect(cx - 2, cy - 2, 4, 4);
                        }
                    }
                }
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(14, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(15, 15));
                button.revalidate();
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(14, 14));
                button.revalidate();
                button.repaint();
            }
        });

        return button;
    }

    private void toggleMaximize() {
        if (isMaximized) {
            setBounds(normalBounds);
            isMaximized = false;
        } else {
            normalBounds = getBounds();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screenBounds = ge.getMaximumWindowBounds();
            setBounds(screenBounds);
            isMaximized = true;
        }
        updateWindowShape();
    }

    private void addWindowDrag(Component comp) {
        comp.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });
        comp.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized) {
                    Point curr = e.getLocationOnScreen();
                    setLocation(curr.x - mousePoint.x, curr.y - mousePoint.y);
                }
            }
        });
    }

    private JPanel createInputPanel(JLabel icon, JComponent field, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(icon, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void styleTextField(JTextField field, String placeholder, Color textSub, Color textMain) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(textSub);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(textMain);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(textSub);
                }
            }
        });
    }

    private void stylePasswordField(JPasswordField field, String placeholder, Color textSub, Color textMain) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(textSub);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(textMain);
                }
            }
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setEchoChar((char) 0);
                    field.setForeground(textSub);
                }
            }
        });
    }

    // =================== LOGIN ===================
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.equals("Username") || username.isEmpty() ||
            password.equals("Password") || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT u.*, r.nama_role FROM user u " +
                         "JOIN role r ON u.role_id = r.id_role " +
                         "WHERE u.username = ? AND u.password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("nama_role");
                int userId = rs.getInt("id_user");

                UserSession.setCurrentUser(userId, username, roleId, roleName);
                dispose();
                if (roleId == 1) new KasirDashboard().setVisible(true);
                else if (roleId == 2) new AdminDashboard().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Username atau password salah!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // =================== ROUNDED CORNERS ===================
    private void updateWindowShape() {
        if (!isMaximized) {
            int arc = 20;
            Shape shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc);
            setShape(shape);
        } else {
            setShape(null);
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        updateWindowShape();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateWindowShape();
    }

    // =================== MAIN ===================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}