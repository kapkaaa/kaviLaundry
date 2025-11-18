package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class KasirDashboard extends JFrame {
    private JButton btnInputTransaksi, btnRiwayatTransaksi, btnLaporanKeuangan, btnStatusPesanan, btnLogout;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public KasirDashboard() {
        initComponents();
        setLocationRelativeTo(null);
        updateWindowShape();
    }

    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color buttonBg = Color.decode("#6da395");

        setUndecorated(true);
        setSize(500, 400);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // =================== PANEL UTAMA ===================
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
        mainPanel.setOpaque(false);

        // =================== TITLE BAR (GRIDBAGLAYOUT - PRECISE ALIGNMENT) ===================
        JPanel titleBar = new JPanel(new GridBagLayout()) {
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
        titleBar.setPreferredSize(new Dimension(500, 40));
        titleBar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5); // padding kecil antar elemen
        gbc.anchor = GridBagConstraints.CENTER; // ⭐️ PENTING: agar semua sejajar vertikal
        gbc.fill = GridBagConstraints.NONE;

        // --- TOMBOL macOS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setOpaque(false);
        JButton btnClose = createMacOSButton(new Color(0xFF5F57));
        JButton btnMinimize = createMacOSButton(new Color(0xFFBD2E));
        JButton btnMaximize = createMacOSButton(new Color(0x28CA42));

        btnClose.addActionListener(e -> System.exit(0));
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMaximize.addActionListener(e -> toggleMaximize());

        buttonPanel.add(btnClose);
        buttonPanel.add(btnMinimize);
        buttonPanel.add(btnMaximize);

        // --- JUDUL ---
        JLabel titleLabel = new JLabel("Kasir Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // --- TANGGAL & WAKTU (mepet kanan atas) ---
        JLabel dateTimeLabel = new JLabel("Memuat...", SwingConstants.RIGHT);
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateTimeLabel.setForeground(textMain);
        // Tidak perlu border — biarkan mepet

        // Susun dengan GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        titleBar.add(buttonPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // ambil ruang kosong di tengah
        titleBar.add(titleLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        titleBar.add(dateTimeLabel, gbc);

        mainPanel.add(titleBar, BorderLayout.NORTH);                

        // =================== HEADER SELAMAT DATANG ===================
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel lblWelcome = new JLabel("Selamat Datang, " + UserSession.getCurrentUsername(), SwingConstants.CENTER);
        lblWelcome.setForeground(textMain);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(lblWelcome);
        mainPanel.add(headerPanel, BorderLayout.CENTER);

        // =================== MENU PANEL DENGAN GRIDBAGLAYOUT ===================
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));
        contentPanel.setOpaque(false);

        GridBagConstraints gbcc = new GridBagConstraints();
        gbcc.insets = new Insets(10, 10, 10, 10);
        gbcc.fill = GridBagConstraints.HORIZONTAL;
        gbcc.weightx = 1.0;
        gbcc.weighty = 0.1;

        // Baris 1
        gbcc.gridx = 0; gbcc.gridy = 0;
        btnInputTransaksi = createStyledButton("💳\nInput Transaksi");
        contentPanel.add(btnInputTransaksi, gbcc);

        gbcc.gridx = 1; gbcc.gridy = 0;
        btnRiwayatTransaksi = createStyledButton("📋\nRiwayat Transaksi");
        contentPanel.add(btnRiwayatTransaksi, gbcc);

        // Baris 2
        gbcc.gridx = 0; gbcc.gridy = 1;
        btnStatusPesanan = createStyledButton("📦\nStatus Pesanan");
        contentPanel.add(btnStatusPesanan, gbcc);

        gbcc.gridx = 1; gbcc.gridy = 1;
        btnLaporanKeuangan = createStyledButton("📊\nLaporan Keuangan");
        contentPanel.add(btnLaporanKeuangan, gbcc);

        // Baris 3 - Logout di tengah
        gbcc.gridx = 0; gbcc.gridy = 2;
        gbcc.gridwidth = 2;
        gbcc.anchor = GridBagConstraints.CENTER;
        btnLogout = createStyledButton("🚪\nLogout");
        btnLogout.setPreferredSize(new Dimension(180, 60));
        contentPanel.add(btnLogout, gbcc);

        mainPanel.add(contentPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        // =================== EVENT LISTENERS ===================
        btnInputTransaksi.addActionListener(e -> new InputTransaksiForm().setVisible(true));
        btnRiwayatTransaksi.addActionListener(e -> new RiwayatTransaksiForm().setVisible(true));
        btnStatusPesanan.addActionListener(e -> new StatusPesananForm().setVisible(true));
        btnLaporanKeuangan.addActionListener(e -> new LaporanKeuanganForm().setVisible(true));
        btnLogout.addActionListener(e -> logout());

        // Drag window
        addWindowDrag(titleBar);
        normalBounds = getBounds();

        // Start real-time date & time updater
        startDateTimeUpdater(dateTimeLabel);
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
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 60));
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

                    if (color.equals(new Color(0xFF5F57))) {
                        g2.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                        g2.drawLine(cx + 3, cy - 3, cx - 3, cy + 3);
                    } else if (color.equals(new Color(0xFFBD2E))) {
                        g2.drawLine(cx - 3, cy, cx + 3, cy);
                    } else if (color.equals(new Color(0x28CA42))) {
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

    // =================== DATE & TIME UPDATER ===================
    private void startDateTimeUpdater(JLabel label) {
        ZoneId jakartaZone = ZoneId.of("Asia/Jakarta");
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("EEEE, dd-MM-yyyy HH:mm:ss", new Locale("id", "ID"));

        updateDateTime(label, jakartaZone, formatter);

        new Timer(1000, e -> updateDateTime(label, jakartaZone, formatter))
            .start();
    }

    private void updateDateTime(JLabel label, ZoneId zone, DateTimeFormatter formatter) {
        LocalDateTime now = LocalDateTime.now(zone);
        label.setText(now.format(formatter));
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