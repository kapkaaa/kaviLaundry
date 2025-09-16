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

public class RiwayatTransaksiForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtCariNama;
    private JButton btnCari, btnRefresh, btnDetail, btnTutup;
    
    public RiwayatTransaksiForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Riwayat Transaksi");
        setSize(900, 500);
        setLayout(new BorderLayout());
        
        // Panel pencarian
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Cari Nama Pelanggan:"));
        
        txtCariNama = new JTextField(20);
        searchPanel.add(txtCariNama);
        
        btnCari = new JButton("Cari");
        btnRefresh = new JButton("Refresh");
        btnDetail = new JButton("Detail");
        btnTutup = new JButton("Tutup");
        
        btnCari.addActionListener(e -> cariTransaksi());
        btnRefresh.addActionListener(e -> loadData());
        btnDetail.addActionListener(e -> showDetail());
        btnTutup.addActionListener(e -> dispose());
        
        searchPanel.add(btnCari);
        searchPanel.add(btnRefresh);
        searchPanel.add(btnDetail);
        searchPanel.add(btnTutup);
        
        // Table
        String[] columns = {"ID", "Tanggal", "Pelanggan", "Paket", "Berat", "Total", "Status", "Kasir"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Enter key untuk pencarian
        txtCariNama.addActionListener(e -> cariTransaksi());
    }
    
    private void loadData() {
        loadData(null);
    }
    
    private void loadData(String namaPelanggan) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT t.id_transaksi, t.tanggal_transaksi, p.nama, ");
            sql.append("pk.nama as paket_nama, pk.kapasitas, t.berat_kg, t.total_biaya, ");
            sql.append("t.status_pesanan, u.username ");
            sql.append("FROM transaksi t ");
            sql.append("LEFT JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan ");
            sql.append("LEFT JOIN paket pk ON t.id_jenis = pk.id ");
            sql.append("LEFT JOIN user u ON t.id_user = u.id_user ");
            
            if (namaPelanggan != null && !namaPelanggan.trim().isEmpty()) {
                sql.append("WHERE p.nama LIKE ? ");
            }
            
            sql.append("ORDER BY t.tanggal_transaksi DESC");
            
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            
            if (namaPelanggan != null && !namaPelanggan.trim().isEmpty()) {
                pstmt.setString(1, "%" + namaPelanggan + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_transaksi"),
                    sdf.format(rs.getTimestamp("tanggal_transaksi")),
                    rs.getString("nama"),
                    rs.getString("paket_nama") + " " + rs.getString("kapasitas"),
                    rs.getDouble("berat_kg") + " kg",
                    "Rp " + String.format("%,.0f", rs.getDouble("total_biaya")),
                    rs.getString("status_pesanan"),
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
        new DetailTransaksiDialog(this, idTransaksi).setVisible(true);
    }
}
