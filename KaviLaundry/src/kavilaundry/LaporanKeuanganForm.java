/*
 * LaporanKeuanganForm.java - Updated dengan Metode Pembayaran dan Filter Pending
 */
package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class LaporanKeuanganForm extends JFrame {
    private JDateChooser dateFrom, dateTo;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTotalPendapatan, lblTotalTransaksi, lblTotalCash, lblTotalQRIS;
    private JButton btnFilter, btnExport, btnTutup;
    
    public LaporanKeuanganForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Laporan Keuangan");
        setSize(1000, 700);
        setLayout(new BorderLayout());
        
        // Panel Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Tanggal"));
        
        filterPanel.add(new JLabel("Dari:"));
        dateFrom = new JDateChooser();
        dateFrom.setPreferredSize(new Dimension(120, 25));
        dateFrom.setDate(new Date()); // Default hari ini
        filterPanel.add(dateFrom);
        
        filterPanel.add(new JLabel("Sampai:"));
        dateTo = new JDateChooser();
        dateTo.setPreferredSize(new Dimension(120, 25));
        dateTo.setDate(new Date()); // Default hari ini
        filterPanel.add(dateTo);
        
        btnFilter = new JButton("Filter");
        btnExport = new JButton("Export Excel");
        btnTutup = new JButton("Tutup");
        
        btnFilter.addActionListener(e -> loadDataByDateRange());
        btnExport.addActionListener(e -> exportToExcel());
        btnTutup.addActionListener(e -> dispose());
        
        filterPanel.add(btnFilter);
        filterPanel.add(btnExport);
        filterPanel.add(btnTutup);
        
        // Summary Panel dengan 4 kolom
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan"));
        
        lblTotalPendapatan = new JLabel("Total Pendapatan: Rp 0", SwingConstants.CENTER);
        lblTotalPendapatan.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalPendapatan.setOpaque(true);
        lblTotalPendapatan.setBackground(Color.GREEN);
        lblTotalPendapatan.setForeground(Color.BLACK);
        
        lblTotalTransaksi = new JLabel("Total Transaksi: 0", SwingConstants.CENTER);
        lblTotalTransaksi.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalTransaksi.setOpaque(true);
        lblTotalTransaksi.setBackground(Color.CYAN);
        lblTotalTransaksi.setForeground(Color.BLACK);
        
        lblTotalCash = new JLabel("Cash: Rp 0", SwingConstants.CENTER);
        lblTotalCash.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalCash.setOpaque(true);
        lblTotalCash.setBackground(new Color(255, 165, 0)); // Orange
        lblTotalCash.setForeground(Color.BLACK);
        
        lblTotalQRIS = new JLabel("QRIS: Rp 0", SwingConstants.CENTER);
        lblTotalQRIS.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalQRIS.setOpaque(true);
        lblTotalQRIS.setBackground(Color.MAGENTA); // Purple
        lblTotalQRIS.setForeground(Color.BLACK);
        
        summaryPanel.add(lblTotalPendapatan);
        summaryPanel.add(lblTotalTransaksi);
        summaryPanel.add(lblTotalCash);
        summaryPanel.add(lblTotalQRIS);
        
        // Table dengan kolom metode pembayaran
        String[] columns = {"Tanggal", "ID Transaksi", "Pelanggan", "Paket", "Berat", "Total", "Metode Bayar", "Status", "Kasir"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // Tanggal
        table.getColumnModel().getColumn(1).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Pelanggan
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Paket
        table.getColumnModel().getColumn(4).setPreferredWidth(70);  // Berat
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Total
        table.getColumnModel().getColumn(6).setPreferredWidth(150); // Metode Bayar
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Kasir
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadData() {
        // Load data untuk hari ini
        loadDataByDateRange();
    }
    
    private void loadDataByDateRange() {
        Date fromDate = dateFrom.getDate();
        Date toDate = dateTo.getDate();
        
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal dari dan sampai!");
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
                        // HANYA TRANSAKSI YANG SUDAH LUNAS (tidak termasuk pending/belum bayar)
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
                
                // Hanya hitung yang sudah lunas
                if ("Lunas".equals(statusBayar)) {
                    totalPendapatan += biaya;
                    totalTransaksi++;
                    
                    // Pisahkan berdasarkan metode pembayaran
                    if (tingkatCuci != null && tingkatCuci.contains("QRIS")) {
                        totalQRIS += biaya;
                    } else {
                        totalCash += biaya;
                    }
                }
                
                // Extract metode pembayaran untuk display
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
            
            // Update summary
            lblTotalPendapatan.setText("Total Pendapatan: Rp " + String.format("%,.0f", totalPendapatan));
            lblTotalTransaksi.setText("Total Transaksi: " + totalTransaksi);
            lblTotalCash.setText("Cash: Rp " + String.format("%,.0f", totalCash));
            lblTotalQRIS.setText("QRIS: Rp " + String.format("%,.0f", totalQRIS));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void exportToExcel() {
        // Implementasi export ke CSV dengan data yang sudah difilter
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
                    // Write header
                    writer.println("LAPORAN KEUANGAN KAVI LAUNDRY");
                    writer.println("Periode: " + new SimpleDateFormat("dd/MM/yyyy").format(dateFrom.getDate()) + 
                                  " - " + new SimpleDateFormat("dd/MM/yyyy").format(dateTo.getDate()));
                    writer.println();
                    
                    // Write column headers
                    writer.println("Tanggal,ID Transaksi,Pelanggan,Paket,Berat,Total,Metode Bayar,Status,Kasir");
                    
                    // Write data
                    for (int i = 0; i < model.getRowCount(); i++) {
                        StringBuilder line = new StringBuilder();
                        for (int j = 0; j < model.getColumnCount(); j++) {
                            if (j > 0) line.append(",");
                            Object value = model.getValueAt(i, j);
                            line.append("\"").append(value != null ? value.toString() : "").append("\"");
                        }
                        writer.println(line.toString());
                    }
                    
                    // Write summary
                    writer.println();
                    writer.println("RINGKASAN");
                    writer.println(lblTotalPendapatan.getText().replace("Total Pendapatan: ", "Total Pendapatan,"));
                    writer.println(lblTotalTransaksi.getText().replace("Total Transaksi: ", "Total Transaksi,"));
                    writer.println();
                    writer.println("BREAKDOWN METODE PEMBAYARAN");
                    writer.println(lblTotalCash.getText().replace("Cash: ", "Pembayaran Tunai,"));
                    writer.println(lblTotalQRIS.getText().replace("QRIS: ", "Pembayaran Non Tunai,"));
                    
                    // Persentase
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
                        // Skip percentage calculation if parsing fails
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