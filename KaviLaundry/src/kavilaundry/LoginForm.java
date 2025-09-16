/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kavilaundry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    
    public LoginForm() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login - Kavi Laundry");
        setSize(400, 300);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel lblTitle = new JLabel("KAVI LAUNDRY", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 30, 0);
        add(lblTitle, gbc);
        
        // Username
        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 10);
        add(lblUsername, gbc);
        
        txtUsername = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 20);
        add(txtUsername, gbc);
        
        // Password
        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 10, 10);
        add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 10, 20);
        add(txtPassword, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnLogin = new JButton("Login");
        btnExit = new JButton("Exit");
        
        btnLogin.addActionListener(e -> login());
        btnExit.addActionListener(e -> System.exit(0));
        
        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 20, 0);
        add(btnPanel, gbc);
        
        // Enter key listener
        getRootPane().setDefaultButton(btnLogin);
    }
    
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT u.*, r.nama_role FROM user u " +
                        "JOIN role r ON u.role_id = r.id_role " +
                        "WHERE u.username = ? AND u.password = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("nama_role");
                int userId = rs.getInt("id_user");
                
                // Store user session
                UserSession.setCurrentUser(userId, username, roleId, roleName);
                
                this.dispose();
                
                if (roleId == 1) { // Kasir
                    new KasirDashboard().setVisible(true);
                } else if (roleId == 2) { // Admin
                    new AdminDashboard().setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username atau password salah!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}