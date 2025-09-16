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

public class VoucherManagementForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtNamaPelanggan, txtJumlahVoucher;
    private JButton btnTambah, btnKurang, btnRefresh, btnTutup;
    
    public VoucherManagementForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Manajemen Voucher Pelanggan");
        setSize(600, 400);
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Kelola Voucher"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nama Pelanggan
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Pelanggan:"), gbc);
        gbc.gridx = 1;
        txtNamaPelanggan = new JTextField(15);
        formPanel.add(txtNamaPelanggan, gbc);
        
        // Jumlah Voucher
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Jumlah Voucher:"), gbc);
        gbc.gridx = 1;
        txtJumlahVoucher = new JTextField(15);
        formPanel.add(txtJumlahVoucher, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnTambah = new JButton("Tambah Voucher");
        btnKurang = new JButton("Kurangi Voucher");
        btnRefresh = new JButton("Refresh");
        btnTutup = new JButton("Tutup");
        
        btnTambah.addActionListener(e -> tambahVoucher());
        btnKurang.addActionListener(e -> kurangiVoucher());
        btnRefresh.addActionListener(e -> loadData());
        btnTutup.addActionListener(e -> dispose());
        
        btnPanel.add(btnTambah);
        btnPanel.add(btnKurang);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnTutup);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        // Table
        String[] columns = {"ID", "Nama Pelanggan", "Total Voucher", "Bergabung"};
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
            String sql = "SELECT id_pelanggan, nama, total_voucher, created_at FROM pelanggan ORDER BY nama";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_pelanggan"),
                    rs.getString("nama"),
                    rs.getInt("total_voucher"),
                    rs.getTimestamp("created_at")
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
            txtNamaPelanggan.setText((String) model.getValueAt(row, 1));
            txtJumlahVoucher.setText(model.getValueAt(row, 2).toString());
        }
    }
    
    private void tambahVoucher() {
        String namaPelanggan = txtNamaPelanggan.getText().trim();
        String jumlahStr = txtJumlahVoucher.getText().trim();
        
        if (namaPelanggan.isEmpty() || jumlahStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pelanggan dan jumlah voucher harus diisi!");
            return;
        }
        
        try {
            int jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah voucher harus lebih dari 0!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE pelanggan SET total_voucher = total_voucher + ? WHERE nama = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, jumlah);
                pstmt.setString(2, namaPelanggan);
                
                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Voucher berhasil ditambahkan!");
                    loadData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Pelanggan tidak ditemukan!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah voucher harus berupa angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void kurangiVoucher() {
        String namaPelanggan = txtNamaPelanggan.getText().trim();
        String jumlahStr = txtJumlahVoucher.getText().trim();
        
        if (namaPelanggan.isEmpty() || jumlahStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pelanggan dan jumlah voucher harus diisi!");
            return;
        }
        
        try {
            int jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah voucher harus lebih dari 0!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Cek voucher yang tersedia
                String checkSql = "SELECT total_voucher FROM pelanggan WHERE nama = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, namaPelanggan);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    int voucherTersedia = rs.getInt("total_voucher");
                    if (jumlah > voucherTersedia) {
                        JOptionPane.showMessageDialog(this, "Voucher tidak mencukupi! Tersedia: " + voucherTersedia);
                        return;
                    }
                    
                    // Update voucher
                    String sql = "UPDATE pelanggan SET total_voucher = total_voucher - ? WHERE nama = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, jumlah);
                    pstmt.setString(2, namaPelanggan);
                    
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Voucher berhasil dikurangi!");
                    loadData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Pelanggan tidak ditemukan!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah voucher harus berupa angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtNamaPelanggan.setText("");
        txtJumlahVoucher.setText("");
    }
}
