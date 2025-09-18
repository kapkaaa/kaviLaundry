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

public class KelolaPegawaiForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtUsername, txtPassword, txtNamaLengkap, txtAlamat, txtNoTelepon;
    private JComboBox<String> cmbRole;
    private JButton btnTambah, btnEdit, btnHapus, btnTutup;
    private int selectedId = -1;
    
    public KelolaPegawaiForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Kelola Pegawai");
        setSize(800, 500);
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Pegawai"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Baris pertama: Username dan Password
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        formPanel.add(txtUsername, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 3;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);
        
        // Baris kedua: Nama Lengkap dan Alamat
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        txtNamaLengkap = new JTextField(15);
        formPanel.add(txtNamaLengkap, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 3;
        txtAlamat = new JTextField(15);
        formPanel.add(txtAlamat, gbc);
        
        // Baris ketiga: No. Telepon dan Role (di tengah)
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("No. Telepon:"), gbc);
        gbc.gridx = 1;
        txtNoTelepon = new JTextField(15);
        formPanel.add(txtNoTelepon, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 3;
        cmbRole = new JComboBox<>(new String[]{"kasir", "admin"});
        formPanel.add(cmbRole, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnTambah = new JButton("Tambah");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnTutup = new JButton("Tutup");
        
        btnTambah.addActionListener(e -> tambahPegawai());
        btnEdit.addActionListener(e -> editPegawai());
        btnHapus.addActionListener(e -> hapusPegawai());
        btnTutup.addActionListener(e -> dispose());
        
        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnTutup);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);
        
        // Table
        String[] columns = {"ID", "Username", "Nama Lengkap", "Alamat", "No. Telepon", "Role", "Created At"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Nama Lengkap
        table.getColumnModel().getColumn(3).setPreferredWidth(200);  // Alamat
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // No. Telepon
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Role
        table.getColumnModel().getColumn(6).setPreferredWidth(150);  // Created At
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT u.id_user, u.username, u.nama, u.alamat, u.no_telp, r.nama_role, u.created_at " +
                        "FROM user u JOIN role r ON u.role_id = r.id_role";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("nama"),
                    rs.getString("alamat"),
                    rs.getString("no_telp"),
                    rs.getString("nama_role"),
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
            selectedId = (Integer) model.getValueAt(row, 0);
            txtUsername.setText((String) model.getValueAt(row, 1));
            txtNamaLengkap.setText((String) model.getValueAt(row, 2));
            txtAlamat.setText((String) model.getValueAt(row, 3));
            txtNoTelepon.setText((String) model.getValueAt(row, 4));
            cmbRole.setSelectedItem((String) model.getValueAt(row, 5));
            txtPassword.setText(""); // Clear password field for security
        }
    }
    
    private void tambahPegawai() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String noTelepon = txtNoTelepon.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        
        if (username.isEmpty() || password.isEmpty() || namaLengkap.isEmpty() || 
            alamat.isEmpty() || noTelepon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get role ID
            String getRoleIdSql = "SELECT id_role FROM role WHERE nama_role = ?";
            PreparedStatement getRoleStmt = conn.prepareStatement(getRoleIdSql);
            getRoleStmt.setString(1, role);
            ResultSet roleRs = getRoleStmt.executeQuery();
            
            if (!roleRs.next()) {
                JOptionPane.showMessageDialog(this, "Role tidak ditemukan!");
                return;
            }
            
            int roleId = roleRs.getInt("id_role");
            
            // Insert user
            String sql = "INSERT INTO user (username, password, nama, alamat, no_telp, role_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, namaLengkap);
            pstmt.setString(4, alamat);
            pstmt.setString(5, noTelepon);
            pstmt.setInt(6, roleId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pegawai berhasil ditambahkan!");
            clearForm();
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void editPegawai() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pegawai yang akan diedit!");
            return;
        }
        
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String noTelepon = txtNoTelepon.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        
        if (username.isEmpty() || namaLengkap.isEmpty() || alamat.isEmpty() || noTelepon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, Nama Lengkap, Alamat, dan No. Telepon harus diisi!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get role ID
            String getRoleIdSql = "SELECT id_role FROM role WHERE nama_role = ?";
            PreparedStatement getRoleStmt = conn.prepareStatement(getRoleIdSql);
            getRoleStmt.setString(1, role);
            ResultSet roleRs = getRoleStmt.executeQuery();
            
            if (!roleRs.next()) {
                JOptionPane.showMessageDialog(this, "Role tidak ditemukan!");
                return;
            }
            
            int roleId = roleRs.getInt("id_role");
            
            // Update user
            String sql;
            if (password.isEmpty()) {
                sql = "UPDATE user SET username = ?, nama = ?, alamat = ?, no_telp = ?, role_id = ? WHERE id_user = ?";
            } else {
                sql = "UPDATE user SET username = ?, password = ?, nama = ?, alamat = ?, no_telp = ?, role_id = ? WHERE id_user = ?";
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            if (password.isEmpty()) {
                pstmt.setString(2, namaLengkap);
                pstmt.setString(3, alamat);
                pstmt.setString(4, noTelepon);
                pstmt.setInt(5, roleId);
                pstmt.setInt(6, selectedId);
            } else {
                pstmt.setString(2, password);
                pstmt.setString(3, namaLengkap);
                pstmt.setString(4, alamat);
                pstmt.setString(5, noTelepon);
                pstmt.setInt(6, roleId);
                pstmt.setInt(7, selectedId);
            }
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pegawai berhasil diupdate!");
            clearForm();
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void hapusPegawai() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pegawai yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus pegawai ini?", 
                                                   "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM user WHERE id_user = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pegawai berhasil dihapus!");
            clearForm();
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtNamaLengkap.setText("");
        txtAlamat.setText("");
        txtNoTelepon.setText("");
        cmbRole.setSelectedIndex(0);
        selectedId = -1;
    }
}