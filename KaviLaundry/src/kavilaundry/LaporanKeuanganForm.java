/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
    private JLabel lblTotalPendapatan, lblTotalTransaksi;
    private JButton btnFilter, btnExport, btnTutup;
    
    public LaporanKeuanganForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Laporan Keuangan");
        setSize(900, 600);
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
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan"));
        
        lblTotalPendapatan = new JLabel("Total Pendapatan: Rp 0", SwingConstants.CENTER);
        lblTotalPendapatan.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalPendapatan.setOpaque(true);
        lblTotalPendapatan.setBackground(Color.GREEN);
        lblTotalPendapatan.setForeground(Color.WHITE);
        
        lblTotalTransaksi = new JLabel("Total Transaksi: 0", SwingConstants.CENTER);
        lblTotalTransaksi.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalTransaksi.setOpaque(true);
        lblTotalTransaksi.setBackground(Color.BLUE);
        lblTotalTransaksi.setForeground(Color.WHITE);
        
        summaryPanel.add(lblTotalPendapatan);
        summaryPanel.add(lblTotalTransaksi);
        
        // Table
        String[] columns = {"Tanggal", "ID Transaksi", "Pelanggan", "Paket", "Total", "Status", "Kasir"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        
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
        int totalTransaksi = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.tanggal_transaksi, t.id_transaksi, p.nama, " +
                        "pk.nama as paket_nama, pk.kapasitas, t.total_biaya, " +
                        "t.status_pesanan, u.username " +
                        "FROM transaksi t " +
                        "LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                        "LEFT JOIN paket pk ON t.id_jenis = pk.id " +
                        "LEFT JOIN user u ON t.id_user = u.id_user " +
                        "WHERE DATE(t.tanggal_transaksi) BETWEEN ? AND ? " +
                        "ORDER BY t.tanggal_transaksi DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, new java.sql.Date(fromDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(toDate.getTime()));
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                double biaya = rs.getDouble("total_biaya");
                totalPendapatan += biaya;
                totalTransaksi++;
                
                Object[] row = {
                    sdf.format(rs.getTimestamp("tanggal_transaksi")),
                    rs.getInt("id_transaksi"),
                    rs.getString("nama"),
                    rs.getString("paket_nama") + " " + rs.getString("kapasitas"),
                    "Rp " + String.format("%,.0f", biaya),
                    rs.getString("status_pesanan"),
                    rs.getString("username")
                };
                model.addRow(row);
            }
            
            // Update summary
            lblTotalPendapatan.setText("Total Pendapatan: Rp " + String.format("%,.0f", totalPendapatan));
            lblTotalTransaksi.setText("Total Transaksi: " + totalTransaksi);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void exportToExcel() {
        // Implementasi sederhana export ke CSV (bisa dibuka di Excel)
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan");
            fileChooser.setSelectedFile(new java.io.File("laporan_keuangan.csv"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                
                try (java.io.PrintWriter writer = new java.io.PrintWriter(fileToSave)) {
                    // Write header
                    writer.println("Tanggal,ID Transaksi,Pelanggan,Paket,Total,Status,Kasir");
                    
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
