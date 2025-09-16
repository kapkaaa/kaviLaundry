/*
 * StatusPesananForm.java - Enhanced dengan Status Pembayaran
 */
package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class StatusPesananForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbStatus, cmbStatusBayar;
    private JButton btnUpdateStatus, btnUpdateBayar, btnRefresh, btnTutup, btnDetail;
    
    public StatusPesananForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Status Pesanan & Pembayaran");
        setSize(1000, 500);
        setLayout(new BorderLayout());
        
        // Panel kontrol
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Kontrol Status"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Status Pesanan
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Status Pesanan:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"diterima", "dicuci", "dijemur", "setrika", "selesai"});
        controlPanel.add(cmbStatus, gbc);
        gbc.gridx = 2;
        btnUpdateStatus = new JButton("Update Status");
        controlPanel.add(btnUpdateStatus, gbc);
        
        // Status Pembayaran
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Status Bayar:"), gbc);
        gbc.gridx = 1;
        cmbStatusBayar = new JComboBox<>(new String[]{"Belum Bayar", "Lunas"});
        controlPanel.add(cmbStatusBayar, gbc);
        gbc.gridx = 2;
        btnUpdateBayar = new JButton("Update Pembayaran");
        controlPanel.add(btnUpdateBayar, gbc);
        
        // Action buttons
        gbc.gridx = 0; gbc.gridy = 2;
        btnDetail = new JButton("Detail");
        controlPanel.add(btnDetail, gbc);
        gbc.gridx = 1;
        btnRefresh = new JButton("Refresh");
        controlPanel.add(btnRefresh, gbc);
        gbc.gridx = 2;
        btnTutup = new JButton("Tutup");
        controlPanel.add(btnTutup, gbc);
        
        // Event listeners
        btnUpdateStatus.addActionListener(e -> updateStatus());
        btnUpdateBayar.addActionListener(e -> updateStatusBayar());
        btnDetail.addActionListener(e -> showDetail());
        btnRefresh.addActionListener(e -> loadData());
        btnTutup.addActionListener(e -> dispose());
        
        // Table dengan kolom tambahan untuk status pembayaran
        String[] columns = {"ID", "Tanggal", "Pelanggan", "Paket", "Berat", "Status Pesanan", "Status Bayar", "Metode & Waktu", "Total"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
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
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Status Pesanan
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Status Bayar
        table.getColumnModel().getColumn(7).setPreferredWidth(150); // Metode & Waktu
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Total
        
        // Add row selection listener to update combo boxes
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateComboBoxes();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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
                        "WHERE t.status_pesanan != 'selesai' OR " +
                        "      (t.status_pesanan = 'selesai' AND t.pembayaran LIKE '%Bayar Setelah Selesai%' AND " +
                        "       t.pembayaran NOT LIKE '%LUNAS%') " +
                        "ORDER BY t.tanggal_transaksi DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                String statusBayar = rs.getString("status_bayar");
                String tingkatCuci = rs.getString("pembayaran");
                
                // Override status bayar jika sudah ditandai LUNAS
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void updateComboBoxes() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String statusPesanan = (String) model.getValueAt(selectedRow, 5);
            String statusBayar = (String) model.getValueAt(selectedRow, 6);
            
            cmbStatus.setSelectedItem(statusPesanan);
            
            if (statusBayar.equals("Lunas")) {
                cmbStatusBayar.setSelectedIndex(1);
            } else {
                cmbStatusBayar.setSelectedIndex(0);
            }
        }
    }
    
    private void updateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pesanan untuk mengubah status!");
            return;
        }
        
        int idTransaksi = (Integer) model.getValueAt(selectedRow, 0);
        String statusBaru = (String) cmbStatus.getSelectedItem();
        String statusLama = (String) model.getValueAt(selectedRow, 5);
        
        if (statusLama.equals(statusBaru)) {
            JOptionPane.showMessageDialog(this, "Status sudah sama, tidak ada perubahan!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE transaksi SET status_pesanan = ? WHERE id_transaksi = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, statusBaru);
            pstmt.setInt(2, idTransaksi);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status pesanan berhasil diupdate dari '" + statusLama + "' ke '" + statusBaru + "'!");
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage());
        }
    }
    
    private void updateStatusBayar() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pesanan untuk mengubah status pembayaran!");
            return;
        }
        
        int idTransaksi = (Integer) model.getValueAt(selectedRow, 0);
        String statusBayarBaru = (String) cmbStatusBayar.getSelectedItem();
        String statusBayarLama = (String) model.getValueAt(selectedRow, 6);
        String metodeLama = (String) model.getValueAt(selectedRow, 7);
        
        if (statusBayarLama.equals(statusBayarBaru)) {
            JOptionPane.showMessageDialog(this, "Status pembayaran sudah sama, tidak ada perubahan!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String metodeBaruString = metodeLama;
            
            if (statusBayarBaru.equals("Lunas")) {
                // Tambahkan flag LUNAS ke pembayaran
                if (!metodeLama.contains("LUNAS")) {
                    metodeBaruString = metodeLama + " - LUNAS";
                }
                
                // Jika pembayaran ditunda dan sekarang dibayar, minta konfirmasi metode
                if (metodeLama.contains("Bayar Setelah Selesai")) {
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
                        return; // User cancelled
                    }
                }
            } else {
                // Hapus flag LUNAS
                metodeBaruString = metodeLama.replace(" - LUNAS", "");
            }
            
            String sql = "UPDATE transaksi SET pembayaran = ? WHERE id_transaksi = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, metodeBaruString);
            pstmt.setInt(2, idTransaksi);
            
            pstmt.executeUpdate();
            
            String message = "Status pembayaran berhasil diupdate!\n" +
                           "Dari: " + statusBayarLama + "\n" +
                           "Ke: " + statusBayarBaru;
            
            JOptionPane.showMessageDialog(this, message);
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating payment status: " + e.getMessage());
        }
    }
    
    private void showDetail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pesanan untuk melihat detail!");
            return;
        }
        
        int idTransaksi = (Integer) model.getValueAt(selectedRow, 0);
        
        // Create detail dialog
        JDialog detailDialog = new JDialog(this, "Detail Transaksi", true);
        detailDialog.setSize(500, 400);
        detailDialog.setLocationRelativeTo(this);
        
        JTextArea txtDetail = new JTextArea();
        txtDetail.setEditable(false);
        txtDetail.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
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
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                StringBuilder detail = new StringBuilder();
                
                detail.append("DETAIL TRANSAKSI\n");
                detail.append("=====================================\n");
                detail.append(String.format("ID Transaksi : %d\n", rs.getInt("id_transaksi")));
                detail.append(String.format("Tanggal      : %s\n", sdf.format(rs.getTimestamp("tanggal_transaksi"))));
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
}