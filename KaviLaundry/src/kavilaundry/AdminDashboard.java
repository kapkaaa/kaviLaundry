package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class AdminDashboard extends JFrame {
    private JButton btnKelolaPegawai, btnKelolaHarga;
    private JButton btnRiwayatTransaksi, btnLaporanKeuangan, btnKelolaVoucher;
    private JButton btnLogout;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public AdminDashboard() {
        initComponents();
        setLocationRelativeTo(null);
        updateWindowShape();
    }

    private void initComponents() {
        // 🎨 Warna tema (sama seperti login)
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color buttonBg = Color.decode("#6da395");

        setUndecorated(true);
        setSize(700, 500);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // =================== PANEL UTAMA DENGAN ROUNDED BACKGROUND DAN TITLE BAR DI DALAMNYA ===================
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
        mainPanel.setOpaque(false); // Agar background rounded bisa terlihat

        // =================== TITLE BAR (DI DALAM PANEL UTAMA) ===================
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor); // Sama dengan background
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        titleBar.setPreferredSize(new Dimension(700, 40));
        titleBar.setOpaque(false); // Penting agar tidak override background rounded

        // Tombol macOS
        JButton btnClose = createMacOSButton(new Color(0xFF5F57));
        JButton btnMinimize = createMacOSButton(new Color(0xFFBD2E));
        JButton btnMaximize = createMacOSButton(new Color(0x28CA42));

        btnClose.addActionListener(e -> System.exit(0));
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMaximize.addActionListener(e -> toggleMaximize());

        titleBar.add(btnClose);
        titleBar.add(btnMinimize);
        titleBar.add(btnMaximize);

        // Judul dashboard di tengah title bar
        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setOpaque(false);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalGlue());

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== HEADER SELAMAT DATANG ===================
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel lblWelcome = new JLabel("Selamat Datang, " + UserSession.getCurrentUsername(), SwingConstants.CENTER);
        lblWelcome.setForeground(textMain);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(lblWelcome);
        mainPanel.add(headerPanel, BorderLayout.CENTER);

        // =================== MENU PANEL ===================
        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 25, 25));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        menuPanel.setOpaque(false);

        btnKelolaPegawai = createStyledButton("👥\nKelola Pegawai");
        btnKelolaHarga = createStyledButton("💰\nKelola Harga Layanan");
        btnRiwayatTransaksi = createStyledButton("📋\nRiwayat Transaksi");
        btnLaporanKeuangan = createStyledButton("📊\nLaporan Keuangan");
        btnKelolaVoucher = createStyledButton("🎫\nKelola Voucher");
        btnLogout = createStyledButton("🚪\nLogout");

        menuPanel.add(btnKelolaPegawai);
        menuPanel.add(btnKelolaHarga);
        menuPanel.add(btnRiwayatTransaksi);
        menuPanel.add(btnLaporanKeuangan);
        menuPanel.add(btnKelolaVoucher);
        menuPanel.add(btnLogout);

        mainPanel.add(menuPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        // =================== EVENT LISTENERS ===================
        btnKelolaPegawai.addActionListener(e -> new KelolaPegawaiForm().setVisible(true));
        btnKelolaHarga.addActionListener(e -> new KelolaHargaForm().setVisible(true));
        btnRiwayatTransaksi.addActionListener(e -> new RiwayatTransaksiForm().setVisible(true));
        btnLaporanKeuangan.addActionListener(e -> new LaporanKeuanganForm().setVisible(true));
        btnKelolaVoucher.addActionListener(e -> new VoucherManagementForm().setVisible(true));
        btnLogout.addActionListener(e -> logout());

        // Drag window hanya dari title bar
        addWindowDrag(titleBar);
        normalBounds = getBounds();
    }

    // =================== STYLED BUTTON ===================
    private JButton createStyledButton(String text) {
        JButton button = new JButton("<html><center>" + text + "</center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color baseColor = Color.decode("#6da395");
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setPreferredSize(new Dimension(180, 90));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // =================== macOS BUTTONS ===================
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

    // =================== UTILITAS WINDOW ===================
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

    // =================== LOGOUT ===================
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.clearSession();
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}