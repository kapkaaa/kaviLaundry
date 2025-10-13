package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;

public class RiwayatTransaksiForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtCariNama;
    private JCheckBox chkShowPending;
    private JButton btnCari, btnRefresh, btnDetail, btnTutup;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public RiwayatTransaksiForm() {
        setUndecorated(true);
        initComponents();
        loadData();
        setLocationRelativeTo(null);
        updateWindowShape();
    }

    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color buttonBg = Color.decode("#6da395");
        Color detailBg = Color.decode("#4A90E2");
        Color tableBg = Color.WHITE;

        setSize(1200, 600);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // =================== PANEL UTAMA DENGAN ROUNDED BACKGROUND ===================
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

        // =================== macOS TITLE BAR ===================
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
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
        titleBar.setPreferredSize(new Dimension(1200, 40));
        titleBar.setOpaque(false);

        JButton btnClose = createMacOSButton(new Color(0xFF5F57));
        JButton btnMinimize = createMacOSButton(new Color(0xFFBD2E));
        JButton btnMaximize = createMacOSButton(new Color(0x28CA42));

        btnClose.addActionListener(e -> dispose());
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMaximize.addActionListener(e -> toggleMaximize());

        titleBar.add(btnClose);
        titleBar.add(btnMinimize);
        titleBar.add(btnMaximize);

        JLabel titleLabel = new JLabel("Riwayat Transaksi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setOpaque(false);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalGlue());

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== PANEL PENCARIAN ===================
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        searchPanel.setOpaque(false);

        searchPanel.add(createLabel("Cari Nama Pelanggan:"));

        txtCariNama = createStyledTextField(20);
        searchPanel.add(txtCariNama);

        btnCari = createActionButton("Cari", buttonBg);
        btnRefresh = createActionButton("Refresh", Color.decode("#FFA500"));
        btnDetail = createActionButton("Detail", detailBg);
        btnTutup = createActionButton("Tutup", Color.decode("#AAAAAA"));

        // Checkbox stylish
        chkShowPending = new JCheckBox("Hanya Pending/Belum Bayar") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isSelected()) {
                    setBackground(new Color(0, 0, 0, 0));
                    setForeground(textMain);
                }
            }
        };
        chkShowPending.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPending.setForeground(textMain);
        chkShowPending.setOpaque(false);
        chkShowPending.addActionListener(e -> loadData());

        btnCari.addActionListener(e -> cariTransaksi());
        btnRefresh.addActionListener(e -> loadData());
        btnDetail.addActionListener(e -> showDetail());
        btnTutup.addActionListener(e -> dispose());

        searchPanel.add(btnCari);
        searchPanel.add(btnRefresh);
        searchPanel.add(chkShowPending);
        searchPanel.add(btnDetail);
        searchPanel.add(btnTutup);

        // Wrap in titled border
        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        searchWrapper.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Pencarian"));
        searchWrapper.setOpaque(false);
        searchWrapper.add(searchPanel, BorderLayout.CENTER);

        // =================== TABLE ===================
        String[] columns = {"ID", "Tanggal", "Pelanggan", "Paket", "Berat", "Total", "Status Pesanan", "Status Bayar", "Metode Bayar", "Kasir"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(buttonBg);
        table.setSelectionForeground(Color.WHITE);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(150);
        table.getColumnModel().getColumn(9).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel tablePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(tableBg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(searchWrapper, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Enter key untuk pencarian
        txtCariNama.addActionListener(e -> cariTransaksi());

        // Drag window
        addWindowDrag(titleBar);
        normalBounds = getBounds();

        setOpacity(1.0f);
        updateWindowShape();
    }

    // =================== HELPER METHODS ===================
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.decode("#222222"));
        return label;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.decode("#222222"));
        return field;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(Color.LIGHT_GRAY);
                } else if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 32));
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

    // =================== LOGIC FORM ===================
    private void loadData() {
        loadData(null);
    }

    private void loadData(String namaPelanggan) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT t.id_transaksi, t.tanggal_transaksi, p.nama, ");
            sql.append("pk.nama as paket_nama, t.berat_kg, t.total_biaya, ");
            sql.append("t.status_pesanan, u.username, t.pembayaran, ");
            sql.append("CASE ");
            sql.append("   WHEN t.pembayaran LIKE '%Bayar Sekarang%' THEN 'Lunas' ");
            sql.append("   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.status_pesanan = 'selesai' AND t.pembayaran NOT LIKE '%LUNAS%' THEN 'Belum Bayar' ");
            sql.append("   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.pembayaran LIKE '%LUNAS%' THEN 'Lunas' ");
            sql.append("   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' THEN 'Pending' ");
            sql.append("   ELSE 'Lunas' ");
            sql.append("END as status_bayar ");
            sql.append("FROM transaksi t ");
            sql.append("LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan ");
            sql.append("LEFT JOIN paket pk ON t.id_jenis = pk.id ");
            sql.append("LEFT JOIN user u ON t.id_user = u.id_user ");

            boolean hasWhere = false;

            if (namaPelanggan != null && !namaPelanggan.trim().isEmpty()) {
                sql.append("WHERE p.nama LIKE ? ");
                hasWhere = true;
            }

            if (chkShowPending.isSelected()) {
                if (hasWhere) {
                    sql.append("AND ");
                } else {
                    sql.append("WHERE ");
                }
                sql.append("(t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.pembayaran NOT LIKE '%LUNAS%') ");
            }

            sql.append("ORDER BY t.tanggal_transaksi DESC");

            PreparedStatement pstmt = conn.prepareStatement(sql.toString());

            if (namaPelanggan != null && !namaPelanggan.trim().isEmpty()) {
                pstmt.setString(1, "%" + namaPelanggan + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                String statusBayar = rs.getString("status_bayar");
                String tingkatCuci = rs.getString("pembayaran");

                String metodeBayar = "Cash - Bayar Sekarang";
                if (tingkatCuci != null) {
                    metodeBayar = tingkatCuci.replace(" - LUNAS", "");
                }

                Object[] row = {
                    rs.getInt("id_transaksi"),
                    sdf.format(rs.getTimestamp("tanggal_transaksi")),
                    rs.getString("nama"),
                    rs.getString("paket_nama"),
                    rs.getDouble("berat_kg") + " kg",
                    "Rp " + String.format("%,.0f", rs.getDouble("total_biaya")),
                    rs.getString("status_pesanan"),
                    statusBayar,
                    metodeBayar,
                    rs.getString("username")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void cariTransaksi() {
        String namaPelanggan = txtCariNama.getText().trim();
        loadData(namaPelanggan);
    }

    private void showDetail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi untuk melihat detail!");
            return;
        }

        int idTransaksi = (Integer) model.getValueAt(selectedRow, 0);
        showDetailDialog(idTransaksi);
    }

    private void showDetailDialog(int idTransaksi) {
        JDialog detailDialog = new JDialog(this, "Detail Transaksi", true);
        detailDialog.setSize(500, 450);
        detailDialog.setLocationRelativeTo(this);

        JTextArea txtDetail = new JTextArea();
        txtDetail.setEditable(false);
        txtDetail.setFont(new Font("Monospaced", Font.PLAIN, 12));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.*, p.nama as nama_pelanggan, pk.nama as paket_nama, u.username, " +
                        "CASE " +
                        "   WHEN t.pembayaran LIKE '%Bayar Sekarang%' THEN 'Lunas' " +
                        "   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.status_pesanan = 'selesai' AND t.pembayaran NOT LIKE '%LUNAS%' THEN 'Belum Bayar' " +
                        "   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.pembayaran LIKE '%LUNAS%' THEN 'Lunas' " +
                        "   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' THEN 'Pending' " +
                        "   ELSE 'Lunas' " +
                        "END as status_bayar " +
                        "FROM transaksi t " +
                        "LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                        "LEFT JOIN paket pk ON t.id_jenis = pk.id " +
                        "LEFT JOIN user u ON t.id_user = u.id_user " +
                        "WHERE t.id_transaksi = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idTransaksi);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                StringBuilder detail = new StringBuilder();

                detail.append("DETAIL TRANSAKSI\n");
                detail.append("========================================\n");
                detail.append(String.format("ID Transaksi : %d\n", rs.getInt("id_transaksi")));
                detail.append(String.format("Tanggal      : %s\n", sdf.format(rs.getTimestamp("tanggal_transaksi"))));
                detail.append(String.format("Pelanggan    : %s\n", rs.getString("nama_pelanggan")));
                detail.append(String.format("Paket        : %s\n", rs.getString("paket_nama")));
                detail.append(String.format("Berat        : %.1f kg\n", rs.getDouble("berat_kg")));
                detail.append(String.format("Total Biaya  : Rp %,.0f\n", rs.getDouble("total_biaya")));
                detail.append(String.format("Status       : %s\n", rs.getString("status_pesanan")));
                detail.append(String.format("Status Bayar : %s\n", rs.getString("status_bayar")));
                detail.append(String.format("Kasir        : %s\n", rs.getString("username")));

                detail.append("========================================\n");
                detail.append("DETAIL PEMBAYARAN:\n");
                String tingkatCuci = rs.getString("pembayaran");
                if (tingkatCuci != null && !tingkatCuci.trim().isEmpty()) {
                    String metode = tingkatCuci.trim();

                    detail.append(String.format("Metode       : %s\n", metode));

                    if (metode.contains("Bayar di Awal")) {
                        if (metode.contains("QRIS")) {
                            detail.append("Jenis        : Non Tunai (QRIS)\n");
                        } else {
                            detail.append("Jenis        : Tunai (Cash)\n");
                        }
                        detail.append("Keterangan   : Dibayar di awal\n");
                    } else if (metode.contains("Bayar Setelah Selesai")) {
                        if (metode.contains("LUNAS")) {
                            if (metode.contains("QRIS")) {
                                detail.append("Jenis        : Non Tunai (QRIS)\n");
                            } else {
                                detail.append("Jenis        : Tunai (Cash)\n");
                            }
                            detail.append("Keterangan   : Sudah dibayar setelah selesai\n");
                        } else {
                            detail.append("Keterangan   : Belum dibayar\n");
                        }
                    }
                }

                String addonIds = rs.getString("addon_ids");
                if (addonIds != null && !addonIds.trim().isEmpty()) {
                    detail.append("========================================\n");
                    detail.append("ADDON YANG DIGUNAKAN:\n");

                    int detergenCount = addonIds.length() - addonIds.replace("1", "").length();
                    int pewangiCount = addonIds.length() - addonIds.replace("2", "").length();

                    if (detergenCount > 0) {
                        detail.append(String.format("Detergen     : %d sachet\n", detergenCount));
                    }
                    if (pewangiCount > 0) {
                        detail.append(String.format("Pewangi      : %d sachet\n", pewangiCount));
                    }
                }

                int voucherDidapat = rs.getInt("voucher_didapat");
                if (voucherDidapat > 0) {
                    detail.append("========================================\n");
                    detail.append(String.format("Voucher +    : %d voucher\n", voucherDidapat));
                }

                txtDetail.setText(detail.toString());
            }
        } catch (SQLException e) {
            txtDetail.setText("Error loading detail: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(txtDetail);
        JButton btnClose = new JButton("Tutup");
        btnClose.addActionListener(e -> detailDialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnClose);

        detailDialog.add(scrollPane, BorderLayout.CENTER);
        detailDialog.add(btnPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }
}