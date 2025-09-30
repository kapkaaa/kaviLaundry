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
import javax.swing.text.*;

public class KelolaHargaForm extends JFrame {
    private JTable tablePaket;
    private DefaultTableModel modelPaket;
    private JTextField txtNama, txtKapasitas, txtHarga, txtKeterangan;
    private JButton btnTambah, btnEdit, btnHapus, btnBatal, btnTutup;
    private int selectedId = -1;
    private boolean isEditMode = false;
    
    public KelolaHargaForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
        setEditMode(false); // Set mode awal ke tambah
    }
    
    private void initComponents() {
        setTitle("Kelola Harga Layanan");
        setSize(700, 500);
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Paket Layanan"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nama
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Paket:"), gbc);
        gbc.gridx = 1;
        txtNama = new JTextField(20);
        formPanel.add(txtNama, gbc);
        
        // Kapasitas
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Kapasitas:"), gbc);
        gbc.gridx = 1;
        txtKapasitas = new JTextField(20);
        formPanel.add(txtKapasitas, gbc);
        
        // Harga
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1;
        txtHarga = new JTextField(20);
        ((AbstractDocument) txtHarga.getDocument()).setDocumentFilter(new NumberOnlyFilter());
        formPanel.add(txtHarga, gbc);
        
        // Keterangan
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Keterangan:"), gbc);
        gbc.gridx = 1;
        txtKeterangan = new JTextField(20);
        formPanel.add(txtKeterangan, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnTambah = new JButton("Tambah");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnBatal = new JButton("Batal"); // Tambah tombol batal
        btnTutup = new JButton("Tutup");
        
        btnTambah.addActionListener(e -> tambahPaket());
        btnEdit.addActionListener(e -> editPaket());
        btnHapus.addActionListener(e -> hapusPaket());
        btnBatal.addActionListener(e -> batalEdit()); // Handler untuk tombol batal
        btnTutup.addActionListener(e -> dispose());
        
        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBatal);
        btnPanel.add(btnTutup);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        // Table
        String[] columns = {"ID", "Nama Paket", "Kapasitas", "Harga", "Keterangan"};
        modelPaket = new DefaultTableModel(columns, 0);
        tablePaket = new JTable(modelPaket);
        tablePaket.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePaket.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablePaket);
        
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        btnTambah.setEnabled(!editMode); // Disable tombol tambah saat mode edit
        btnEdit.setEnabled(editMode);    // Enable tombol edit saat mode edit
        btnHapus.setEnabled(editMode);   // Enable tombol hapus saat mode edit
        btnBatal.setVisible(editMode);   // Show tombol batal saat mode edit
        
        // Update title border
        if (editMode) {
            ((JPanel) getContentPane().getComponent(0)).setBorder(
                BorderFactory.createTitledBorder("Edit Data Pegawai"));
        } else {
            ((JPanel) getContentPane().getComponent(0)).setBorder(
                BorderFactory.createTitledBorder("Data Pegawai"));
        }
    }
    
    // Method untuk membatalkan edit dan kembali ke mode tambah
    private void batalEdit() {
        clearForm();
        setEditMode(false);
        tablePaket.clearSelection(); // Clear selection di tabel
    }
    
    // Filter angka saja
    class NumberOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                throws BadLocationException {
            if (string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
            if (text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
    
    private void loadData() {
        modelPaket.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM paket ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kapasitas"),
                    "Rp " + String.format("%,.0f", rs.getDouble("harga")),
                    rs.getString("keterangan")
                };
                modelPaket.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void selectRow() {
        int row = tablePaket.getSelectedRow();
        if (row >= 0) {
            selectedId = (Integer) modelPaket.getValueAt(row, 0);
            txtNama.setText((String) modelPaket.getValueAt(row, 1));
            txtKapasitas.setText((String) modelPaket.getValueAt(row, 2));
            
            String hargaStr = (String) modelPaket.getValueAt(row, 3);
            hargaStr = hargaStr.replace("Rp ", "").replace(",", "").replace(".", "");
            txtHarga.setText(hargaStr);
            
            txtKeterangan.setText((String) modelPaket.getValueAt(row, 4));
            
            setEditMode(true); // Set ke mode edit saat baris dipilih
        }
    }
    
    private void tambahPaket() {
        if (isEditMode) {
            JOptionPane.showMessageDialog(this, "Sedang dalam mode edit! Klik Batal untuk menambah data baru.");
            return;
        }
        
        String nama = txtNama.getText().trim();
        String kapasitas = txtKapasitas.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String keterangan = txtKeterangan.getText().trim();
        
        if (nama.isEmpty() || kapasitas.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, kapasitas, dan harga harus diisi!");
            return;
        }
        
        try {
            int harga = Integer.parseInt(hargaStr);
            if (harga <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus lebih dari 0!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO paket (nama, kapasitas, harga, keterangan) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nama);
                pstmt.setString(2, kapasitas);
                pstmt.setInt(3, harga);
                pstmt.setString(4, keterangan);
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Paket berhasil ditambahkan!");
                clearForm();
                loadData();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!");
        }
    }
    
    private void editPaket() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih paket yang akan diedit!");
            return;
        }
        
        String nama = txtNama.getText().trim();
        String kapasitas = txtKapasitas.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String keterangan = txtKeterangan.getText().trim();
        
        if (nama.isEmpty() || kapasitas.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, kapasitas, dan harga harus diisi!");
            return;
        }
        
        try {
            int harga = Integer.parseInt(hargaStr);
            if (harga <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus lebih dari 0!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE paket SET nama = ?, kapasitas = ?, harga = ?, keterangan = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nama);
                pstmt.setString(2, kapasitas);
                pstmt.setInt(3, harga);
                pstmt.setString(4, keterangan);
                pstmt.setInt(5, selectedId);
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Paket berhasil diupdate!");
                clearForm();
                setEditMode(false);
                loadData();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!");
        }
    }
    
    private void hapusPaket() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih paket yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus paket ini?", 
                                                   "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM paket WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Paket berhasil dihapus!");
            clearForm();
            setEditMode(false);
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtNama.setText("");
        txtKapasitas.setText("");
        txtHarga.setText("0");
        txtKeterangan.setText("");
        selectedId = -1;
        tablePaket.clearSelection();
    }
}