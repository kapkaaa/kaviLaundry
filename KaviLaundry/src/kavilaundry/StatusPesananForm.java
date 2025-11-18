package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.regex.*;

public class StatusPesananForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbStatus, cmbStatusBayar;
    private JButton btnUpdate, btnRefresh, btnTutup, btnDetail, btnClearSearch;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public StatusPesananForm() {
        setUndecorated(true);
        initComponents();
        loadData();
        setLocationRelativeTo(null);
        updateWindowShape();
    }
    
    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color updateBg = Color.decode("#6da395");   // Hijau untuk update
        Color detailBg = Color.decode("#4A90E2");   // Biru untuk detail
        Color tutupBg = Color.decode("#FF4444");    // Merah untuk tutup
        Color tableBg = Color.WHITE;

        setSize(1100, 600);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // =================== PANEL UTAMA DENGAN ROUNDED BACKGROUND ===================
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        titleBar.setPreferredSize(new Dimension(1100, 40));
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

        JLabel titleLabel = new JLabel("Status Pesanan & Pembayaran", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setOpaque(false);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalGlue());

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== PANEL SEARCH ===================
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Pencarian"));

        searchPanel.add(createLabel("Cari (ID/Nama/Paket):"));
        txtSearch = createStyledTextField(30);
        searchPanel.add(txtSearch);

        btnClearSearch = createActionButton("Clear", Color.GRAY);
        searchPanel.add(btnClearSearch);

        // =================== PANEL KONTROL ===================
        JPanel controlPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Update Status & Pembayaran"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Status Pesanan
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(createLabel("Status Pesanan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbStatus = new JComboBox<>(new String[]{"diterima", "dicuci", "dijemur", "setrika", "selesai", "diambil"});
        styleComboBox(cmbStatus);
        cmbStatus.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(cmbStatus, gbc);

        // Status Pembayaran
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        controlPanel.add(createLabel("Status Bayar:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbStatusBayar = new JComboBox<>(new String[]{"Belum Bayar", "Lunas"});
        styleComboBox(cmbStatusBayar);
        cmbStatusBayar.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(cmbStatusBayar, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        btnUpdate = createActionButton("💾 Update", updateBg);
        btnUpdate.setPreferredSize(new Dimension(120, 32));
        controlPanel.add(btnUpdate, gbc);

        gbc.gridx = 1;
        btnDetail = createActionButton("📄 Detail", detailBg);
        btnDetail.setPreferredSize(new Dimension(100, 32));
        controlPanel.add(btnDetail, gbc);

        gbc.gridx = 2;
        btnRefresh = createActionButton("🔄 Refresh", Color.decode("#FFA500"));
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        controlPanel.add(btnRefresh, gbc);

        gbc.gridx = 3;
        btnTutup = createActionButton("❌ Tutup", tutupBg);
        btnTutup.setPreferredSize(new Dimension(100, 32));
        controlPanel.add(btnTutup, gbc);

        // Event listeners
        btnUpdate.addActionListener(e -> updateSemuaStatus());
        btnDetail.addActionListener(e -> showDetail());
        btnRefresh.addActionListener(e -> loadData());
        btnTutup.addActionListener(e -> dispose());
        btnClearSearch.addActionListener(e -> clearSearch());

        // Search listener dengan delay
        txtSearch.addKeyListener(new KeyAdapter() {
            private Timer timer;
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (timer != null) {
                    timer.stop();
                }
                timer = new Timer(300, evt -> filterTable());
                timer.setRepeats(false);
                timer.start();
            }
        });

        // =================== TABLE ===================
        String[] columns = {"ID", "Tanggal", "Pelanggan", "Paket", "Berat", "Status Pesanan", "Status Bayar", "Metode & Waktu", "Total"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateComboBoxes();
            }
        });

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
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Daftar Pesanan"));
        tablePanel.setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // =================== ASSEMBLE LAYOUT ===================
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setOpaque(false);
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

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

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.decode("#222222"));
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
        button.setPreferredSize(new Dimension(100, 32));
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
    private void filterTable() {
        String searchText = txtSearch.getText().trim();
        
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                RowFilter<DefaultTableModel, Object> rf = RowFilter.orFilter(
                    java.util.Arrays.asList(
                        RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0),
                        RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 2),
                        RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 3)
                    )
                );
                sorter.setRowFilter(rf);
            } catch (java.util.regex.PatternSyntaxException e) {
                sorter.setRowFilter(null);
            }
        }
        
        int visibleRows = table.getRowCount();
        int totalRows = model.getRowCount();
        
        if (visibleRows != totalRows) {
            setTitle(String.format("Status Pesanan & Pembayaran - Menampilkan %d dari %d data", visibleRows, totalRows));
        } else {
            setTitle("Status Pesanan & Pembayaran");
        }
    }
    
    private void clearSearch() {
        txtSearch.setText("");
        sorter.setRowFilter(null);
        setTitle("Status Pesanan & Pembayaran");
    }
    
    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.id_transaksi, t.tanggal_transaksi, p.nama, " +
                        "pk.nama as paket_nama, t.berat_kg, t.status_pesanan, t.total_biaya, " +
                        "t.pembayaran, " +
                        "CASE " +
                        "   WHEN t.pembayaran LIKE '%Bayar Sekarang%' THEN 'Lunas' " +
                        "   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.status_pesanan = 'selesai' THEN 'Belum Bayar' " +
                        "   WHEN t.pembayaran LIKE '%Bayar Setelah Selesai%' THEN 'Pending' " +
                        "   ELSE 'Lunas' " +
                        "END as status_bayar " +
                        "FROM transaksi t " +
                        "LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                        "LEFT JOIN paket pk ON t.id_jenis = pk.id " +
                        "WHERE t.status_pesanan != 'diambil' OR " +
                        "      (t.status_pesanan = 'selesai' AND t.pembayaran LIKE '%Bayar Setelah Selesai%' AND " +
                        "       t.pembayaran NOT LIKE '%LUNAS%') " +
                        "ORDER BY t.tanggal_transaksi DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                String statusBayar = rs.getString("status_bayar");
                String tingkatCuci = rs.getString("pembayaran");
                
                if (tingkatCuci != null && tingkatCuci.contains("LUNAS")) {
                    statusBayar = "Lunas";
                }
                
                Object[] row = {
                    rs.getInt("id_transaksi"),
                    sdf.format(rs.getTimestamp("tanggal_transaksi")),
                    rs.getString("nama"),
                    rs.getString("paket_nama"),
                    rs.getDouble("berat_kg") + " kg",
                    rs.getString("status_pesanan"),
                    statusBayar,
                    tingkatCuci != null ? tingkatCuci : "Cash - Bayar Sekarang",
                    "Rp " + String.format("%,.0f", rs.getDouble("total_biaya"))
                };
                model.addRow(row);
            }
            
            setTitle(String.format("Status Pesanan & Pembayaran - Total: %d data", model.getRowCount()));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void updateComboBoxes() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            
            String statusPesanan = (String) model.getValueAt(modelRow, 5);
            String statusBayar = (String) model.getValueAt(modelRow, 6);
            
            cmbStatus.setSelectedItem(statusPesanan);
            
            if (statusBayar.equals("Lunas")) {
                cmbStatusBayar.setSelectedIndex(1);
                cmbStatusBayar.setEnabled(false);
                cmbStatusBayar.setToolTipText("Pembayaran sudah lunas, tidak bisa diubah");
            } else {
                if (statusBayar.equals("Belum Bayar")) {
                    cmbStatusBayar.setSelectedIndex(0);
                } else {
                    cmbStatusBayar.setSelectedIndex(0);
                }
                cmbStatusBayar.setEnabled(true);
                cmbStatusBayar.setToolTipText(null);
            }
        }
    }
    
    private void updateSemuaStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pesanan untuk mengubah status!");
            return;
        }
    
        int modelRow = table.convertRowIndexToModel(selectedRow);
    
        int idTransaksi = (Integer) model.getValueAt(modelRow, 0);
        String statusPesananBaru = (String) cmbStatus.getSelectedItem();
        String statusPesananLama = (String) model.getValueAt(modelRow, 5);
        String statusBayarBaru = (String) cmbStatusBayar.getSelectedItem();
        String statusBayarLama = (String) model.getValueAt(modelRow, 6);
        String metodeLama = (String) model.getValueAt(modelRow, 7);
    
        boolean statusPesananBerubah = !statusPesananLama.equals(statusPesananBaru);
        boolean statusBayarBerubah = !statusBayarLama.equals(statusBayarBaru);
    
        // VALIDASI: Jika status pesanan = "diambil", maka status bayar HARUS "Lunas"
        if ("diambil".equals(statusPesananBaru) && !"Lunas".equals(statusBayarBaru)) {
            JOptionPane.showMessageDialog(this,
                "Untuk update status pesanan menjadi \"diambil\", status bayar harus di set \"Lunas\".",
                "Validasi",
                JOptionPane.WARNING_MESSAGE);
            return; // Hentikan proses, jangan lanjut update
        }
    
        // Jika status bayar lama sudah lunas, tidak bisa diubah
        if (statusBayarLama.equals("Lunas") && statusBayarBerubah) {
            JOptionPane.showMessageDialog(this,
                "Pembayaran sudah lunas!\nTidak bisa mengubah status pembayaran.",
                "Validasi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        if (!statusPesananBerubah && !statusBayarBerubah) {
            JOptionPane.showMessageDialog(this, "Tidak ada perubahan status!");
            return;
        }
    
        boolean updateTanggal = false;
    
        if (statusBayarBerubah && statusBayarBaru.equals("Lunas")) {
            updateTanggal = true;
        }
    
        StringBuilder confirmMsg = new StringBuilder("Konfirmasi perubahan:\n\n");
        if (statusPesananBerubah) {
            confirmMsg.append(String.format("Status Pesanan: %s → %s\n", statusPesananLama, statusPesananBaru));
        }
        if (statusBayarBerubah) {
            confirmMsg.append(String.format("Status Bayar: %s → %s\n", statusBayarLama, statusBayarBaru));
        }
    
        // Jika status bayar menjadi Lunas, tampilkan detail pesanan
        if (statusBayarBerubah && statusBayarBaru.equals("Lunas")) {
            confirmMsg.append("\n═══════════════════════════════════════════\n");
            confirmMsg.append("DETAIL PEMBAYARAN:\n");
            confirmMsg.append(String.format("ID Transaksi : %d\n", idTransaksi));
            confirmMsg.append(String.format("Pelanggan    : %s\n", model.getValueAt(modelRow, 2)));
            confirmMsg.append(String.format("Paket        : %s\n", model.getValueAt(modelRow, 3)));
            confirmMsg.append(String.format("Berat        : %s\n", model.getValueAt(modelRow, 4)));
            confirmMsg.append(String.format("Total Biaya  : %s\n", model.getValueAt(modelRow, 8)));
            confirmMsg.append("═══════════════════════════════════════════\n");
        
            // Tampilkan tanggal bayar
            confirmMsg.append(String.format("\n⚠️ Tanggal akan diupdate ke: %s\n", 
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date())));
        } else {
            confirmMsg.append("\n✓ Tanggal tetap (tidak diubah)\n");
        }
    
        confirmMsg.append("\nLanjutkan?");
    
        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg.toString(),
            "Konfirmasi Update",
            JOptionPane.YES_NO_OPTION);
    
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            String metodeBaruString = metodeLama;
        
            if (statusBayarBerubah && statusBayarBaru.equals("Lunas")) {
                if (!metodeLama.contains("LUNAS")) {
                    metodeBaruString = metodeLama + " - LUNAS";
                }
            
                if (metodeLama.contains("Bayar Setelah Selesai") && !metodeLama.contains("LUNAS")) {
                    String[] options = {"Cash", "QRIS"};
                    int choice = JOptionPane.showOptionDialog(
                        this,
                        "Pilih metode pembayaran:",
                        "Metode Pembayaran",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                    );
                
                    if (choice >= 0) {
                        metodeBaruString = options[choice] + " - Bayar Setelah Selesai - LUNAS";
                    } else {
                        return;
                    }
                }
            } else if (statusBayarBerubah && statusBayarBaru.equals("Belum Bayar")) {
                metodeBaruString = metodeLama.replace(" - LUNAS", "");
            }
        
            String sql;
            PreparedStatement pstmt;
        
            if (updateTanggal) {
                sql = "UPDATE transaksi SET status_pesanan = ?, pembayaran = ?, tanggal_transaksi = NOW() WHERE     id_transaksi = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, statusPesananBaru);
                pstmt.setString(2, metodeBaruString);
                pstmt.setInt(3, idTransaksi);
            } else {
                sql = "UPDATE transaksi SET status_pesanan = ?, pembayaran = ? WHERE id_transaksi = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, statusPesananBaru);
                pstmt.setString(2, metodeBaruString);
                pstmt.setInt(3, idTransaksi);
            }
        
            pstmt.executeUpdate();
        
            StringBuilder successMsg = new StringBuilder("✅ Update berhasil!\n\n");
            if (statusPesananBerubah) {
                successMsg.append(String.format("✓ Status Pesanan: %s → %s\n", statusPesananLama, statusPesananBaru));
            }
            if (statusBayarBerubah) {
                successMsg.append(String.format("✓ Status Bayar: %s → %s\n", statusBayarLama, statusBayarBaru));
            }
        
            if (updateTanggal) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                successMsg.append(String.format("✓ Tanggal diupdate ke: %s\n", sdf.format(new java.util.Date())));
            } else {
                successMsg.append("✓ Tanggal tetap (tidak diubah)\n");
            }
        
            JOptionPane.showMessageDialog(this, successMsg.toString(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        
            // Jika status bayar menjadi Lunas, tampilkan struk
            if (statusBayarBerubah && statusBayarBaru.equals("Lunas")) {
                showStrukPembayaran(idTransaksi);
            }
        
            loadData();
        
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showDetail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pesanan untuk melihat detail!");
            return;
        }
        
        int actualRow = table.convertRowIndexToModel(selectedRow);
        int idTransaksi = (Integer) model.getValueAt(actualRow, 0);
        
        JDialog detailDialog = new JDialog(this, "Detail Transaksi", true);
        detailDialog.setSize(500, 400);
        detailDialog.setLocationRelativeTo(this);
        
        JTextArea txtDetail = new JTextArea();
        txtDetail.setEditable(false);
        txtDetail.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetail.setMargin(new Insets(10, 10, 10, 10));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.*, p.nama as nama_pelanggan, pk.nama as paket_nama, u.username " +
                        "FROM transaksi t " +
                        "LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                        "LEFT JOIN paket pk ON t.id_jenis = pk.id " +
                        "LEFT JOIN user u ON t.id_user = u.id_user " +
                        "WHERE t.id_transaksi = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idTransaksi);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                SimpleDateFormat sdfDetail = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                StringBuilder detail = new StringBuilder();
                
                detail.append("╔═══════════════════════════════════════════╗\n");
                detail.append("║          DETAIL TRANSAKSI                 ║\n");
                detail.append("╚═══════════════════════════════════════════╝\n\n");
                detail.append(String.format("ID Transaksi : %d\n", rs.getInt("id_transaksi")));
                detail.append(String.format("Tanggal      : %s\n", sdfDetail.format(rs.getTimestamp("tanggal_transaksi"))));
                detail.append(String.format("Pelanggan    : %s\n", rs.getString("nama_pelanggan")));
                detail.append(String.format("Paket        : %s\n", rs.getString("paket_nama")));
                detail.append(String.format("Berat        : %.1f kg\n", rs.getDouble("berat_kg")));
                detail.append(String.format("Total Biaya  : Rp %,.0f\n", rs.getDouble("total_biaya")));
                detail.append(String.format("Status       : %s\n", rs.getString("status_pesanan")));
                detail.append(String.format("Kasir        : %s\n", rs.getString("username")));
                
                String tingkatCuci = rs.getString("pembayaran");
                if (tingkatCuci != null) {
                    detail.append(String.format("Pembayaran   : %s\n", tingkatCuci));
                }
                
                String addonIds = rs.getString("addon_ids");
                if (addonIds != null && !addonIds.trim().isEmpty()) {
                    detail.append("Addon        : ");
                    if (addonIds.contains("1")) detail.append("Detergen ");
                    if (addonIds.contains("2")) detail.append("Pewangi ");
                    detail.append("\n");
                }
                
                int voucherDidapat = rs.getInt("voucher_didapat");
                detail.append(String.format("Voucher +    : %d\n", voucherDidapat));
                
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

    private void showStrukPembayaran(int idTransaksi) {
        JDialog strukDialog = new JDialog(this, "Struk Transaksi", true);
        strukDialog.setSize(450, 600);
        strukDialog.setLocationRelativeTo(this);

        JTextArea txtStruk = new JTextArea();
        txtStruk.setEditable(false);
        txtStruk.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtStruk.setMargin(new Insets(10, 10, 10, 10));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.*, p.nama as nama_pelanggan, pk.harga as harga_kg, pk.nama as paket_nama, u.username " +
                         "FROM transaksi t " +
                         "LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                         "LEFT JOIN paket pk ON t.id_jenis = pk.id " +
                         "LEFT JOIN user u ON t.id_user = u.id_user " +
                         "WHERE t.id_transaksi = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idTransaksi);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                StringBuilder struk = new StringBuilder();
                struk.append("       ╔═══════════════════════════════════════════╗\n");
                struk.append("       ║              KAVI LAUNDRY            ║\n");
                struk.append("       ║            Jl. Contoh No. 123        ║\n");
                struk.append("       ║           Telp: 0812-3456-7890       ║\n");
                struk.append("       ╚═══════════════════════════════════════════╝\n\n");

                struk.append("=================== STRUK PEMBAYARAN ===================\n\n");
                struk.append(String.format("Tanggal      : %s\n", sdfDisplay.format(rs.getTimestamp("tanggal_transaksi"))));
                struk.append(String.format("Kasir        : %s\n", rs.getString("username")));
                struk.append("\n");

                struk.append("=================== RINCIAN BIAYA =====================\n\n");
                struk.append(String.format("Pelanggan    : %s\n", rs.getString("nama_pelanggan")));
                struk.append(String.format("Paket        : %s\n", rs.getString("paket_nama")));
                struk.append(String.format("Harga per kg : Rp %,.0f\n", rs.getDouble("harga_kg")));
                struk.append(String.format("Berat total  : %.1f kg\n", rs.getDouble("berat_kg")));
                struk.append(String.format("Biaya normal : Rp %,.0f\n", rs.getDouble("total_biaya")));
                struk.append("\n");

                struk.append("=================== TOTAL BAYAR =======================\n");
                struk.append(String.format("TOTAL BAYAR  : Rp %,.0f\n", rs.getDouble("total_biaya")));
                struk.append("\n");
                String metodeBayar = rs.getString("pembayaran");
                if (metodeBayar != null) {
                    if (metodeBayar.contains("Cash")) {
                        struk.append("Metode Bayar : Cash\n");
                    } else if (metodeBayar.contains("QRIS")) {
                        struk.append("Metode Bayar : QRIS\n");
                    } else {
                        struk.append("Metode Bayar : " + metodeBayar + "\n");
                    }
                }

                struk.append("\n");
                struk.append("=======================================================\n");
                struk.append("Terima kasih atas kepercayaan\n");
                struk.append("Anda kepada kami\n");
                struk.append("=======================================================\n");

                txtStruk.setText(struk.toString());
            }
        } catch (SQLException e) {
            txtStruk.setText("Error loading receipt: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(txtStruk);
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnPrint = new JButton("Print");
        btnPrint.addActionListener(e -> {
            try {
                txtStruk.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(strukDialog, "Gagal print: " + ex.getMessage());
            }
        });

        JButton btnClose = new JButton("Tutup");
        btnClose.addActionListener(e -> strukDialog.dispose());

        buttonPanel.add(btnPrint);
        buttonPanel.add(btnClose);

        strukDialog.add(scrollPane, BorderLayout.CENTER);
        strukDialog.add(buttonPanel, BorderLayout.SOUTH);
        strukDialog.setVisible(true);
    }
}