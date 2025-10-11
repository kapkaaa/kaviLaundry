/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class Splash extends JWindow {
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer timer;
    private int progress = 0;
    
    public Splash() {
        initComponents();
        setLocationRelativeTo(null);
        startSplashScreen();
    }
    
    private void initComponents() {
        // Set ukuran splash screen
        setSize(400, 300);
        
        // Buat panel utama dengan background gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(102, 126, 234),
                    0, getHeight(), new Color(118, 75, 162)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Rounded border
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 20, 20));
            }
        };
        
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel untuk logo dan text
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Logo (menggunakan teks sebagai logo sederhana)
        JLabel logoLabel = createLogoLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nama aplikasi
        JLabel appNameLabel = new JLabel("KAVI LAUNDRY");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        appNameLabel.setForeground(Color.WHITE);
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Tagline
        JLabel taglineLabel = new JLabel("Layanan Laundry Gacor");
        taglineLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        taglineLabel.setForeground(new Color(255, 255, 255, 180));
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        progressBar.setForeground(new Color(102, 126, 234));
        progressBar.setBackground(Color.WHITE);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Status Label
        statusLabel = new JLabel("Memuat aplikasi...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(255, 255, 255, 150));
        
        // Version Label
        JLabel versionLabel = new JLabel("v1.0.0", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(255, 255, 255, 100));
        
        // Menambahkan komponen ke center panel
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(appNameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(taglineLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 70)));
        
        
        // Panel untuk progress bar dan status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); 
            
        // Bungkus progressBar dalam panel agar tidak terlalu mepet
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // kiri-kanan lebih lebar
        progressPanel.add(progressBar, BorderLayout.CENTER);

        bottomPanel.add(progressPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 8))); // jarak ke statusLabel
        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 12))); // jarak ke versionLabel
        bottomPanel.add(versionLabel);

        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set rounded corners
        setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
    }
    
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();

        // Load image dari file
        ImageIcon originalIcon = new ImageIcon("src/images/Logo.jpg");
            
        // Resize image
        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);
            
        logoLabel.setIcon(scaledIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        logoLabel.setPreferredSize(new Dimension(120, 120));
        return logoLabel;
    }
    
    private void startSplashScreen() {
        // Array status loading
        String[] loadingSteps = {
            "Memuat aplikasi...",
            "Menginisialisasi komponen...",
            "Memuat data...",
            "Menyiapkan antarmuka...",
            "Hampir selesai...",
            "Selesai!"
        };
        
        timer = new Timer(500, new ActionListener() {
            private int stepIndex = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 20;
                progressBar.setValue(progress);
                
                if (stepIndex < loadingSteps.length) {
                    statusLabel.setText(loadingSteps[stepIndex]);
                    stepIndex++;
                }
                
                if (progress >= 100) {
                    timer.stop();
                    // Delay sebentar sebelum menutup splash screen
                    Timer closeTimer = new Timer(800, evt -> {
                        closeSplashScreen();
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }
            }
        });
        
        timer.start();
    }
    
    private void closeSplashScreen() {
        setVisible(false);
        dispose();
        
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
    
}