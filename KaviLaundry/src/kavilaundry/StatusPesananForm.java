/*
 * StatusPesananForm.java - Enhanced dengan Auto Detect Update Tanggal
 */
package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
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
    
    public StatusPesananForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Status Pesanan & Pembayaran");
        setSize(1100, 600);
        setLayout(new BorderLayout(10, 10));
        
        // Panel utama dengan padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Search di atas
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Pencarian"));
        
        searchPanel.add(new JLabel("Cari (ID/Nama/Paket):"));
        txtSearch = new JTextField(30);
        searchPanel.add(txtSearch);
        
        btnClearSearch = new JButton("Clear");
        searchPanel.add(btnClearSearch);
        
        // Panel kontrol
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Update Status & Pembayaran"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Status Pesanan
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Status Pesanan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbStatus = new JComboBox<>(new String[]{"diterima", "dicuci", "dijemur", "setrika", "selesai", "diambil"});
        cmbStatus.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(cmbStatus, gbc);
        
        // Status Pembayaran
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Status Bayar:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbStatusBayar = new JComboBox<>(new String[]{"Belum Bayar", "Lunas"});
        cmbStatusBayar.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(cmbStatusBayar, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        btnUpdate = new JButton("💾 Update");
        btnUpdate.setPreferredSize(new Dimension(150, 30));
        btnUpdate.setBackground(new Color(46, 204, 113));
        btnUpdate.setFocusPainted(false);
        controlPanel.add(btnUpdate, gbc);
        
        gbc.gridx = 1;
        btnDetail = new JButton("📄 Detail");
        btnDetail.setPreferredSize(new Dimension(100, 30));
        controlPanel.add(btnDetail, gbc);
        
        gbc.gridx = 2;
        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        controlPanel.add(btnRefresh, gbc);
        
        gbc.gridx = 3;
        btnTutup = new JButton("❌ Tutup");
        btnTutup.setPreferredSize(new Dimension(100, 30));
        btnTutup.setBackground(new Color(231, 76, 60));
        btnTutup.setFocusPainted(false);
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
        
        // Table dengan kolom tambahan untuk status pembayaran
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
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Setup sorter untuk search
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Pesanan"));
        
        // Gabungkan panels
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void filterTable() {
        String searchText = txtSearch.getText().trim();
        
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Search di kolom ID, Pelanggan, dan Paket (kolom 0, 2, 3)
                RowFilter<DefaultTableModel, Object> rf = RowFilter.orFilter(
                    java.util.Arrays.asList(
                        RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0), // ID
                        RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 2), // Pelanggan
                        RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 3)  // Paket
                    )
                );
                sorter.setRowFilter(rf);
            } catch (java.util.regex.PatternSyntaxException e) {
                // Jika ada error di regex, tampilkan semua data
                sorter.setRowFilter(null);
            }
        }
        
        // Update label info
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
            
            setTitle(String.format("Status Pesanan & Pembayaran - Total: %d data", model.getRowCount()));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void updateComboBoxes() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            // Konversi dari view row index ke model row index (untuk filtered table)
            int modelRow = table.convertRowIndexToModel(selectedRow);
            
            String statusPesanan = (String) model.getValueAt(modelRow, 5);
            String statusBayar = (String) model.getValueAt(modelRow, 6);
            
            cmbStatus.setSelectedItem(statusPesanan);
            
            // Set status bayar di combobox
            if (statusBayar.equals("Lunas")) {
                cmbStatusBayar.setSelectedIndex(1);
                // VALIDASI: Jika sudah lunas, combobox jadi readonly
                cmbStatusBayar.setEnabled(false);
                cmbStatusBayar.setToolTipText("Pembayaran sudah lunas, tidak bisa diubah");
            } else {
                if (statusBayar.equals("Belum Bayar")) {
                    cmbStatusBayar.setSelectedIndex(0);
                } else { // Pending
                    cmbStatusBayar.setSelectedIndex(0);
                }
                // Belum lunas, masih bisa diubah
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
        
        // Konversi dari view row index ke model row index (untuk filtered table)
        int modelRow = table.convertRowIndexToModel(selectedRow);
        
        int idTransaksi = (Integer) model.getValueAt(modelRow, 0);
        String statusPesananBaru = (String) cmbStatus.getSelectedItem();
        String statusPesananLama = (String) model.getValueAt(modelRow, 5);
        String statusBayarBaru = (String) cmbStatusBayar.getSelectedItem();
        String statusBayarLama = (String) model.getValueAt(modelRow, 6);
        String metodeLama = (String) model.getValueAt(modelRow, 7);
        
        boolean statusPesananBerubah = !statusPesananLama.equals(statusPesananBaru);
        boolean statusBayarBerubah = !statusBayarLama.equals(statusBayarBaru);
        
        // Validasi: Cek apakah sudah lunas
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
        
        // LOGIKA AUTO DETECT: Apakah perlu update tanggal?
        boolean updateTanggal = false;
        String alasanUpdateTanggal = "";
        
        // Update tanggal HANYA jika status pembayaran berubah (Belum Bayar → Lunas)
        if (statusBayarBerubah && statusBayarBaru.equals("Lunas")) {
            updateTanggal = true;
            alasanUpdateTanggal = "Pembayaran baru lunas";
        }
        // TIDAK update tanggal jika:
        // - Sudah lunas dari awal (statusBayarLama = Lunas dan tidak berubah)
        // - Hanya update status pesanan saja
        
        // Konfirmasi
        StringBuilder confirmMsg = new StringBuilder("Konfirmasi perubahan:\n\n");
        if (statusPesananBerubah) {
            confirmMsg.append(String.format("Status Pesanan: %s → %s\n", statusPesananLama, statusPesananBaru));
        }
        if (statusBayarBerubah) {
            confirmMsg.append(String.format("Status Bayar: %s → %s\n", statusBayarLama, statusBayarBaru));
        }
        
        if (updateTanggal) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            confirmMsg.append(String.format("\n⚠️ Tanggal akan diupdate ke: %s\n", sdf.format(new java.util.Date())));
            confirmMsg.append(String.format("   Alasan: %s\n", alasanUpdateTanggal));
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
            
            // Update status pembayaran jika berubah
            if (statusBayarBerubah && statusBayarBaru.equals("Lunas")) {
                if (!metodeLama.contains("LUNAS")) {
                    metodeBaruString = metodeLama + " - LUNAS";
                }
                
                // Jika pembayaran ditunda dan sekarang dibayar, minta konfirmasi metode
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
                        return; // User cancelled
                    }
                }
            } else if (statusBayarBerubah && statusBayarBaru.equals("Belum Bayar")) {
                metodeBaruString = metodeLama.replace(" - LUNAS", "");
            }
            
            // Update ke database
            String sql;
            PreparedStatement pstmt;
            
            if (updateTanggal) {
                // Update dengan tanggal baru (karena pembayaran baru lunas)
                sql = "UPDATE transaksi SET status_pesanan = ?, pembayaran = ?, tanggal_transaksi = NOW() WHERE id_transaksi = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, statusPesananBaru);
                pstmt.setString(2, metodeBaruString);
                pstmt.setInt(3, idTransaksi);
            } else {
                // Update tanpa mengubah tanggal (hanya update status pesanan atau sudah lunas dari awal)
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
                successMsg.append(String.format("  (%s)\n", alasanUpdateTanggal));
            } else {
                successMsg.append("✓ Tanggal tetap (tidak diubah)\n");
            }
            
            JOptionPane.showMessageDialog(this, successMsg.toString(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
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
        
        // Get actual row index from filtered table
        int actualRow = table.convertRowIndexToModel(selectedRow);
        int idTransaksi = (Integer) model.getValueAt(actualRow, 0);
        
        // Create detail dialog
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
}