/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {
    private JButton btnKelolaPegawai, btnKelolaHarga, btnKelolaAlat;
    private JButton btnRiwayatTransaksi, btnLaporanKeuangan, btnKelolaVoucher;
    private JButton btnLogout;
    
    public AdminDashboard() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Admin Dashboard - Kavi Laundry");
        setSize(700, 500);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 255));
        JLabel lblWelcome = new JLabel("Selamat Datang, " + UserSession.getCurrentUsername(), SwingConstants.CENTER);
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblWelcome);
        add(headerPanel, BorderLayout.NORTH);
        
        // Menu Panel
        JPanel menuPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        btnKelolaPegawai = createMenuButton("Kelola Pegawai", "👥");
        btnKelolaHarga = createMenuButton("Kelola Harga Layanan", "💰");
        btnKelolaAlat = createMenuButton("Kelola Alat", "🛠️");
        btnRiwayatTransaksi = createMenuButton("Riwayat Transaksi", "📋");
        btnLaporanKeuangan = createMenuButton("Laporan Keuangan", "📊");
        btnKelolaVoucher = createMenuButton("Kelola Voucher", "🎫");
        btnLogout = createMenuButton("Logout", "🚪");
        
        menuPanel.add(btnKelolaPegawai);
        menuPanel.add(btnKelolaHarga);
        menuPanel.add(btnKelolaAlat);
        menuPanel.add(btnRiwayatTransaksi);
        menuPanel.add(btnLaporanKeuangan);
        menuPanel.add(btnKelolaVoucher);
        menuPanel.add(btnLogout);
        
        add(menuPanel, BorderLayout.CENTER);
        
        // Event Listeners
        btnKelolaPegawai.addActionListener(e -> new KelolaPegawaiForm().setVisible(true));
        btnKelolaHarga.addActionListener(e -> new KelolaHargaForm().setVisible(true));
        btnKelolaAlat.addActionListener(e -> new KelolaAlatForm().setVisible(true));
        btnRiwayatTransaksi.addActionListener(e -> new RiwayatTransaksiForm().setVisible(true));
        btnLaporanKeuangan.addActionListener(e -> new LaporanKeuanganForm().setVisible(true));
        btnKelolaVoucher.addActionListener(e -> new VoucherManagementForm().setVisible(true));
        btnLogout.addActionListener(e -> logout());
    }
    
    private JButton createMenuButton(String text, String emoji) {
        JButton button = new JButton("<html><center>" + emoji + "<br>" + text + "</center></html>");
        button.setPreferredSize(new Dimension(150, 80));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.clearSession();
            this.dispose();
            new LoginForm().setVisible(true);
        }
    }
}