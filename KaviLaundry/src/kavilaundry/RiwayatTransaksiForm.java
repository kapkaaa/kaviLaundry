/*
 * RiwayatTransaksiForm.java - Updated dengan Status Bayar dan Metode Pembayaran
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
    private JCheckBox chkShowPending;
    private JButton btnCari, btnRefresh, btnDetail, btnTutup;
    
    public RiwayatTransaksiForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Riwayat Transaksi");
        setSize(1200, 600);
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
        
        // Checkbox untuk filter pending/belum bayar
        chkShowPending = new JCheckBox("Hanya Pending/Belum Bayar");
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
        
        // Table dengan kolom tambahan
        String[] columns = {"ID", "Tanggal", "Pelanggan", "Paket", "Berat", "Total", "Status Pesanan", "Status Bayar", "Metode Bayar", "Kasir"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Tanggal
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Pelanggan
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Paket
        table.getColumnModel().getColumn(4).setPreferredWidth(70);  // Berat
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Total
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Status Pesanan
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status Bayar
        table.getColumnModel().getColumn(8).setPreferredWidth(150); // Metode Bayar
        table.getColumnModel().getColumn(9).setPreferredWidth(100); // Kasir
        
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
            
            // Build WHERE clause
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
                
                // Extract metode pembayaran
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
        // Create detail dialog
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

                    // Tampilkan metode pembayaran
                    detail.append(String.format("Metode       : %s\n", metode));

                    // Logika untuk metode Bayar di Awal
                    if (metode.contains("Bayar di Awal")) {
                        // Jenis pembayaran harus sudah ada (Cash / QRIS)
                        if (metode.contains("QRIS")) {
                            detail.append("Jenis        : Non Tunai (QRIS)\n");
                        } else {
                            detail.append("Jenis        : Tunai (Cash)\n");
                        }
                        detail.append("Keterangan   : Dibayar di awal\n");
                    }

                    // Logika untuk metode Bayar Setelah Selesai
                    else if (metode.contains("Bayar Setelah Selesai")) {
                        // Kalau sudah ada kata "LUNAS" -> berarti sudah dibayar
                        if (metode.contains("LUNAS")) {
                            // Tentukan jenis pembayaran saat pelunasan
                            if (metode.contains("QRIS")) {
                                detail.append("Jenis        : Non Tunai (QRIS)\n");
                            } else {
                                detail.append("Jenis        : Tunai (Cash)\n");
                            }
                            detail.append("Keterangan   : Sudah dibayar setelah selesai\n");
                        } else {
                            // Pending -> jangan tampilkan jenis pembayaran
                            detail.append("Keterangan   : Belum dibayar\n");
                        }
                    }
                }
                            
                
                String addonIds = rs.getString("addon_ids");
                if (addonIds != null && !addonIds.trim().isEmpty()) {
                    detail.append("========================================\n");
                    detail.append("ADDON YANG DIGUNAKAN:\n");
                    
                    // Count addon occurrences
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