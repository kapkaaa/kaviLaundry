/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class KasirDashboard extends JFrame {
    private JButton btnInputTransaksi, btnRiwayatTransaksi, btnStatusPesanan, btnLogout;
    
    public KasirDashboard() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Kasir Dashboard - Kavi Laundry");
        setSize(500, 350);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(40, 167, 69));
        JLabel lblWelcome = new JLabel("Selamat Datang, " + UserSession.getCurrentUsername(), SwingConstants.CENTER);
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblWelcome);
        add(headerPanel, BorderLayout.NORTH);
        
        // Menu Panel
        JPanel menuPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        btnInputTransaksi = createMenuButton("Input Transaksi", "💳");
        btnRiwayatTransaksi = createMenuButton("Riwayat Transaksi", "📋");
        btnStatusPesanan = createMenuButton("Status Pesanan", "📦");
        btnLogout = createMenuButton("Logout", "🚪");
        
        menuPanel.add(btnInputTransaksi);
        menuPanel.add(btnRiwayatTransaksi);
        menuPanel.add(btnStatusPesanan);
        menuPanel.add(btnLogout);
        
        add(menuPanel, BorderLayout.CENTER);
        
        // Event Listeners
        btnInputTransaksi.addActionListener(e -> new InputTransaksiForm().setVisible(true));
        btnRiwayatTransaksi.addActionListener(e -> new RiwayatTransaksiForm().setVisible(true));
        btnStatusPesanan.addActionListener(e -> new StatusPesananForm().setVisible(true));
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