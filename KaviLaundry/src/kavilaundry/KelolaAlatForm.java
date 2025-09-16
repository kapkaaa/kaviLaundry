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
import java.util.Date;

public class KelolaAlatForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtNamaAlat;
    private JComboBox<String> cmbJenisAlat, cmbStatus;
    private JTextField txtMaintenanceDate;
    private JButton btnTambah, btnEdit, btnHapus, btnMaintenance, btnTutup;
    private int selectedId = -1;
    
    public KelolaAlatForm() {
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("Kelola Alat");
        setSize(800, 500);
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Alat"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nama Alat
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Alat:"), gbc);
        gbc.gridx = 1;
        txtNamaAlat = new JTextField(20);
        formPanel.add(txtNamaAlat, gbc);
        
        // Jenis Alat
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Jenis Alat:"), gbc);
        gbc.gridx = 1;
        cmbJenisAlat = new JComboBox<>(new String[]{"cuci", "setrika"});
        formPanel.add(cmbJenisAlat, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"tersedia", "digunakan", "maintenance"});
        formPanel.add(cmbStatus, gbc);
        
        // Maintenance Date
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Tanggal Maintenance:"), gbc);
        gbc.gridx = 1;
        txtMaintenanceDate = new JTextField(20);
        txtMaintenanceDate.setToolTipText("Format: yyyy-MM-dd");
        formPanel.add(txtMaintenanceDate, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnTambah = new JButton("Tambah");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnMaintenance = new JButton("Set Maintenance");
        btnTutup = new JButton("Tutup");
        
        btnTambah.addActionListener(e -> tambahAlat());
        btnEdit.addActionListener(e -> editAlat());
        btnHapus.addActionListener(e -> hapusAlat());
        btnMaintenance.addActionListener(e -> setMaintenance());
        btnTutup.addActionListener(e -> dispose());
        
        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnMaintenance);
        btnPanel.add(btnTutup);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        // Table
        String[] columns = {"ID", "Nama Alat", "Jenis", "Status", "Maintenance Date"};
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
            String sql = "SELECT * FROM alat ORDER BY id_alat";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_alat"),
                    rs.getString("nama_alat"),
                    rs.getString("jenis_alat"),
                    rs.getString("status"),
                    rs.getDate("maintenance_date")
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
            txtNamaAlat.setText((String) model.getValueAt(row, 1));
            cmbJenisAlat.setSelectedItem((String) model.getValueAt(row, 2));
            cmbStatus.setSelectedItem((String) model.getValueAt(row, 3));
            
            Date maintenanceDate = (Date) model.getValueAt(row, 4);
            if (maintenanceDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                txtMaintenanceDate.setText(sdf.format(maintenanceDate));
            } else {
                txtMaintenanceDate.setText("");
            }
        }
    }
    
    private void tambahAlat() {
        String namaAlat = txtNamaAlat.getText().trim();
        String jenisAlat = (String) cmbJenisAlat.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();
        String maintenanceDate = txtMaintenanceDate.getText().trim();
        
        if (namaAlat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama alat harus diisi!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO alat (nama_alat, jenis_alat, status, maintenance_date) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, namaAlat);
            pstmt.setString(2, jenisAlat);
            pstmt.setString(3, status);
            
            if (maintenanceDate.isEmpty()) {
                pstmt.setNull(4, java.sql.Types.DATE);
            } else {
                pstmt.setString(4, maintenanceDate);
            }
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Alat berhasil ditambahkan!");
            clearForm();
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void editAlat() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih alat yang akan diedit!");
            return;
        }
        
        String namaAlat = txtNamaAlat.getText().trim();
        String jenisAlat = (String) cmbJenisAlat.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();
        String maintenanceDate = txtMaintenanceDate.getText().trim();
        
        if (namaAlat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama alat harus diisi!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE alat SET nama_alat = ?, jenis_alat = ?, status = ?, maintenance_date = ? WHERE id_alat = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, namaAlat);
            pstmt.setString(2, jenisAlat);
            pstmt.setString(3, status);
            
            if (maintenanceDate.isEmpty()) {
                pstmt.setNull(4, java.sql.Types.DATE);
            } else {
                pstmt.setString(4, maintenanceDate);
            }
            
            pstmt.setInt(5, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Alat berhasil diupdate!");
            clearForm();
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void hapusAlat() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih alat yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus alat ini?", 
                                                   "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM alat WHERE id_alat = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Alat berhasil dihapus!");
            clearForm();
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void setMaintenance() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih alat yang akan di-maintenance!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE alat SET status = 'maintenance' WHERE id_alat = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status alat diubah ke maintenance!");
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtNamaAlat.setText("");
        cmbJenisAlat.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        txtMaintenanceDate.setText("");
        selectedId = -1;
    }
}