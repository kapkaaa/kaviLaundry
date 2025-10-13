package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class VoucherManagementForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtNamaPelanggan, txtJumlahVoucher;
    private JButton btnTambah, btnKurang, btnRefresh, btnTutup;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public VoucherManagementForm() {
        setUndecorated(true);
        initComponents();
        loadData();
        setLocationRelativeTo(null);
        updateWindowShape();
    }

    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color tambahBg = Color.decode("#6da395");   // Hijau untuk tambah
        Color kurangBg = Color.decode("#FF4444");   // Merah untuk kurang
        Color tableBg = Color.WHITE;

        setSize(600, 400);
        setBackground(new Color(0, 0, 0, 0));
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
        titleBar.setPreferredSize(new Dimension(600, 40));
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

        JLabel titleLabel = new JLabel("Manajemen Voucher Pelanggan", SwingConstants.CENTER);
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

        // Nama Pelanggan
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Nama Pelanggan:"), gbc);
        gbc.gridx = 1;
        txtNamaPelanggan = createStyledTextField(15);
        formPanel.add(txtNamaPelanggan, gbc);

        // Jumlah Voucher
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Jumlah Voucher:"), gbc);
        gbc.gridx = 1;
        txtJumlahVoucher = createStyledTextField(15);
        formPanel.add(txtJumlahVoucher, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        btnTambah = createActionButton("Tambah Voucher", tambahBg);
        btnKurang = createActionButton("Kurangi Voucher", kurangBg);
        btnRefresh = createActionButton("Refresh", Color.decode("#FFA500"));
        btnTutup = createActionButton("Tutup", Color.decode("#AAAAAA"));

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

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // =================== TABLE ===================
        String[] columns = {"ID", "Nama Pelanggan", "Total Voucher", "Bergabung"};
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
        table.setSelectionBackground(tambahBg);
        table.setSelectionForeground(Color.WHITE);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

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
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        tablePanel.setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Drag window
        addWindowDrag(titleBar);
        normalBounds = getBounds();

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
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.decode("#222222"));
        return field;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(Color.LIGHT_GRAY);
                } else if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
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
        button.setPreferredSize(new Dimension(120, 32));
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