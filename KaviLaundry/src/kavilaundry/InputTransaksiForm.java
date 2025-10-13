package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import com.toedter.calendar.JDateChooser;

public class InputTransaksiForm extends JFrame {
    private JTextField txtNamaPelanggan, txtBerat, txtid;
    private JComboBox<String> cmbPaket, cmbWaktuBayar, cmbMetodePembayaran;
    private JCheckBox chkVoucherDigunakan;
    private JTextArea txtRincian;
    private JLabel lblTotal, lblVoucherInfo;
    private JButton btnHitung, btnSimpan, btnCetak, btnTutup;
    private JDateChooser dateAmbil;
    
    private List<PaketLayanan> paketList = new ArrayList<>();
    private double totalBiaya = 0;
    private int idPelanggan = 0;
    private int currentTransactionId = 0;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public InputTransaksiForm() {
        setUndecorated(true);
        initComponents();
        loadPaketData();
        generateNextTransactionId();
        setLocationRelativeTo(null);
        updateWindowShape();
    }
    
    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color buttonBg = Color.decode("#6da395");
        Color cetakBg = Color.decode("#4A90E2");
        Color tableBg = Color.WHITE;

        setSize(650, 700);
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
        titleBar.setPreferredSize(new Dimension(650, 40));
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

        JLabel titleLabel = new JLabel("Input Transaksi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setOpaque(false);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalGlue());

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== FORM PANEL — TRANSPARAN ===================
        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // ID Transaksi (readonly)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("ID Transaksi:"), gbc);
        gbc.gridx = 1;
        txtid = createStyledTextField(20);
        txtid.setEditable(false);
        txtid.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtid, gbc);
        
        // Nama Pelanggan
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Nama Pelanggan:"), gbc);
        gbc.gridx = 1;
        txtNamaPelanggan = createStyledTextField(20);
        formPanel.add(txtNamaPelanggan, gbc);
        
        // Paket
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Paket Layanan:"), gbc);
        gbc.gridx = 1;
        cmbPaket = new JComboBox<>();
        styleComboBox(cmbPaket);
        cmbPaket.addActionListener(e -> {
            updateVoucherVisibility();
            updateEstimasiTanggalAmbil();
        });
        formPanel.add(cmbPaket, gbc);
        
        // Berat
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Berat (kg):"), gbc);
        gbc.gridx = 1;
        txtBerat = createStyledTextField(20);
        txtBerat.setText("1");
        formPanel.add(txtBerat, gbc);
        
        // Tanggal Ambil
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Tanggal Ambil:"), gbc);
        gbc.gridx = 1;
        dateAmbil = new JDateChooser();
        dateAmbil.setDateFormatString("dd/MM/yyyy");
        dateAmbil.setPreferredSize(new Dimension(200, 25));
        styleDateChooser(dateAmbil);
        
        Calendar minCal = Calendar.getInstance();
        minCal.add(Calendar.DAY_OF_MONTH, 1);
        minCal.set(Calendar.HOUR_OF_DAY, 0);
        minCal.set(Calendar.MINUTE, 0);
        minCal.set(Calendar.SECOND, 0);
        minCal.set(Calendar.MILLISECOND, 0);
        dateAmbil.setMinSelectableDate(minCal.getTime());
        
        Calendar defaultCal = Calendar.getInstance();
        defaultCal.add(Calendar.DAY_OF_MONTH, 3);
        dateAmbil.setDate(defaultCal.getTime());
        
        formPanel.add(dateAmbil, gbc);
        
        // Voucher dengan info
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Gunakan Voucher:"), gbc);
        gbc.gridx = 1;
        JPanel voucherPanel = new JPanel(new BorderLayout());
        voucherPanel.setOpaque(false);
        
