/*
 Navicat Premium Dump SQL

 Source Server         : kafka-cool
 Source Server Type    : MySQL
 Source Server Version : 100432 (10.4.32-MariaDB)
 Source Host           : localhost:3306
 Source Schema         : kavi_laundry

 Target Server Type    : MySQL
 Target Server Version : 100432 (10.4.32-MariaDB)
 File Encoding         : 65001

 Date: 09/10/2025 11:38:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for addon
-- ----------------------------
DROP TABLE IF EXISTS `addon`;
CREATE TABLE `addon`  (
  `id_addon` int NOT NULL AUTO_INCREMENT,
  `nama_addon` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `harga` decimal(10, 2) NOT NULL,
  `stok` int NULL DEFAULT 0,
  PRIMARY KEY (`id_addon`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of addon
-- ----------------------------
INSERT INTO `addon` VALUES (1, 'Detergen Sachet', 1000.00, 86);
INSERT INTO `addon` VALUES (2, 'Pewangi', 1000.00, 94);

-- ----------------------------
-- Table structure for alat
-- ----------------------------
DROP TABLE IF EXISTS `alat`;
CREATE TABLE `alat`  (
  `id_alat` int NOT NULL AUTO_INCREMENT,
  `nama_alat` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `jenis_alat` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT 'tersedia',
  `maintenance_date` date NULL DEFAULT NULL,
  PRIMARY KEY (`id_alat`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of alat
-- ----------------------------
INSERT INTO `alat` VALUES (1, 'Mesin Cuci 1', 'cuci', 'maintenance', '2025-12-31');
INSERT INTO `alat` VALUES (2, 'Mesin Cuci 2', 'cuci', 'digunakan', '2025-12-15');
INSERT INTO `alat` VALUES (3, 'Setrika 1', 'setrika', 'tersedia', '2025-10-14');
INSERT INTO `alat` VALUES (4, 'Mesin Cuci 3', 'cuci', 'tersedia', '2025-12-30');
INSERT INTO `alat` VALUES (5, 'Mesin Cuci 4', 'cuci', 'tersedia', '2025-12-30');
INSERT INTO `alat` VALUES (6, 'Mesin Cuci 5', 'cuci', 'tersedia', '2025-12-30');
INSERT INTO `alat` VALUES (7, 'Mesin Cuci 6', 'cuci', 'tersedia', '2025-12-30');
INSERT INTO `alat` VALUES (10, 'Setrika 2', 'setrika', 'tersedia', NULL);

-- ----------------------------
-- Table structure for laporan_keuangan
-- ----------------------------
DROP TABLE IF EXISTS `laporan_keuangan`;
CREATE TABLE `laporan_keuangan`  (
  `id_laporan` int NOT NULL AUTO_INCREMENT,
  `tanggal_laporan` date NOT NULL,
  `total_pendapatan` decimal(12, 2) NULL DEFAULT NULL,
  `total_transaksi` int NULL DEFAULT NULL,
  `generated_by` int NULL DEFAULT NULL,
  PRIMARY KEY (`id_laporan`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of laporan_keuangan
-- ----------------------------

-- ----------------------------
-- Table structure for paket
-- ----------------------------
DROP TABLE IF EXISTS `paket`;
CREATE TABLE `paket`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `nama` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `kapasitas` varchar(10) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `harga` int NULL DEFAULT NULL,
  `keterangan` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of paket
-- ----------------------------
INSERT INTO `paket` VALUES (1, 'Cuci Basah', 'per kg', 2000, 'Baju basah pelanggan jemur sendiri');
INSERT INTO `paket` VALUES (3, 'Cuci Kering', 'per kg', 3000, 'Baju kering tidak di setrika');
INSERT INTO `paket` VALUES (7, 'Setrika', 'per kg', 4000, 'Hanya setrika');
INSERT INTO `paket` VALUES (9, 'Cuci Setrika', 'per kg', 11000, 'Cuci kering dengan setrika');
INSERT INTO `paket` VALUES (13, 'cuci uap', 'per kg', 7000, 'cuci uap');

-- ----------------------------
-- Table structure for pelanggan
-- ----------------------------
DROP TABLE IF EXISTS `pelanggan`;
CREATE TABLE `pelanggan`  (
  `id_pelanggan` int NOT NULL AUTO_INCREMENT,
  `nama` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `total_voucher` int NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id_pelanggan`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pelanggan
-- ----------------------------
INSERT INTO `pelanggan` VALUES (1, 'Budi Santoso', 2, '2025-09-09 10:22:53');
INSERT INTO `pelanggan` VALUES (3, 'Andi Wijaya', 5, '2025-09-09 10:22:53');
INSERT INTO `pelanggan` VALUES (4, 'Mamak', 1, '2025-09-11 06:39:28');
INSERT INTO `pelanggan` VALUES (9, 'viana', 2, '2025-09-11 08:53:44');
INSERT INTO `pelanggan` VALUES (10, 'hanif', 1, '2025-09-11 08:56:58');
INSERT INTO `pelanggan` VALUES (11, 'adit', 1, '2025-09-11 09:03:22');
INSERT INTO `pelanggan` VALUES (12, 'sdg', 1, '2025-09-11 09:06:44');
INSERT INTO `pelanggan` VALUES (13, 'dgdsfgds', 1, '2025-09-11 09:08:57');
INSERT INTO `pelanggan` VALUES (14, 'asfasdf', 1, '2025-09-11 09:10:53');
INSERT INTO `pelanggan` VALUES (15, 'kafka', 6, '2025-09-13 20:25:11');
INSERT INTO `pelanggan` VALUES (19, 'testcok', 5, '2025-09-16 09:15:33');
INSERT INTO `pelanggan` VALUES (20, 'romi', 3, '2025-09-18 13:28:02');
INSERT INTO `pelanggan` VALUES (21, 'amir', 1, '2025-09-20 18:02:49');
INSERT INTO `pelanggan` VALUES (22, 'daffa', 3, '2025-09-25 10:46:40');
INSERT INTO `pelanggan` VALUES (23, 'bayu', 4, '2025-09-25 10:47:48');
INSERT INTO `pelanggan` VALUES (24, 'made', 1, '2025-09-25 10:48:15');
INSERT INTO `pelanggan` VALUES (25, 'ali', 1, '2025-09-25 10:48:39');
INSERT INTO `pelanggan` VALUES (26, 'a', 1, '2025-09-30 07:59:43');
INSERT INTO `pelanggan` VALUES (27, 'b', 1, '2025-09-30 08:00:12');
INSERT INTO `pelanggan` VALUES (28, 'c', 1, '2025-09-30 08:00:35');
INSERT INTO `pelanggan` VALUES (29, 'd', 1, '2025-09-30 08:00:54');
INSERT INTO `pelanggan` VALUES (30, 'e', 1, '2025-09-30 08:01:31');
INSERT INTO `pelanggan` VALUES (31, 'cok', 2, '2025-09-30 08:39:37');
INSERT INTO `pelanggan` VALUES (32, 'ajay', 1, '2025-09-30 09:45:10');
INSERT INTO `pelanggan` VALUES (33, 'huza', 2, '2025-10-07 07:22:07');
INSERT INTO `pelanggan` VALUES (34, 'alan', 1, '2025-10-07 07:22:49');
INSERT INTO `pelanggan` VALUES (35, 'fabian', 1, '2025-10-07 07:23:55');
INSERT INTO `pelanggan` VALUES (36, 'steven', 1, '2025-10-07 07:24:20');
INSERT INTO `pelanggan` VALUES (37, 'galang', 1, '2025-10-08 07:30:13');
INSERT INTO `pelanggan` VALUES (38, 'rama', 1, '2025-10-08 07:30:30');
INSERT INTO `pelanggan` VALUES (39, 'andre', 1, '2025-10-08 07:30:54');
INSERT INTO `pelanggan` VALUES (40, 'data', 1, '2025-10-07 07:49:16');
INSERT INTO `pelanggan` VALUES (41, 'coba', 1, '2025-10-07 08:03:22');
INSERT INTO `pelanggan` VALUES (42, 'coba2', 1, '2025-10-07 08:03:42');
INSERT INTO `pelanggan` VALUES (43, 'bayuu', 1, '2025-10-09 11:07:05');
INSERT INTO `pelanggan` VALUES (44, 'dani', 1, '2025-10-09 11:07:21');
INSERT INTO `pelanggan` VALUES (45, 'daniel', 1, '2025-10-10 11:09:03');
INSERT INTO `pelanggan` VALUES (46, 'toriq', 1, '2025-10-10 11:09:21');
INSERT INTO `pelanggan` VALUES (47, 'test', 1, '2025-10-09 11:26:17');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id_role` int NOT NULL AUTO_INCREMENT,
  `nama_role` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  PRIMARY KEY (`id_role`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'kasir');
INSERT INTO `role` VALUES (2, 'admin');

-- ----------------------------
-- Table structure for transaksi
-- ----------------------------
DROP TABLE IF EXISTS `transaksi`;
CREATE TABLE `transaksi`  (
  `id_transaksi` int NOT NULL AUTO_INCREMENT,
  `id_pelanggan` int NULL DEFAULT NULL,
  `id_jenis` int NOT NULL,
  `berat_kg` decimal(5, 2) NOT NULL,
  `pembayaran` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `total_biaya` decimal(10, 2) NOT NULL,
  `addon_ids` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  `voucher_didapat` int NULL DEFAULT 0,
  `status_pesanan` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT 'diterima',
  `id_user` int NULL DEFAULT NULL,
  `tanggal_transaksi` timestamp NOT NULL DEFAULT current_timestamp,
  `tanggal_ambil` date NULL DEFAULT NULL,
  PRIMARY KEY (`id_transaksi`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transaksi
-- ----------------------------
INSERT INTO `transaksi` VALUES (54, 33, 1, 10.00, 'Cash - Bayar Sekarang', 20000.00, NULL, 1, 'diambil', 8, '2025-10-09 11:06:51', '2025-10-12');
INSERT INTO `transaksi` VALUES (55, 43, 3, 9.00, 'QRIS - Bayar Sekarang', 27000.00, NULL, 1, 'diambil', 8, '2025-10-09 11:07:06', '2025-10-12');
INSERT INTO `transaksi` VALUES (56, 44, 9, 11.00, 'Cash - Bayar Setelah Selesai - LUNAS', 121000.00, NULL, 1, 'diambil', 8, '2025-10-11 11:14:30', '2025-10-12');
INSERT INTO `transaksi` VALUES (57, 20, 1, 13.00, 'Cash - Bayar Sekarang', 26000.00, NULL, 1, 'diterima', 8, '2025-10-10 11:08:49', '2025-10-13');
INSERT INTO `transaksi` VALUES (58, 45, 13, 12.00, 'QRIS - Bayar Sekarang', 84000.00, NULL, 1, 'diambil', 8, '2025-10-10 11:09:06', '2025-10-13');
INSERT INTO `transaksi` VALUES (59, 46, 9, 11.00, 'Cash - Bayar Setelah Selesai - LUNAS', 121000.00, NULL, 1, 'diambil', 8, '2025-10-09 11:24:34', '2025-10-13');
INSERT INTO `transaksi` VALUES (60, 47, 1, 11.00, 'Cash - Bayar Setelah Selesai - LUNAS', 22000.00, NULL, 1, 'diambil', 8, '2025-11-01 11:31:36', '2025-10-12');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id_user` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `password` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `role_id` int NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp,
  `alamat` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `no_telp` decimal(16, 0) NULL DEFAULT NULL,
  `nama` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id_user`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2, 'admin', 'admin123', 2, '2025-09-09 10:22:53', NULL, NULL, NULL);
INSERT INTO `user` VALUES (8, 'kafka', 'kafka123', 1, '2025-10-07 07:19:14', 'di rumah', 5358320953, 'kafka ahmad sanjaya');

SET FOREIGN_KEY_CHECKS = 1;
