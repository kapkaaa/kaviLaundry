# Aplikasi Kasir Laundry Kavi

Aplikasi desktop untuk manajemen kasir laundry yang dikembangkan menggunakan Java Swing dan NetBeans IDE. Sistem ini menyediakan solusi lengkap untuk pengelolaan transaksi, karyawan, inventori, dan laporan keuangan.

## Fitur Utama

### Dashboard Admin
- **Kelola Data Karyawan** - CRUD pegawai dengan role kasir/admin
- **Kelola Harga Layanan** - Manajemen paket layanan dengan harga per kg
- **Kelola Alat** - Manajemen mesin cuci, setrika dengan jadwal maintenance
- **Riwayat Transaksi** - Laporan semua transaksi dengan filter pencarian
- **Laporan Keuangan** - Report pendapatan dengan export CSV/Excel
- **Kelola Voucher** - Manajemen voucher pelanggan

### Dashboard Kasir
- **Input Transaksi** - Sistem transaksi dengan validasi paket dan voucher
- **Cetak Struk** - Generate struk pembayaran otomatis
- **Status Pesanan** - Update status real-time dan pembayaran
- **Riwayat Transaksi** - View dan pencarian transaksi

### Sistem Voucher Otomatis
- Setiap transaksi mendapat 1 voucher
- 7 voucher = gratis cuci 7kg (hanya untuk paket Cuci Baik itu Cuci Basah, Cuci Kering dan Cuci Setrika)
- Voucher mengurangi berat yang harus dibayar (contoh: cuci 10kg dengan voucher = bayar 3kg)
- Validasi otomatis voucher yang tersedia

### Sistem Pembayaran Fleksibel
- **Metode Pembayaran**: Cash atau QRIS
- **Waktu Pembayaran**: Bayar Sekarang atau Bayar Setelah Selesai
- **Status Pembayaran**: Belum Bayar, Pending, Lunas
- **Tracking Pembayaran**: Update status pembayaran terpisah dari status pesanan

### Logika Bisnis Per Kg
- Semua paket layanan dihitung per kilogram
- Harga fleksibel berdasarkan jenis layanan
- Voucher berlaku hanya untuk paket "Cuci" (tidak termasuk Setrika)

## Persyaratan Sistem

### Software yang Dibutuhkan
- Java Development Kit (JDK) 8 atau lebih tinggi
- NetBeans IDE 12 atau lebih tinggi
- MySQL Server 5.7 atau MariaDB 10.4
- MySQL Connector/J (JDBC Driver)

### Library Tambahan
- MySQL Connector/J untuk koneksi database
- Java Swing untuk GUI (built-in)

## Struktur Database

### Tabel Utama
```sql
- user (id_user, username, password, role_id)
- role (id_role, nama_role)
- pelanggan (id_pelanggan, nama, total_voucher)
- paket (id, nama, kapasitas, harga, keterangan)
- transaksi (id_transaksi, id_pelanggan, id_jenis, berat_kg, total_biaya, status_pesanan, pembayaran)
- alat (id_alat, nama_alat, jenis_alat, status, maintenance_date)
- laporan_keuangan (id_laporan, tanggal_laporan, total_pendapatan, total_transaksi)
```

## Instalasi dan Setup

### 1. Setup Database
```sql
-- Buat database baru
CREATE DATABASE kavi_laundry;

-- Import file SQL yang disediakan
-- Jalankan script database dari file dump
```

### 2. Setup NetBeans Project
1. Buat Java Application baru di NetBeans
2. Tambahkan MySQL Connector/J ke Libraries
3. Copy semua file Java ke src folder
4. Build dan run project

### 3. Konfigurasi Database Connection
Edit file `DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/kavi_laundry";
private static final String USERNAME = "root"; // Sesuaikan username MySQL
private static final String PASSWORD = "";     // Sesuaikan password MySQL
```

### 4. Login Credentials (Default)
**Admin:**
- Username: `admin1`
- Password: `admin123`

**Kasir:**
- Username: `kasir1`  
- Password: `kasir123`

## Cara Penggunaan

### Input Transaksi (Kasir)
1. Login sebagai kasir
2. Pilih "Input Transaksi"
3. Isi data pelanggan dan pilih paket layanan
4. Masukkan berat cucian (semua paket per kg)
5. Pilih metode dan waktu pembayaran
6. Gunakan voucher jika tersedia (7 voucher untuk gratis 7kg)
7. Hitung total dan simpan transaksi
8. Cetak struk untuk pelanggan

