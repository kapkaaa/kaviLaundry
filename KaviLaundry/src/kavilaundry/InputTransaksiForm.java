/*
 * InputTransaksiForm.java - Updated dengan logika per KG dan Payment Method
 */
package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InputTransaksiForm extends JFrame {
    private JTextField txtNamaPelanggan, txtBerat;
    private JComboBox<String> cmbPaket, cmbWaktuBayar, cmbMetodePembayaran;
    private JCheckBox chkVoucherDigunakan;
    private JTextArea txtRincian;
    private JLabel lblTotal, lblVoucherInfo;
    private JButton btnHitung, btnSimpan, btnCetak, btnTutup;
    
    private List<PaketLayanan> paketList = new ArrayList<>();
    private double totalBiaya = 0;
    private int idPelanggan = 0;
    
    public InputTransaksiForm() {
        initComponents();
        loadPaketData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Input Transaksi");
        setSize(650, 650);
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Transaksi"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nama Pelanggan
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Pelanggan:"), gbc);
        gbc.gridx = 1;
        txtNamaPelanggan = new JTextField(20);
        formPanel.add(txtNamaPelanggan, gbc);
        
        // Paket
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Paket Layanan:"), gbc);
        gbc.gridx = 1;
        cmbPaket = new JComboBox<>();
        cmbPaket.addActionListener(e -> updateVoucherVisibility());
        formPanel.add(cmbPaket, gbc);
        
        // Berat
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Berat (kg):"), gbc);
        gbc.gridx = 1;
        txtBerat = new JTextField(20);
        txtBerat.setText("1");
        formPanel.add(txtBerat, gbc);
        
        // Voucher dengan info
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Gunakan Voucher:"), gbc);
        gbc.gridx = 1;
        JPanel voucherPanel = new JPanel(new BorderLayout());
        chkVoucherDigunakan = new JCheckBox("Gunakan 7 Voucher (Gratis 7kg)");
        chkVoucherDigunakan.setEnabled(false);
        lblVoucherInfo = new JLabel("Voucher tersedia: 0");
        lblVoucherInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblVoucherInfo.setForeground(Color.BLUE);
        
        voucherPanel.add(chkVoucherDigunakan, BorderLayout.NORTH);
        voucherPanel.add(lblVoucherInfo, BorderLayout.SOUTH);
        formPanel.add(voucherPanel, gbc);
        
        // Waktu Pembayaran
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Waktu Pembayaran:"), gbc);
        gbc.gridx = 1;
        cmbWaktuBayar = new JComboBox<>(new String[]{"Bayar Sekarang", "Bayar Setelah Selesai"});
        formPanel.add(cmbWaktuBayar, gbc);
        
        // Metode Pembayaran
        JLabel lblMetodePembayaran = new JLabel("Metode Pembayaran:");
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(lblMetodePembayaran, gbc);
        gbc.gridx = 1;
        cmbMetodePembayaran = new JComboBox<>(new String[]{"Cash", "QRIS"});
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
    
            // refresh panel supaya update
            formPanel.revalidate();
            formPanel.repaint();
        });
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnHitung = new JButton("Hitung Total");
        btnSimpan = new JButton("Simpan");
        btnCetak = new JButton("Cetak Struk");
        btnTutup = new JButton("Tutup");
        
        btnHitung.addActionListener(e -> hitungTotal());
        btnSimpan.addActionListener(e -> simpanTransaksi());
        btnCetak.addActionListener(e -> cetakStruk());
        btnTutup.addActionListener(e -> dispose());
        
        btnPanel.add(btnHitung);
        btnPanel.add(btnSimpan);
        btnPanel.add(btnCetak);
        btnPanel.add(btnTutup);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        // Rincian Panel
        JPanel rincianPanel = new JPanel(new BorderLayout());
        rincianPanel.setBorder(BorderFactory.createTitledBorder("Rincian Biaya"));
        
        txtRincian = new JTextArea(8, 40);
        txtRincian.setEditable(false);
        txtRincian.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollRincian = new JScrollPane(txtRincian);
        
        lblTotal = new JLabel("TOTAL: Rp 0", SwingConstants.CENTER);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setOpaque(true);
        lblTotal.setBackground(Color.YELLOW);
        
        rincianPanel.add(scrollRincian, BorderLayout.CENTER);
        rincianPanel.add(lblTotal, BorderLayout.SOUTH);
        
        add(formPanel, BorderLayout.NORTH);
        add(rincianPanel, BorderLayout.CENTER);
        
        btnSimpan.setEnabled(false);
        btnCetak.setEnabled(false);
        
        // Add listener untuk update voucher info saat nama pelanggan berubah
        txtNamaPelanggan.addActionListener(e -> updateVoucherInfo());
        txtNamaPelanggan.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateVoucherInfo();
            }
        });
    }
    
    private void updateVoucherVisibility() {
        if (cmbPaket.getSelectedIndex() < 0 || paketList.isEmpty()) {
            return;
        }
        
        PaketLayanan selectedPaket = paketList.get(cmbPaket.getSelectedIndex());
        String paketNama = selectedPaket.nama.toLowerCase();
        
        // Voucher hanya aktif untuk paket "Cuci" (Cuci Basah atau Cuci Kering)
        // Tidak aktif untuk "Setrika" atau "Cuci Setrika"
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
            
            PaketLayanan selectedPaket = paketList.get(selectedPaketIndex);
            double berat = Double.parseDouble(txtBerat.getText().trim());
            
            if (berat <= 0) {
                JOptionPane.showMessageDialog(this, "Berat harus lebih dari 0!");
                return;
            }
            
            // Cek dan buat pelanggan jika belum ada
            idPelanggan = getOrCreatePelanggan(namaPelanggan);
            
            // Hitung biaya dasar - SEMUA PAKET SEKARANG PER KG
            double biayaDasar = selectedPaket.harga * berat;
            
            // Hitung penggunaan voucher - mengurangi 7kg dari berat
            boolean voucherDigunakan = chkVoucherDigunakan.isSelected();
            double beratBayar = berat;
            double diskonVoucher = 0;
            
            if (voucherDigunakan) {
                // Cek voucher yang dimiliki pelanggan
                int voucherTersedia = getVoucherPelanggan(idPelanggan);
                if (voucherTersedia < 7) {
                    JOptionPane.showMessageDialog(this, "Voucher tidak mencukupi! Tersedia: " + voucherTersedia + ", Dibutuhkan: 7");
                    chkVoucherDigunakan.setSelected(false);
                    return;
                }
                
                // Kurangi 7kg dari berat yang harus dibayar
                beratBayar = Math.max(0, berat - 7);
                diskonVoucher = Math.min(7, berat) * selectedPaket.harga;
            }
            
            totalBiaya = beratBayar * selectedPaket.harga;
            
            // Get payment info
            String metodePembayaran = (String) cmbMetodePembayaran.getSelectedItem();
            String waktuBayar = (String) cmbWaktuBayar.getSelectedItem();
            
            // Tampilkan rincian
            StringBuilder rincian = new StringBuilder();
            rincian.append("                  RINCIAN BIAYA                  \n");
            rincian.append("=================================================\n");
            rincian.append(String.format("Pelanggan       : %s\n", namaPelanggan));
            rincian.append(String.format("Paket           : %s\n", selectedPaket.nama));
            rincian.append(String.format("Harga per kg    : Rp %,.0f\n", (double)selectedPaket.harga));
            rincian.append(String.format("Berat total     : %.1f kg\n", berat));
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
            rincian.append(String.format("Metode Bayar    : %s\n", metodePembayaran));
            rincian.append(String.format("Waktu Bayar     : %s\n", waktuBayar));
            
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
            // Cek apakah pelanggan sudah ada
            String checkSql = "SELECT id_pelanggan FROM pelanggan WHERE nama = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nama);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_pelanggan");
            } else {
                // Buat pelanggan baru
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
            
            int selectedPaketIndex = cmbPaket.getSelectedIndex();
            PaketLayanan selectedPaket = paketList.get(selectedPaketIndex);
            double berat = Double.parseDouble(txtBerat.getText().trim());
            boolean voucherDigunakan = chkVoucherDigunakan.isSelected();
            String metodePembayaran = (String) cmbMetodePembayaran.getSelectedItem();
            String waktuBayar = (String) cmbWaktuBayar.getSelectedItem();
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                // Simpan transaksi
                String insertTransaksiSql = "INSERT INTO transaksi (id_pelanggan, id_jenis,                 berat_kg, " + "pembayaran, total_biaya, voucher_didapat, status_pesanan,                  id_user) " + "VALUES (?, ?, ?, ?, ?, ?, 'diterima', ?)";
                
                PreparedStatement pstmt = conn.prepareStatement(insertTransaksiSql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, idPelanggan);
                pstmt.setInt(2, selectedPaket.id);
                pstmt.setDouble(3, berat);
                pstmt.setString(4, metodePembayaran + " - " + waktuBayar);
                pstmt.setDouble(5, totalBiaya);
                pstmt.setInt(6, 1); // Setiap transaksi dapat 1 voucher
                pstmt.setInt(7, UserSession.getCurrentUserId());
                
                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                int idTransaksi = 0;
                if (generatedKeys.next()) {
                    idTransaksi = generatedKeys.getInt(1);
                }
                
                if (voucherDigunakan) {
                    // Jika voucher digunakan, kurangi 7 poin
                    String updateVoucherSql = "UPDATE pelanggan SET total_voucher = total_voucher - 7 WHERE id_pelanggan = ?";
                    try (PreparedStatement updateVoucherStmt = conn.prepareStatement(updateVoucherSql)) {
                        updateVoucherStmt.setInt(1, idPelanggan);
                        updateVoucherStmt.executeUpdate();
                    }
                } else {
                    // Jika tidak pakai voucher, tambah 1 poin
                    String updateVoucherSql = "UPDATE pelanggan SET total_voucher = total_voucher + 1 WHERE id_pelanggan = ?";
                    try (PreparedStatement updateVoucherStmt = conn.prepareStatement(updateVoucherSql)) {
                        updateVoucherStmt.setInt(1, idPelanggan);
                        updateVoucherStmt.executeUpdate();
                    }
                }
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!\nID Transaksi: " + idTransaksi);
                btnCetak.setEnabled(true);
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error menyimpan transaksi: " + e.getMessage());
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtNamaPelanggan.setText("");
        if (cmbPaket.getItemCount() > 0) {
            cmbPaket.setSelectedIndex(0);
            updateVoucherVisibility();
        }
        txtBerat.setText("1");
        chkVoucherDigunakan.setSelected(false);
        cmbMetodePembayaran.setSelectedIndex(0);
        cmbWaktuBayar.setSelectedIndex(0);
        txtRincian.setText("");
        lblTotal.setText("TOTAL: Rp 0");
        lblVoucherInfo.setText("Voucher tersedia: 0");
        totalBiaya = 0;
        idPelanggan = 0;
        btnSimpan.setEnabled(false);
        btnCetak.setEnabled(false);
    }
    
    private void cetakStruk() {
        if (totalBiaya == 0 && !chkVoucherDigunakan.isSelected()) {
            JOptionPane.showMessageDialog(this, "Belum ada transaksi untuk dicetak!");
            return;
        }
        
        // Buat dialog untuk menampilkan struk
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
        
        // Info voucher yang didapat
        struk.append("* Anda mendapat 1 voucher dari transaksi ini    \n");
        struk.append("* Kumpulkan 7 voucher untuk gratis cuci 7kg     \n");
        
        // Info pembayaran
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
    
    // Inner class untuk menyimpan data paket
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