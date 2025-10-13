package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import javax.swing.text.*;

public class KelolaPegawaiForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtUsername, txtPassword, txtNamaLengkap, txtAlamat, txtNoTelepon;
    private JComboBox<String> cmbRole;
    private JButton btnTambah, btnEdit, btnHapus, btnTutup, btnBatal;
    private int selectedId = -1;
    private boolean isEditMode = false;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public KelolaPegawaiForm() {
        setUndecorated(true); // ⚠️ Penting: sebelum setVisible()
        initComponents();
        loadData();
        setLocationRelativeTo(null);
        setEditMode(false);
        updateWindowShape();
    }

    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");       // Background utama
        Color textMain = Color.decode("#222222");      // Warna teks
        Color buttonBg = Color.decode("#6da395");      // Tombol tambah
        Color editBg = Color.decode("#FFA500");        // Tombol edit
        Color deleteBg = Color.decode("#FF4444");      // Tombol hapus
        Color tableBg = Color.WHITE;                   // Background tabel

        setSize(800, 550);
        setBackground(new Color(0, 0, 0, 0));          // Transparan agar shape bisa bekerja
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // =================== PANEL UTAMA DENGAN ROUNDED BACKGROUND ===================
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);

        // =================== macOS TITLE BAR ===================
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        titleBar.setPreferredSize(new Dimension(800, 40));
        titleBar.setOpaque(false);

        JButton btnClose = createMacOSButton(new Color(0xFF5F57));
        JButton btnMinimize = createMacOSButton(new Color(0xFFBD2E));
        JButton btnMaximize = createMacOSButton(new Color(0x28CA42));

        btnClose.addActionListener(e -> dispose());
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMaximize.addActionListener(e -> toggleMaximize());

        titleBar.add(btnClose);
        titleBar.add(btnMinimize);
        titleBar.add(btnMaximize);

        JLabel titleLabel = new JLabel("Kelola Pegawai", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textMain);
        titleLabel.setOpaque(false);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalGlue());

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // =================== FORM PANEL — TRANSPARAN ===================
        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Baris pertama: Username dan Password
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = createStyledTextField(15);
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(createLabel("Password:"), gbc);
        gbc.gridx = 3;
        txtPassword = new JPasswordField(15);
        styleTextField((JTextField) txtPassword);
        formPanel.add(txtPassword, gbc);

        // Baris kedua: Nama Lengkap dan Alamat
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        txtNamaLengkap = createStyledTextField(15);
        formPanel.add(txtNamaLengkap, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(createLabel("Alamat:"), gbc);
        gbc.gridx = 3;
        txtAlamat = createStyledTextField(15);
        formPanel.add(txtAlamat, gbc);

        // Baris ketiga: No. Telepon dan Role
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("No. Telepon:"), gbc);
        gbc.gridx = 1;
        txtNoTelepon = createStyledTextField(15);
        ((AbstractDocument) txtNoTelepon.getDocument()).setDocumentFilter(new NumberOnlyFilter());
        formPanel.add(txtNoTelepon, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        formPanel.add(createLabel("Role:"), gbc);
        gbc.gridx = 3;
        cmbRole = new JComboBox<>(new String[]{"kasir", "admin"});
        styleComboBox(cmbRole);
        formPanel.add(cmbRole, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        btnTambah = createActionButton("Tambah", buttonBg);
        btnEdit = createActionButton("Edit", editBg);
        btnHapus = createActionButton("Hapus", deleteBg);
        btnBatal = createActionButton("Batal", Color.GRAY);
        btnTutup = createActionButton("Tutup", Color.decode("#AAAAAA"));

        btnTambah.addActionListener(e -> tambahPegawai());
        btnEdit.addActionListener(e -> editPegawai());
        btnHapus.addActionListener(e -> hapusPegawai());
        btnBatal.addActionListener(e -> batalEdit());
        btnTutup.addActionListener(e -> dispose());

        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBatal);
        btnPanel.add(btnTutup);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // =================== TABLE WITH ROUNDED CORNERS ===================
        String[] columns = {"ID", "Username", "Nama Lengkap", "Alamat", "No. Telepon", "Role", "Created At"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(buttonBg);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Wrap table in a rounded panel
        JPanel tablePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(tableBg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Hapus border default
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Drag window
        addWindowDrag(titleBar);
        normalBounds = getBounds();

        // ⚠️ Pastikan opacity dan shape
        setOpacity(1.0f);
        updateWindowShape();
    }

    // =================== HELPER METHODS ===================
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.decode("#222222"));
        return label;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.decode("#222222"));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.decode("#222222"));
    }

    private JButton createActionButton(String text, Color activeColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor;
                if (!isEnabled()) {
                    bgColor = Color.LIGHT_GRAY; // Warna disabled
                } else if (getModel().isPressed()) {
                    bgColor = activeColor.darker();
                } else if (getModel().isRollover()) {
                    bgColor = activeColor.brighter();
                } else {
                    bgColor = activeColor;
                }

                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 32));
        return button;
    }

    // =================== macOS BUTTONS ===================
    private JButton createMacOSButton(Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());

                if (getModel().isRollover()) {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1.2f));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    if (color.equals(new Color(0xFF5F57))) {
                        g2.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                        g2.drawLine(cx + 3, cy - 3, cx - 3, cy + 3);
                    } else if (color.equals(new Color(0xFFBD2E))) {
                        g2.drawLine(cx - 3, cy, cx + 3, cy);
                    } else if (color.equals(new Color(0x28CA42))) {
                        if (isMaximized) {
                            g2.drawRect(cx - 2, cy - 1, 3, 3);
                            g2.drawRect(cx - 1, cy - 2, 3, 3);
                        } else {
                            g2.drawRect(cx - 2, cy - 2, 4, 4);
                        }
                    }
                }
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(14, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setPreferredSize(new Dimension(15, 15));
                button.revalidate();
                button.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setPreferredSize(new Dimension(14, 14));
                button.revalidate();
                button.repaint();
            }
        });
        return button;
    }

    // =================== UTILITAS WINDOW ===================
    private void toggleMaximize() {
        if (isMaximized) {
            setBounds(normalBounds);
            isMaximized = false;
        } else {
            normalBounds = getBounds();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screenBounds = ge.getMaximumWindowBounds();
            setBounds(screenBounds);
            isMaximized = true;
        }
        updateWindowShape();
    }

    private void addWindowDrag(Component comp) {
        comp.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });
        comp.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized) {
                    Point curr = e.getLocationOnScreen();
                    setLocation(curr.x - mousePoint.x, curr.y - mousePoint.y);
                }
            }
        });
    }

    private void updateWindowShape() {
        if (!isMaximized) {
            int arc = 20;
            Shape shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc);
            setShape(shape);
        } else {
            setShape(null);
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        updateWindowShape();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateWindowShape();
    }

    // =================== LOGIC FORM ===================
    class NumberOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                throws BadLocationException {
            if (string.matches("\\d*")) {
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
            if (text.matches("\\d*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        btnTambah.setEnabled(!editMode);
        btnEdit.setEnabled(editMode);
        btnHapus.setEnabled(editMode);
        btnBatal.setVisible(editMode);
    }

    private void batalEdit() {
        clearForm();
        setEditMode(false);
        table.clearSelection();
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
            txtPassword.setText("");
            setEditMode(true);
        }
    }

    private void tambahPegawai() {
        if (isEditMode) {
            JOptionPane.showMessageDialog(this, "Sedang dalam mode edit! Klik Batal untuk menambah data baru.");
            return;
        }
        
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
            String checkUserSql = "SELECT COUNT(*) FROM user WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkUserSql);
            checkStmt.setString(1, username);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            
            if (checkRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username sudah ada! Pilih username lain.");
                return;
            }
            
            String getRoleIdSql = "SELECT id_role FROM role WHERE nama_role = ?";
            PreparedStatement getRoleStmt = conn.prepareStatement(getRoleIdSql);
            getRoleStmt.setString(1, role);
            ResultSet roleRs = getRoleStmt.executeQuery();
            
            if (!roleRs.next()) {
                JOptionPane.showMessageDialog(this, "Role tidak ditemukan!");
                return;
            }
            
            int roleId = roleRs.getInt("id_role");
            
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
            String checkUserSql = "SELECT COUNT(*) FROM user WHERE username = ? AND id_user != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkUserSql);
            checkStmt.setString(1, username);
            checkStmt.setInt(2, selectedId);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            
            if (checkRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username sudah ada! Pilih username lain.");
                return;
            }
            
            String getRoleIdSql = "SELECT id_role FROM role WHERE nama_role = ?";
            PreparedStatement getRoleStmt = conn.prepareStatement(getRoleIdSql);
            getRoleStmt.setString(1, role);
            ResultSet roleRs = getRoleStmt.executeQuery();
            
            if (!roleRs.next()) {
                JOptionPane.showMessageDialog(this, "Role tidak ditemukan!");
                return;
            }
            
            int roleId = roleRs.getInt("id_role");
            
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
            setEditMode(false);
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
            setEditMode(false);
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