### Manajemen Status (Kasir)
1. Buka "Status Pesanan"
2. Update status pesanan: diterima → dicuci → dijemur → setrika → selesai
3. Update status pembayaran: Belum Bayar → Lunas
4. View detail transaksi lengkap

### Manajemen Data (Admin)
1. **Kelola Pegawai**: Tambah/edit karyawan dengan role
2. **Kelola Harga**: Set harga per kg untuk setiap jenis layanan  
3. **Kelola Alat**: Monitor mesin dan jadwal maintenance
4. **Laporan**: Generate laporan keuangan dengan filter tanggal

## Workflow Sistem Voucher

### Cara Kerja:
1. **Mendapat Voucher**: Setiap transaksi = +1 voucher
2. **Menggunakan Voucher**: 7 voucher = gratis cuci 7kg
3. **Penerapan Diskon**: Voucher mengurangi berat yang harus dibayar
4. **Validasi**: Sistem cek voucher tersedia secara real-time

### Contoh Skenario:
```
Pelanggan A: Cuci 10kg paket Cuci Basah (Rp 2000/kg)
- Punya 7 voucher
- Gunakan voucher: gratis 7kg
- Bayar: 3kg × Rp 2000 = Rp 6000
- Dapat: +1 voucher baru
```

## Workflow Pembayaran

### Bayar Sekarang:
1. Customer pilih "Bayar Sekarang" 
2. Status otomatis "Lunas"
3. Transaksi selesai

### Bayar Setelah Selesai:
1. Customer pilih "Bayar Setelah Selesai"
2. Status "Belum Bayar" 
3. Pesanan diproses normal
4. Saat selesai: status tetap "Belum Bayar"
5. Staff update pembayaran saat customer bayar
6. Status berubah "Lunas"

## Struktur File Project

```
KaviLaundryApp/
├── src/
|   ├── images/
|   |   ├── Logo.jpg
│   ├── kavilaundry/
│   │   ├── Main.java
│   │   ├── DatabaseConnection.java
│   │   ├── User.java
│   │   ├── UserSession.java
│   │   ├── LoginForm.java
│   │   ├── AdminDashboard.java
│   │   ├── KasirDashboard.java
│   │   ├── InputTransaksiForm.java
│   │   ├── StatusPesananForm.java
│   │   ├── KelolaHargaForm.java
│   │   ├── KelolaAlatForm.java
│   │   ├── KelolaPegawaiForm.java
│   │   ├── RiwayatTransaksiForm.java
│   │   ├── LaporanKeuanganForm.java
│   │   ├── VoucherManagementForm.java
│   │   ├── StokAddonForm.java
│   │   └── DetailTransaksiDialog.java
│   └── lib/
│       └── mysql-connector-java-x.x.x.jar
└── README.md
```

## Troubleshooting

### Error Koneksi Database:
1. Pastikan MySQL/MariaDB running
2. Check username/password di DatabaseConnection.java
3. Pastikan database `kavi_laundry` sudah dibuat
4. Pastikan MySQL Connector/J di project libraries

### Error Voucher:
1. Check field `total_voucher` di tabel pelanggan
2. Pastikan logika voucher hanya untuk paket "Cuci"
3. Validasi input 7 voucher minimum

### Error Status Pembayaran:
1. Check field `tingkat_cuci` untuk flag "LUNAS"
2. Pastikan status update terpisah pesanan vs pembayaran

## Pengembangan Lebih Lanjut

### Fitur Enhancement:
- Notifikasi SMS/WhatsApp untuk status pesanan
- Barcode scanner untuk tracking
- Dashboard analytics dengan charts
- Mobile app untuk customer
- Multi-branch support
- Integration payment gateway

### Optimisasi Database:
- Indexing untuk query yang sering digunakan
- Connection pooling
- Automated backup system

## Lisensi

Aplikasi ini dikembangkan untuk keperluan pembelajaran dan bisnis. Dapat digunakan secara bebas dengan atribusi yang sesuai.

## Dukungan Teknis

Untuk bantuan teknis atau pertanyaan pengembangan, silakan hubungi pengembang melalui dokumentasi project atau issue tracking system.

---

**Kavi Laundry Management System** - Solusi Digital untuk Bisnis Laundry Modern
