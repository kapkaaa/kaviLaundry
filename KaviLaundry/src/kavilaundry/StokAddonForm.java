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

public class StokAddonForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtNamaAddon, txtHarga, txtStok;
    private JButton btnTambah, btnEdit, btnHapus, btnTutup;
    private int selectedId = -1;
    
    public StokAddonForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Kelola Stok Addon");
        setSize(600, 400);
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Addon"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nama Addon
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Addon:"), gbc);
        gbc.gridx = 1;
        txtNamaAddon = new JTextField(15);
        formPanel.add(txtNamaAddon, gbc);
        
        // Harga
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1;
        txtHarga = new JTextField(15);
        formPanel.add(txtHarga, gbc);
        
        // Stok
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Stok:"), gbc);
        gbc.gridx = 1;
        txtStok = new JTextField(15);
        formPanel.add(txtStok, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnTambah = new JButton("Tambah");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnTutup = new JButton("Tutup");
        
        btnTambah.addActionListener(e -> tambahAddon());
        btnEdit.addActionListener(e -> editAddon());
        btnHapus.addActionListener(e -> hapusAddon());
        btnTutup.addActionListener(e -> dispose());
        
        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnTutup);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        // Table
        String[] columns = {"ID", "Nama Addon", "Harga", "Stok"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM addon ORDER BY nama_addon";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_addon"),
                    rs.getString("nama_addon"),
                    "Rp " + String.format("%,.0f", rs.getDouble("harga")),
                    rs.getInt("stok")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void selectRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            selectedId = (Integer) model.getValueAt(row, 0);
            txtNamaAddon.setText((String) model.getValueAt(row, 1));
            
            String hargaStr = (String) model.getValueAt(row, 2);
            hargaStr = hargaStr.replace("Rp ", "").replace(",", "");
            txtHarga.setText(hargaStr);
            
            txtStok.setText(model.getValueAt(row, 3).toString());
        }
    }
    
    private void tambahAddon() {
        String namaAddon = txtNamaAddon.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String stokStr = txtStok.getText().trim();
        
        if (namaAddon.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }
        
        try {
            double harga = Double.parseDouble(hargaStr);
            int stok = Integer.parseInt(stokStr);
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO addon (nama_addon, harga, stok) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, namaAddon);
                pstmt.setDouble(2, harga);
                pstmt.setInt(3, stok);
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Addon berhasil ditambahkan!");
                clearForm();
                loadData();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga dan stok harus berupa angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void editAddon() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih addon yang akan diedit!");
            return;
        }
        
        String namaAddon = txtNamaAddon.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String stokStr = txtStok.getText().trim();
        
        if (namaAddon.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }
        
        try {
            double harga = Double.parseDouble(hargaStr);
            int stok = Integer.parseInt(stokStr);
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE addon SET nama_addon = ?, harga = ?, stok = ? WHERE id_addon = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, namaAddon);
                pstmt.setDouble(2, harga);
                pstmt.setInt(3, stok);
                pstmt.setInt(4, selectedId);
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Addon berhasil diupdate!");
                clearForm();
                loadData();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga dan stok harus berupa angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void hapusAddon() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih addon yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus addon ini?", 
                                                   "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM addon WHERE id_addon = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Addon berhasil dihapus!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtNamaAddon.setText("");
        txtHarga.setText("");
        txtStok.setText("");
        selectedId = -1;
    }
}
