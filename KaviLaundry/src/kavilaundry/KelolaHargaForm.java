package kavilaundry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import javax.swing.text.*;

public class KelolaHargaForm extends JFrame {
    private JTable tablePaket;
    private DefaultTableModel modelPaket;
    private JTextField txtNama, txtKapasitas, txtHarga, txtKeterangan;
    private JButton btnTambah, btnEdit, btnHapus, btnBatal, btnTutup;
    private int selectedId = -1;
    private boolean isEditMode = false;
    private Point mousePoint;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public KelolaHargaForm() {
        setUndecorated(true);
        initComponents();
        loadData();
        setLocationRelativeTo(null);
        setEditMode(false);
        updateWindowShape();
    }

    private void initComponents() {
        Color bgColor = Color.decode("#b3ebf2");
        Color textMain = Color.decode("#222222");
        Color buttonBg = Color.decode("#6da395");
        Color editBg = Color.decode("#FFA500");
        Color deleteBg = Color.decode("#FF4444");
        Color tableBg = Color.WHITE;

        setSize(700, 500);
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
        titleBar.setPreferredSize(new Dimension(700, 40));
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

        JLabel titleLabel = new JLabel("Kelola Harga Layanan", SwingConstants.CENTER);
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

        // Nama
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Nama Paket:"), gbc);
        gbc.gridx = 1;
        txtNama = createStyledTextField(20);
        txtNama.setPreferredSize(new Dimension(200, 25)); // ⭐ Tetapkan lebar tetap
        formPanel.add(txtNama, gbc);

        // Kapasitas
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Kapasitas:"), gbc);
        gbc.gridx = 1;
        txtKapasitas = createStyledTextField(20);
        txtKapasitas.setPreferredSize(new Dimension(200, 25)); // ⭐ Tetapkan lebar tetap
        formPanel.add(txtKapasitas, gbc);

        // Harga
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Harga:"), gbc);
        gbc.gridx = 1;
        txtHarga = createStyledTextField(20);
        txtHarga.setPreferredSize(new Dimension(200, 25)); // ⭐ Tetapkan lebar tetap
        ((AbstractDocument) txtHarga.getDocument()).setDocumentFilter(new NumberOnlyFilter());
        formPanel.add(txtHarga, gbc);

        // Keterangan
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Keterangan:"), gbc);
        gbc.gridx = 1;
        txtKeterangan = createStyledTextField(20);
        txtKeterangan.setPreferredSize(new Dimension(200, 25)); // ⭐ Tetapkan lebar tetap
        formPanel.add(txtKeterangan, gbc);

        // Buttons — letakkan di baris baru, kolom 0-1
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        btnTambah = createActionButton("Tambah", buttonBg);
        btnEdit = createActionButton("Edit", editBg);
        btnHapus = createActionButton("Hapus", deleteBg);
        btnBatal = createActionButton("Batal", Color.GRAY);
        btnTutup = createActionButton("Tutup", Color.decode("#AAAAAA"));

        btnTambah.addActionListener(e -> tambahPaket());
        btnEdit.addActionListener(e -> editPaket());
        btnHapus.addActionListener(e -> hapusPaket());
        btnBatal.addActionListener(e -> batalEdit());
        btnTutup.addActionListener(e -> dispose());

        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBatal);
        btnPanel.add(btnTutup);

        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // =================== TABLE WITH ROUNDED CORNERS ===================
        String[] columns = {"ID", "Nama Paket", "Kapasitas", "Harga", "Keterangan"};
        modelPaket = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePaket = new JTable(modelPaket);
        tablePaket.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePaket.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePaket.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablePaket.setRowHeight(25);
        tablePaket.setSelectionBackground(buttonBg);
        tablePaket.setSelectionForeground(Color.WHITE);
        tablePaket.setFillsViewportHeight(true);

        tablePaket.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablePaket);
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
        btnBatal.setEnabled(editMode);
    }

    private void batalEdit() {
        clearForm();
        setEditMode(false);
        tablePaket.clearSelection();
    }

    private void loadData() {
        modelPaket.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM paket ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("kapasitas"),
                    "Rp " + String.format("%,.0f", rs.getDouble("harga")),
                    rs.getString("keterangan")
                };
                modelPaket.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void selectRow() {
        int row = tablePaket.getSelectedRow();
        if (row >= 0) {
            selectedId = (Integer) modelPaket.getValueAt(row, 0);
            txtNama.setText((String) modelPaket.getValueAt(row, 1));
            txtKapasitas.setText((String) modelPaket.getValueAt(row, 2));
            
            String hargaStr = (String) modelPaket.getValueAt(row, 3);
            hargaStr = hargaStr.replace("Rp ", "").replace(",", "").replace(".", "");
            txtHarga.setText(hargaStr);
            
            txtKeterangan.setText((String) modelPaket.getValueAt(row, 4));
            
            setEditMode(true);
        }
    }

    private void tambahPaket() {
        if (isEditMode) {
            JOptionPane.showMessageDialog(this, "Sedang dalam mode edit! Klik Batal untuk menambah data baru.");
            return;
        }
        
        String nama = txtNama.getText().trim();
        String kapasitas = txtKapasitas.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String keterangan = txtKeterangan.getText().trim();
        
        if (nama.isEmpty() || kapasitas.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, kapasitas, dan harga harus diisi!");
            return;
        }
        
        try {
            int harga = Integer.parseInt(hargaStr);
            if (harga <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus lebih dari 0!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO paket (nama, kapasitas, harga, keterangan) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nama);
                pstmt.setString(2, kapasitas);
                pstmt.setInt(3, harga);
                pstmt.setString(4, keterangan);
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Paket berhasil ditambahkan!");
                clearForm();
                loadData();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!");
        }
    }

    private void editPaket() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih paket yang akan diedit!");
            return;
        }
        
        String nama = txtNama.getText().trim();
        String kapasitas = txtKapasitas.getText().trim();
        String hargaStr = txtHarga.getText().trim();
        String keterangan = txtKeterangan.getText().trim();
        
        if (nama.isEmpty() || kapasitas.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, kapasitas, dan harga harus diisi!");
            return;
        }
        
        try {
            int harga = Integer.parseInt(hargaStr);
            if (harga <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus lebih dari 0!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE paket SET nama = ?, kapasitas = ?, harga = ?, keterangan = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nama);
                pstmt.setString(2, kapasitas);
                pstmt.setInt(3, harga);
                pstmt.setString(4, keterangan);
                pstmt.setInt(5, selectedId);
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Paket berhasil diupdate!");
                clearForm();
                setEditMode(false);
                loadData();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!");
        }
    }

    private void hapusPaket() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih paket yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus paket ini?", 
                                                   "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM paket WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedId);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Paket berhasil dihapus!");
            clearForm();
            setEditMode(false);
            loadData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtNama.setText("");
        txtKapasitas.setText("");
        txtHarga.setText("");
        txtKeterangan.setText("");
        selectedId = -1;
        tablePaket.clearSelection();
    }
}