        chkVoucherDigunakan = new JCheckBox("Gunakan 7 Voucher (Gratis 7kg)") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isSelected()) {
                    setBackground(new Color(0, 0, 0, 0));
                }
            }
        };
        chkVoucherDigunakan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkVoucherDigunakan.setForeground(textMain);
        chkVoucherDigunakan.setOpaque(false);
        chkVoucherDigunakan.setEnabled(false);
        
        lblVoucherInfo = new JLabel("Voucher tersedia: 0");
        lblVoucherInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblVoucherInfo.setForeground(Color.BLUE);
        
        voucherPanel.add(chkVoucherDigunakan, BorderLayout.NORTH);
        voucherPanel.add(lblVoucherInfo, BorderLayout.SOUTH);
        formPanel.add(voucherPanel, gbc);
        
        // Waktu Pembayaran
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Waktu Pembayaran:"), gbc);
        gbc.gridx = 1;
        cmbWaktuBayar = new JComboBox<>(new String[]{"Bayar Sekarang", "Bayar Setelah Selesai"});
        styleComboBox(cmbWaktuBayar);
        formPanel.add(cmbWaktuBayar, gbc);
        
        // Metode Pembayaran
        JLabel lblMetodePembayaran = new JLabel("Metode Pembayaran:");
        lblMetodePembayaran.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMetodePembayaran.setForeground(textMain);
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(lblMetodePembayaran, gbc);
        gbc.gridx = 1;
        cmbMetodePembayaran = new JComboBox<>(new String[]{"Cash", "QRIS"});
        styleComboBox(cmbMetodePembayaran);
        cmbMetodePembayaran.setSelectedIndex(-1);
        formPanel.add(cmbMetodePembayaran, gbc);
        
        // Listener untuk cmbWaktuBayar
        cmbWaktuBayar.addActionListener(e -> {
            String pilihan = (String) cmbWaktuBayar.getSelectedItem();
            if ("Bayar Sekarang".equals(pilihan)) {
                lblMetodePembayaran.setVisible(true);
                cmbMetodePembayaran.setVisible(true);
            } else {
                lblMetodePembayaran.setVisible(false);
                cmbMetodePembayaran.setVisible(false);
            }
            formPanel.revalidate();
            formPanel.repaint();
        });
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        btnHitung = createActionButton("Hitung Total", buttonBg);
        btnSimpan = createActionButton("Simpan", Color.decode("#FFA500"));
        btnCetak = createActionButton("Cetak Struk", cetakBg);
        btnTutup = createActionButton("Tutup", Color.decode("#AAAAAA"));

        btnHitung.addActionListener(e -> hitungTotal());
        btnSimpan.addActionListener(e -> simpanTransaksi());
        btnCetak.addActionListener(e -> cetakStruk());
        btnTutup.addActionListener(e -> dispose());

        btnPanel.add(btnHitung);
        btnPanel.add(btnSimpan);
        btnPanel.add(btnCetak);
        btnPanel.add(btnTutup);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // =================== RINCIAN PANEL ===================
        JPanel rincianPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        rincianPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        rincianPanel.setOpaque(false);

        txtRincian = new JTextArea(8, 40);
        txtRincian.setEditable(false);
        txtRincian.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtRincian.setBackground(Color.WHITE);
        JScrollPane scrollRincian = new JScrollPane(txtRincian);
        scrollRincian.setBorder(BorderFactory.createEmptyBorder());
        scrollRincian.setOpaque(false);
        scrollRincian.getViewport().setOpaque(false);

        lblTotal = new JLabel("TOTAL: Rp 0", SwingConstants.CENTER);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setOpaque(true);
        lblTotal.setBackground(Color.YELLOW);
        lblTotal.setForeground(Color.BLACK);

        // Wrap rincian dalam rounded panel
        JPanel rincianWrapper = new JPanel(new BorderLayout()) {
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
        rincianWrapper.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Rincian Biaya"));
        rincianWrapper.setOpaque(false);
        rincianWrapper.add(scrollRincian, BorderLayout.CENTER);
        rincianWrapper.add(lblTotal, BorderLayout.SOUTH);

        rincianPanel.add(rincianWrapper, BorderLayout.CENTER);
        mainPanel.add(rincianPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        btnSimpan.setEnabled(false);
        btnCetak.setEnabled(false);
        
        txtNamaPelanggan.addActionListener(e -> updateVoucherInfo());
        txtNamaPelanggan.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateVoucherInfo();
            }
        });

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

    private void styleDateChooser(JDateChooser chooser) {
        JFormattedTextField textField = (JFormattedTextField) chooser.getDateEditor().getUiComponent();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.decode("#222222"));
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
        button.setPreferredSize(new Dimension(110, 32));
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
    // ... (semua method logika seperti loadPaketData, hitungTotal, simpanTransaksi, dll tetap sama seperti kode asli Anda)
    
    private void updateEstimasiTanggalAmbil() {
        if (cmbPaket.getSelectedIndex() < 0 || paketList.isEmpty()) {
            return;
        }
        
        PaketLayanan selectedPaket = paketList.get(cmbPaket.getSelectedIndex());
        String kapasitas = selectedPaket.kapasitas.toLowerCase();
        
        Calendar cal = Calendar.getInstance();
        
        if (kapasitas.contains("express") || kapasitas.contains("kilat")) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else if (kapasitas.contains("reguler")) {
            cal.add(Calendar.DAY_OF_MONTH, 3);
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 3);
        }
        
        Calendar minCal = Calendar.getInstance();
        minCal.add(Calendar.DAY_OF_MONTH, 1);
        
        if (cal.before(minCal)) {
            cal = minCal;
        }
        
        dateAmbil.setDate(cal.getTime());
    }
    
    private void generateNextTransactionId() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COALESCE(MAX(id_transaksi), 0) + 1 as next_id FROM transaksi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                currentTransactionId = rs.getInt("next_id");
                txtid.setText(String.valueOf(currentTransactionId));
            }
        } catch (SQLException e) {
            currentTransactionId = (int) (System.currentTimeMillis() % 1000000);
            txtid.setText(String.valueOf(currentTransactionId));
        }
    }
    
    private void updateVoucherVisibility() {
        if (cmbPaket.getSelectedIndex() < 0 || paketList.isEmpty()) {
            return;
        }
        
        PaketLayanan selectedPaket = paketList.get(cmbPaket.getSelectedIndex());
        String paketNama = selectedPaket.nama.toLowerCase();
        
        boolean canUseVoucher = (paketNama.contains("cuci")) && !paketNama.equals("setrika");
        
        chkVoucherDigunakan.setEnabled(canUseVoucher);
        chkVoucherDigunakan.setSelected(false);
        
        if (!canUseVoucher) {
            lblVoucherInfo.setText("Voucher tidak tersedia untuk paket ini");
            lblVoucherInfo.setForeground(Color.GRAY);
        } else {
            lblVoucherInfo.setForeground(Color.BLUE);
            updateVoucherInfo();
        }
    }
    
    private void updateVoucherInfo() {
        String namaPelanggan = txtNamaPelanggan.getText().trim();
        if (namaPelanggan.isEmpty() || !chkVoucherDigunakan.isEnabled()) {
            return;
        }
        
        try {
            int voucherTersedia = 0;
            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkSql = "SELECT total_voucher FROM pelanggan WHERE nama = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, namaPelanggan);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    voucherTersedia = rs.getInt("total_voucher");
                }
            }
            
            lblVoucherInfo.setText("Voucher tersedia: " + voucherTersedia);
            if (voucherTersedia < 7) {
                lblVoucherInfo.setForeground(Color.RED);
                chkVoucherDigunakan.setText("Gunakan 7 Voucher (Kurang " + (7 - voucherTersedia) + " voucher)");
                chkVoucherDigunakan.setEnabled(false);
            } else {
                lblVoucherInfo.setForeground(Color.GREEN);
                chkVoucherDigunakan.setText("Gunakan 7 Voucher (Gratis 7kg)");
                chkVoucherDigunakan.setEnabled(true);
            }
        } catch (SQLException e) {
            lblVoucherInfo.setText("Error checking voucher");
        }
    }
    
    private void loadPaketData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM paket ORDER BY nama, kapasitas";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            paketList.clear();
            cmbPaket.removeAllItems();
            
            while (rs.next()) {
                PaketLayanan paket = new PaketLayanan(
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kapasitas"),
                    rs.getInt("harga"),
                    rs.getString("keterangan")
                );
                paketList.add(paket);
                cmbPaket.addItem(paket.toString());
            }
            
            if (cmbPaket.getItemCount() > 0) {
                cmbPaket.setSelectedIndex(0);
                updateVoucherVisibility();
                updateEstimasiTanggalAmbil();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading paket: " + e.getMessage());
        }
    }
    
    private void hitungTotal() {
        try {
            String namaPelanggan = txtNamaPelanggan.getText().trim();
            if (namaPelanggan.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama pelanggan harus diisi!");
                return;
            }
            
            int selectedPaketIndex = cmbPaket.getSelectedIndex();
            if (selectedPaketIndex < 0) {
                JOptionPane.showMessageDialog(this, "Pilih paket layanan!");
                return;
            }
            
            if (dateAmbil.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Tanggal ambil harus diisi!");
                return;
            }
            
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTime(dateAmbil.getDate());
            selectedDate.set(Calendar.HOUR_OF_DAY, 0);
            selectedDate.set(Calendar.MINUTE, 0);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            
            if (!selectedDate.after(today)) {
                JOptionPane.showMessageDialog(this, 
                    "Tanggal ambil harus minimal besok!\nLaundry tidak bisa selesai di hari yang sama.", 
                    "Tanggal Tidak Valid", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            PaketLayanan selectedPaket = paketList.get(selectedPaketIndex);
            double berat = Double.parseDouble(
                txtBerat.getText().trim().replace(",", ".")
            );

            
            if (berat <= 0) {
                JOptionPane.showMessageDialog(this, "Berat harus lebih dari 0!");
                return;
            }
            
            idPelanggan = getOrCreatePelanggan(namaPelanggan);
            
            double biayaDasar = selectedPaket.harga * berat;
            
            boolean voucherDigunakan = chkVoucherDigunakan.isSelected();
            double beratBayar = berat;
            double diskonVoucher = 0;
            
            if (voucherDigunakan) {
                int voucherTersedia = getVoucherPelanggan(idPelanggan);
                if (voucherTersedia < 7) {
                    JOptionPane.showMessageDialog(this, "Voucher tidak mencukupi! Tersedia: " + voucherTersedia + ", Dibutuhkan: 7");
                    chkVoucherDigunakan.setSelected(false);
                    return;
                }
                
                beratBayar = Math.max(0, berat - 7);
                diskonVoucher = Math.min(7, berat) * selectedPaket.harga;
            }
            
            totalBiaya = beratBayar * selectedPaket.harga;
            
            String metodePembayaran = (String) cmbMetodePembayaran.getSelectedItem();
            String waktuBayar = (String) cmbWaktuBayar.getSelectedItem();
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            String tanggalAmbilStr = sdf.format(dateAmbil.getDate());
            
            StringBuilder rincian = new StringBuilder();
            rincian.append("                  RINCIAN BIAYA                  \n");
            rincian.append("=================================================\n");
            rincian.append(String.format("Pelanggan       : %s\n", namaPelanggan));
            rincian.append(String.format("Paket           : %s\n", selectedPaket.nama));
            rincian.append(String.format("Harga per kg    : Rp %,.0f\n", (double)selectedPaket.harga));
            rincian.append(String.format("Berat total     : %.1f kg\n", berat));
            rincian.append(String.format("Tanggal Ambil   : %s\n", tanggalAmbilStr));
            rincian.append(String.format("Biaya normal    : Rp %,.0f\n", biayaDasar));
            
            if (voucherDigunakan) {
                rincian.append("------------------------------------------------\n");
                rincian.append("VOUCHER DIGUNAKAN:\n");
                rincian.append(String.format("Gratis 7kg      : -Rp %,.0f\n", diskonVoucher));
                rincian.append(String.format("Berat bayar     : %.1f kg\n", beratBayar));
                rincian.append("(7 voucher digunakan)\n");
            }
            
            rincian.append("=================================================\n");
            rincian.append(String.format("TOTAL BAYAR     : Rp %,.0f\n", totalBiaya));
            rincian.append("=================================================\n");
            if (waktuBayar.equals("Bayar Setelah Selesai")) {
                rincian.append(String.format("Waktu Bayar     : %s\n", waktuBayar));
            }  
            if (metodePembayaran != null && !metodePembayaran.isEmpty()) {
                rincian.append(String.format("Metode Bayar    : %s\n", metodePembayaran));
            }
            
            txtRincian.setText(rincian.toString());
            lblTotal.setText(String.format("TOTAL: Rp %,.0f", totalBiaya));
            
            btnSimpan.setEnabled(true);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Berat harus berupa angka!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private int getOrCreatePelanggan(String nama) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT id_pelanggan FROM pelanggan WHERE nama = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nama);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_pelanggan");
            } else {
                String insertSql = "INSERT INTO pelanggan (nama, total_voucher) VALUES (?, 0)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, nama);
                insertStmt.executeUpdate();
                
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Gagal membuat pelanggan baru");
    }
    
    private int getVoucherPelanggan(int idPelanggan) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT total_voucher FROM pelanggan WHERE id_pelanggan = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idPelanggan);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total_voucher");
            }
        }
        return 0;
    }
    
    private void simpanTransaksi() {
        try {
            if (totalBiaya == 0 && !chkVoucherDigunakan.isSelected()) {
                JOptionPane.showMessageDialog(this, "Hitung total terlebih dahulu!");
                return;
            }
            
            if (dateAmbil.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Tanggal ambil harus diisi!");
                return;
            }
            
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTime(dateAmbil.getDate());
            selectedDate.set(Calendar.HOUR_OF_DAY, 0);
            selectedDate.set(Calendar.MINUTE, 0);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            
            if (!selectedDate.after(today)) {
                JOptionPane.showMessageDialog(this, 
                    "Tanggal ambil harus minimal besok!\nLaundry tidak bisa selesai di hari yang sama.", 
                    "Tanggal Tidak Valid", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int selectedPaketIndex = cmbPaket.getSelectedIndex();
            PaketLayanan selectedPaket = paketList.get(selectedPaketIndex);
            double berat = Double.parseDouble(
                txtBerat.getText().trim().replace(",", ".")
            );

            boolean voucherDigunakan = chkVoucherDigunakan.isSelected();
            String metodePembayaran = (String) cmbMetodePembayaran.getSelectedItem();
            String waktuBayar = (String) cmbWaktuBayar.getSelectedItem();
            String value4;
            if (metodePembayaran == null || metodePembayaran.isEmpty()) {
                value4 = waktuBayar;
            } else {
                value4 = metodePembayaran + " - " + waktuBayar;
            }
            
            java.sql.Date sqlDateAmbil = new java.sql.Date(dateAmbil.getDate().getTime());
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                String insertTransaksiSql = "INSERT INTO transaksi (id_pelanggan, id_jenis, berat_kg, " + 
                                          "pembayaran, total_biaya, voucher_didapat, status_pesanan, id_user, tanggal_ambil) " +
                                          "VALUES (?, ?, ?, ?, ?, ?, 'diterima', ?, ?)";
                
                PreparedStatement pstmt = conn.prepareStatement(insertTransaksiSql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, idPelanggan);
                pstmt.setInt(2, selectedPaket.id);
                pstmt.setDouble(3, berat);
                pstmt.setString(4, value4);
                pstmt.setDouble(5, totalBiaya);
                pstmt.setInt(6, 1);
                pstmt.setInt(7, UserSession.getCurrentUserId());
                pstmt.setDate(8, sqlDateAmbil);
                
                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                int actualIdTransaksi = 0;
                if (generatedKeys.next()) {
                    actualIdTransaksi = generatedKeys.getInt(1);
                    txtid.setText(String.valueOf(actualIdTransaksi));
                }
                
                if (voucherDigunakan) {
                    String updateVoucherSql = "UPDATE pelanggan SET total_voucher = total_voucher - 7 WHERE id_pelanggan = ?";
                    try (PreparedStatement updateVoucherStmt = conn.prepareStatement(updateVoucherSql)) {
                        updateVoucherStmt.setInt(1, idPelanggan);
                        updateVoucherStmt.executeUpdate();
                    }
                } else {
                    String updateVoucherSql = "UPDATE pelanggan SET total_voucher = total_voucher + 1 WHERE id_pelanggan = ?";
                    try (PreparedStatement updateVoucherStmt = conn.prepareStatement(updateVoucherSql)) {
                        updateVoucherStmt.setInt(1, idPelanggan);
                        updateVoucherStmt.executeUpdate();
                    }
                }
                
                conn.commit();
                
                clearForSimpan();
                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!\nID Transaksi: " + actualIdTransaksi);
                btnCetak.setEnabled(true);
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error menyimpan transaksi: " + e.getMessage());
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForSimpan() {
        txtid.setText("");
        txtNamaPelanggan.setText("");
        if (cmbPaket.getItemCount() > 0) {
            cmbPaket.setSelectedIndex(0);
            updateVoucherVisibility();
            updateEstimasiTanggalAmbil();
        }
        txtBerat.setText("1");
        chkVoucherDigunakan.setSelected(false);
        cmbMetodePembayaran.setSelectedIndex(-1);
        cmbWaktuBayar.setSelectedIndex(0);
    }
    
    private void clearForm() {
        txtid.setText("");
        txtNamaPelanggan.setText("");
        if (cmbPaket.getItemCount() > 0) {
            cmbPaket.setSelectedIndex(0);
            updateVoucherVisibility();
            updateEstimasiTanggalAmbil();
        }
        txtBerat.setText("1");
        chkVoucherDigunakan.setSelected(false);
        cmbMetodePembayaran.setSelectedIndex(-1);
        cmbWaktuBayar.setSelectedIndex(0);
        txtRincian.setText("");
        lblTotal.setText("TOTAL: Rp 0");
        lblVoucherInfo.setText("Voucher tersedia: 0");
        totalBiaya = 0;
        idPelanggan = 0;
        btnSimpan.setEnabled(false);
        btnCetak.setEnabled(false);
        
        generateNextTransactionId();
    }
    
    private void cetakStruk() {
        if (totalBiaya == 0 && !chkVoucherDigunakan.isSelected()) {
            JOptionPane.showMessageDialog(this, "Belum ada transaksi untuk dicetak!");
            return;
        }
        
        JDialog strukDialog = new JDialog(this, "Struk Transaksi", true);
        strukDialog.setSize(400, 550);
        strukDialog.setLocationRelativeTo(this);
        
        JTextArea txtStruk = new JTextArea();
        txtStruk.setEditable(false);
        txtStruk.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        StringBuilder struk = new StringBuilder();
        struk.append("                   KAVI LAUNDRY                  \n");
        struk.append("                Jl. Contoh No. 123               \n");
        struk.append("               Telp: 0812-3456-7890              \n");
        struk.append("=================================================\n");
        struk.append("                 STRUK PEMBAYARAN                \n");
        struk.append("=================================================\n");
        struk.append(String.format("Tanggal  : %s\n", new java.util.Date()));
        struk.append(String.format("Kasir    : %s\n", UserSession.getCurrentUsername()));
        struk.append("=================================================\n");
        struk.append(txtRincian.getText());
        struk.append("\n");
        
        struk.append("* Anda mendapat 1 voucher dari transaksi ini    \n");
        struk.append("* Kumpulkan 7 voucher untuk gratis cuci 7kg     \n");
        
        String waktuBayar = (String) cmbWaktuBayar.getSelectedItem();
        if (waktuBayar.equals("Bayar Setelah Selesai")) {
            struk.append("\n");
            struk.append("** PEMBAYARAN DITUNDA **\n");
            struk.append("Bayar saat pengambilan laundry selesai\n");
        }
        
        struk.append("\n");
        struk.append("          Terima kasih atas kepercayaan          \n");
        struk.append("                 Anda kepada kami                \n");
        struk.append("=================================================\n");
        struk.append("* Simpan struk ini guna bukti pengambilan laundry\n");
        
        txtStruk.setText(struk.toString());
        
        JScrollPane scrollPane = new JScrollPane(txtStruk);
        JButton btnPrint = new JButton("Print");
        JButton btnClose = new JButton("Tutup");
        
        btnPrint.addActionListener(e -> {
            try {
                txtStruk.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(strukDialog, "Error printing: " + ex.getMessage());
            }
        });
        
        btnClose.addActionListener(e -> {
            strukDialog.dispose();
            clearForm();
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);
        
        strukDialog.add(scrollPane, BorderLayout.CENTER);
        strukDialog.add(btnPanel, BorderLayout.SOUTH);
        strukDialog.setVisible(true);
    }
    
    private class PaketLayanan {
        int id;
        String nama;
        String kapasitas;
        int harga;
        String keterangan;
        
        public PaketLayanan(int id, String nama, String kapasitas, int harga, String keterangan) {
            this.id = id;
            this.nama = nama;
            this.kapasitas = kapasitas;
            this.harga = harga;
            this.keterangan = keterangan;
        }
        
        @Override
        public String toString() {
            return nama + " - Rp " + String.format("%,d", harga) + "/kg";
        }
    }
}