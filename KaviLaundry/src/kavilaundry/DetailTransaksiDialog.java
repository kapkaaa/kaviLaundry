/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class DetailTransaksiDialog extends JDialog {
    private int idTransaksi;
    
    public DetailTransaksiDialog(Frame parent, int idTransaksi) {
        super(parent, "Detail Transaksi", true);
        this.idTransaksi = idTransaksi;
        initComponents();
        loadDetail();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(400, 300);
        setLayout(new BorderLayout());
        
        JTextArea txtDetail = new JTextArea();
        txtDetail.setEditable(false);
        txtDetail.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(txtDetail);
        
        JButton btnTutup = new JButton("Tutup");
        btnTutup.addActionListener(e -> dispose());
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnTutup);
        
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        
        // Load detail ke txtDetail
        loadDetailToTextArea(txtDetail);
    }
    
    private void loadDetail() {
        // Implementation will be in loadDetailToTextArea
    }
    
    private void loadDetailToTextArea(JTextArea txtDetail) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.*, p.nama as nama_pelanggan, pk.nama as paket_nama, " +
                        "pk.kapasitas, u.username " +
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
                detail.append("=====================================\n");
                detail.append(String.format("ID Transaksi : %d\n", rs.getInt("id_transaksi")));
                detail.append(String.format("Tanggal      : %s\n", sdf.format(rs.getTimestamp("tanggal_transaksi"))));
                detail.append(String.format("Pelanggan    : %s\n", rs.getString("nama_pelanggan")));
                detail.append(String.format("Paket        : %s %s\n", rs.getString("paket_nama"), rs.getString("kapasitas")));
                detail.append(String.format("Berat        : %.1f kg\n", rs.getDouble("berat_kg")));
                detail.append(String.format("Total Biaya  : Rp %,.0f\n", rs.getDouble("total_biaya")));
                detail.append(String.format("Status       : %s\n", rs.getString("status_pesanan")));
                detail.append(String.format("Kasir        : %s\n", rs.getString("username")));
                
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
    }
}
