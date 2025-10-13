package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import com.toedter.calendar.JDateChooser;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class LaporanKeuanganForm extends JFrame {
    private JDateChooser dateFrom, dateTo;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTotalPendapatan, lblTotalTransaksi, lblTotalCash, lblTotalQRIS;
    private JButton btnFilter, btnExport, btnTutup;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public LaporanKeuanganForm() {
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
        Color exportBg = Color.decode("#4A90E2"); // Biru untuk export
        Color tableBg = Color.WHITE;

        setSize(1000, 700);
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
        titleBar.setPreferredSize(new Dimension(1000, 40));
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

        JLabel titleLabel = new JLabel("Laporan Keuangan", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setOpaque(false);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalGlue());

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== PANEL FILTER ===================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        filterPanel.setOpaque(false);

        filterPanel.add(createLabel("Dari:"));
        dateFrom = new JDateChooser();
        dateFrom.setPreferredSize(new Dimension(120, 25));
        dateFrom.setDate(new Date());
        styleDateChooser(dateFrom);
        filterPanel.add(dateFrom);

        filterPanel.add(createLabel("Sampai:"));
        dateTo = new JDateChooser();
        dateTo.setPreferredSize(new Dimension(120, 25));
        dateTo.setDate(new Date());
        styleDateChooser(dateTo);
        filterPanel.add(dateTo);

        dateFrom.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                validateDateRange();
            }
        });

        dateTo.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                validateDateRange();
            }
        });

        btnFilter = createActionButton("Filter", buttonBg);
        btnExport = createActionButton("Export Excel", exportBg);
        btnTutup = createActionButton("Tutup", Color.decode("#AAAAAA"));

        btnFilter.addActionListener(e -> loadDataByDateRange());
        btnExport.addActionListener(e -> exportToExcel());
        btnTutup.addActionListener(e -> dispose());

        filterPanel.add(btnFilter);
        filterPanel.add(btnExport);
        filterPanel.add(btnTutup);

        // Wrap filterPanel in a titled border panel
        JPanel filterWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        filterWrapper.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Filter Tanggal"));
        filterWrapper.setOpaque(false);
        filterWrapper.add(filterPanel, BorderLayout.CENTER);

        // =================== SUMMARY PANEL ===================
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Ringkasan"));

        lblTotalPendapatan = new JLabel("Total Pendapatan: Rp 0", SwingConstants.CENTER);
        lblTotalPendapatan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalPendapatan.setOpaque(true);
        lblTotalPendapatan.setBackground(Color.GREEN);
        lblTotalPendapatan.setForeground(Color.BLACK);

        lblTotalTransaksi = new JLabel("Total Transaksi: 0", SwingConstants.CENTER);
        lblTotalTransaksi.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalTransaksi.setOpaque(true);
        lblTotalTransaksi.setBackground(Color.CYAN);
        lblTotalTransaksi.setForeground(Color.BLACK);

        lblTotalCash = new JLabel("Cash: Rp 0", SwingConstants.CENTER);
        lblTotalCash.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalCash.setOpaque(true);
        lblTotalCash.setBackground(new Color(255, 165, 0));
        lblTotalCash.setForeground(Color.BLACK);

        lblTotalQRIS = new JLabel("QRIS: Rp 0", SwingConstants.CENTER);
        lblTotalQRIS.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalQRIS.setOpaque(true);
        lblTotalQRIS.setBackground(Color.MAGENTA);
        lblTotalQRIS.setForeground(Color.BLACK);

        summaryPanel.add(lblTotalPendapatan);
        summaryPanel.add(lblTotalTransaksi);
        summaryPanel.add(lblTotalCash);
        summaryPanel.add(lblTotalQRIS);

        // =================== TABLE ===================
        String[] columns = {"Tanggal", "ID Transaksi", "Pelanggan", "Paket", "Berat", "Total", "Metode Bayar", "Status", "Kasir"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(buttonBg);
        table.setSelectionForeground(Color.WHITE);

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

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

        // =================== ASSEMBLE LAYOUT ===================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(filterWrapper, BorderLayout.NORTH);
        topPanel.add(summaryPanel, BorderLayout.SOUTH);

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
    private void validateDateRange() {
        Date fromDate = dateFrom.getDate();
        Date toDate = dateTo.getDate();
        
        if (fromDate != null && toDate != null) {
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(fromDate);
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.MINUTE, 0);
            calFrom.set(Calendar.SECOND, 0);
            calFrom.set(Calendar.MILLISECOND, 0);
            
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(toDate);
            calTo.set(Calendar.HOUR_OF_DAY, 0);
            calTo.set(Calendar.MINUTE, 0);
            calTo.set(Calendar.SECOND, 0);
            calTo.set(Calendar.MILLISECOND, 0);
            
            if (calTo.before(calFrom)) {
                dateTo.setMinSelectableDate(calFrom.getTime());
                dateTo.setDate(calFrom.getTime());
            } else {
                dateTo.setMinSelectableDate(calFrom.getTime());
            }
        }
    }
    
    private void loadData() {
        loadDataByDateRange();
    }
    
    private void loadDataByDateRange() {
        Date fromDate = dateFrom.getDate();
        Date toDate = dateTo.getDate();
        
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal dari dan sampai!");
            return;
        }
        
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(fromDate);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calFrom.set(Calendar.MILLISECOND, 0);
        
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(toDate);
        calTo.set(Calendar.HOUR_OF_DAY, 0);
        calTo.set(Calendar.MINUTE, 0);
        calTo.set(Calendar.SECOND, 0);
        calTo.set(Calendar.MILLISECOND, 0);
        
        if (calTo.before(calFrom)) {
            JOptionPane.showMessageDialog(this, 
                "Tanggal 'Sampai' tidak boleh lebih kecil dari tanggal 'Dari'!", 
                "Periode Tidak Valid", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        model.setRowCount(0);
        double totalPendapatan = 0;
        double totalCash = 0;
        double totalQRIS = 0;
        int totalTransaksi = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.tanggal_transaksi, t.id_transaksi, p.nama, " +
                        "pk.nama as paket_nama, t.berat_kg, t.total_biaya, " +
                        "t.status_pesanan, u.username, t.pembayaran, " +
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
                        "WHERE DATE(t.tanggal_transaksi) BETWEEN ? AND ? " +
                        "AND NOT (t.pembayaran LIKE '%Bayar Setelah Selesai%' AND t.pembayaran NOT LIKE '%LUNAS%') " +
                        "ORDER BY t.tanggal_transaksi DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, new java.sql.Date(fromDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(toDate.getTime()));
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                double biaya = rs.getDouble("total_biaya");
                String statusBayar = rs.getString("status_bayar");
                String tingkatCuci = rs.getString("pembayaran");
                
                if ("Lunas".equals(statusBayar)) {
                    totalPendapatan += biaya;
                    totalTransaksi++;
                    
                    if (tingkatCuci != null && tingkatCuci.contains("QRIS")) {
                        totalQRIS += biaya;
                    } else {
                        totalCash += biaya;
                    }
                }
                
                String metodeBayar = "Cash - Bayar Sekarang";
                if (tingkatCuci != null) {
                    metodeBayar = tingkatCuci.replace(" - LUNAS", "");
                }
                
                Object[] row = {
                    sdf.format(rs.getTimestamp("tanggal_transaksi")),
                    rs.getInt("id_transaksi"),
                    rs.getString("nama"),
                    rs.getString("paket_nama"),
                    rs.getDouble("berat_kg") + " kg",
                    "Rp " + String.format("%,.0f", biaya),
                    metodeBayar,
                    statusBayar,
                    rs.getString("username")
                };
                model.addRow(row);
            }
            
            lblTotalPendapatan.setText("Total Pendapatan: Rp " + String.format("%,.0f", totalPendapatan));
            lblTotalTransaksi.setText("Total Transaksi: " + totalTransaksi);
            lblTotalCash.setText("Cash: Rp " + String.format("%,.0f", totalCash));
            lblTotalQRIS.setText("QRIS: Rp " + String.format("%,.0f", totalQRIS));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void exportToExcel() {
        Date fromDate = dateFrom.getDate();
        Date toDate = dateTo.getDate();
        
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal dari dan sampai terlebih dahulu!");
            return;
        }
        
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(fromDate);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calFrom.set(Calendar.MILLISECOND, 0);
        
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(toDate);
        calTo.set(Calendar.HOUR_OF_DAY, 0);
        calTo.set(Calendar.MINUTE, 0);
        calTo.set(Calendar.SECOND, 0);
        calTo.set(Calendar.MILLISECOND, 0);
        
        if (calTo.before(calFrom)) {
            JOptionPane.showMessageDialog(this, 
                "Tanggal 'Sampai' tidak boleh lebih kecil dari tanggal 'Dari'!", 
                "Periode Tidak Valid", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan Keuangan");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String defaultFileName = "laporan_keuangan_" + sdf.format(new Date()) + ".csv";
            fileChooser.setSelectedFile(new java.io.File(defaultFileName));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                
                try (java.io.PrintWriter writer = new java.io.PrintWriter(fileToSave)) {
                    writer.println("LAPORAN KEUANGAN KAVI LAUNDRY");
                    writer.println("Periode: " + new SimpleDateFormat("dd/MM/yyyy").format(dateFrom.getDate()) + 
                                  " - " + new SimpleDateFormat("dd/MM/yyyy").format(dateTo.getDate()));
                    writer.println();
                    writer.println("Tanggal,ID Transaksi,Pelanggan,Paket,Berat,Total,Metode Bayar,Status,Kasir");
                    
                    for (int i = 0; i < model.getRowCount(); i++) {
                        StringBuilder line = new StringBuilder();
                        for (int j = 0; j < model.getColumnCount(); j++) {
                            if (j > 0) line.append(",");
                            Object value = model.getValueAt(i, j);
                            line.append("\"").append(value != null ? value.toString() : "").append("\"");
                        }
                        writer.println(line.toString());
                    }
                    
                    writer.println();
                    writer.println("RINGKASAN");
                    writer.println(lblTotalPendapatan.getText().replace("Total Pendapatan: ", "Total Pendapatan,"));
                    writer.println(lblTotalTransaksi.getText().replace("Total Transaksi: ", "Total Transaksi,"));
                    writer.println();
                    writer.println("BREAKDOWN METODE PEMBAYARAN");
                    writer.println(lblTotalCash.getText().replace("Cash: ", "Pembayaran Tunai,"));
                    writer.println(lblTotalQRIS.getText().replace("QRIS: ", "Pembayaran Non Tunai,"));
                    
                    double totalPendapatan = 0;
                    String totalText = lblTotalPendapatan.getText().replace("Total Pendapatan: Rp ", "").replace(",", "");
                    try {
                        totalPendapatan = Double.parseDouble(totalText);
                        if (totalPendapatan > 0) {
                            double totalCashValue = Double.parseDouble(lblTotalCash.getText().replace("Cash: Rp ", "").replace(",", ""));
                            double totalQRISValue = Double.parseDouble(lblTotalQRIS.getText().replace("QRIS: Rp ", "").replace(",", ""));
                            
                            double persenCash = (totalCashValue / totalPendapatan) * 100;
                            double persenQRIS = (totalQRISValue / totalPendapatan) * 100;
                            
                            writer.println();
                            writer.println("PERSENTASE METODE PEMBAYARAN");
                            writer.println("Tunai," + String.format("%.1f%%", persenCash));
                            writer.println("Non Tunai," + String.format("%.1f%%", persenQRIS));
                        }
                    } catch (NumberFormatException e) {
                        // Skip
                    }
                    
                    JOptionPane.showMessageDialog(this, "Laporan berhasil diekspor ke: " + fileToSave.getAbsolutePath());
                    
                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error menyimpan file: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error export: " + e.getMessage());
        }
    }
}