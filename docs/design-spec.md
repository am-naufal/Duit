# Spesifikasi tampilan — per layar

Nilai di sini adalah nilai yang dipakai di mockup. Token yang disebut (`Spasi.layar`,
`Sudut.kartu`, dst.) ada di `app/src/main/java/com/duit/ui/theme/`.

Aturan umum untuk semua layar:

- Padding kiri-kanan `Spasi.layar` (20 dp).
- Jangan menggambar status bar atau navigation bar palsu. Sisakan ruangnya lewat
  `WindowInsets.systemBars` — sistem yang menggambarnya.
- Latar `MaterialTheme.colorScheme.background`, kartu `…colorScheme.surface`.
- Bayangan kartu: `0 1 2 rgba(0,0,0,.04)` + `0 10 28 rgba(0,0,0,.04)`. Di Compose
  cukup `Surface(tonalElevation = 0.dp, shadowElevation = 1.dp)` plus latar putih —
  jangan pakai elevation Material default, terlalu berat untuk desain ini.

---

## 1. Layar utama (`MainScreen`)

Susunan dari atas ke bawah, dalam satu `LazyColumn`:

**Header (bukan bagian scroll — `TopAppBar` custom, tinggi 48 dp)**
- Kiri: tombol ikon `ic_chevron_left` (40 dp) · label bulan `titleLarge`
  ("Agustus 2026", lebar minimum 112 dp, rata tengah) · `ic_chevron_right`.
- Kanan: `ic_ekspor` lalu `ic_more_vert`, masing-masing 40 dp, jarak 2 dp.
- Ikon `ic_more_vert` membuka menu berisi Kelola kategori + Pengaturan.

**Kartu ringkasan** — `Sudut.kartu` (24 dp), padding 20 dp
- Label "Sisa bulan ini" `labelLarge` warna `teksRedup`.
- Nominal `displaySmall` (34/700), warna `onSurface`.
- Pemisah 1 dp warna `outlineVariant`, margin atas 18 bawah 16.
- Baris dua kolom sama lebar: masing-masing lingkaran ikon 30 dp radius 11
  (`ic_masuk` di latar `pemasukanLembut`, `ic_keluar` di `pengeluaranLembut`),
  lalu label 12 sp + nominal 15 sp/600 berwarna `pemasukan` / `pengeluaran`.

**Blok "Pengeluaran per kategori"**
- Judul `labelLarge` warna `onSurfaceVariant`, di kanannya `ic_chevron_down`
  (blok bisa dilipat).
- Kartu `Sudut.baris` (22 dp) padding 16, jarak antar baris 15 dp.
- Tiap baris: lingkaran 26 dp radius 9 berlatar warna kategori + ikon putih ·
  nama (14 sp/500) · nominal (14 sp/600) · persentase (12 sp, `teksRedup`, lebar
  tetap 32 dp rata kanan) · di bawahnya bar 5 dp radius penuh, track `track`,
  isi warna kategori selebar persentasenya.
- Maksimal 3 baris, sisanya diringkas jadi satu baris teks
  "+N kategori lain · Rp X" (12,5 sp, `teksRedup`).
- **Persentase dibulatkan ke bilangan bulat, dan jumlah yang ditampilkan tidak
  boleh terlihat melebihi 100%.** Bulatkan dengan metode sisa terbesar.

**Daftar transaksi, dikelompokkan per hari**
- Header hari: kiri "Hari ini" / "Kemarin" / "Sen, 25 Agu 2026" (`labelLarge`,
  `onSurfaceVariant`), kanan subtotal **pengeluaran** hari itu (12 sp, `teksRedup`).
- Kartu `Sudut.baris` padding 5 dp, berisi baris-baris.
- Baris transaksi: lingkaran ikon `Ukuran.ikonBaris` (36 dp radius 13) berlatar
  warna kategori · nama kategori (14,5 sp/600) · catatan (12,5 sp, `teksRedup`) ·
  nominal 15 sp/600, `−Rp` warna `pengeluaran` atau `+Rp` warna `pemasukan`.
- Padding baris 11 dp vertikal, 12 dp horizontal, jarak ikon–teks 12 dp.

**FAB** — 60 dp, radius 22, `ic_plus` 26 dp, posisi kanan bawah margin 20/34.
Gradien pudar setinggi 120 dp di atas FAB supaya daftar tidak terpotong keras.

---

## 2. Tambah / ubah transaksi (`TransactionFormScreen`)

Satu layar, tidak boleh scroll.

- App bar: `ic_close` + judul "Transaksi baru" / "Ubah transaksi". Pada mode ubah,
  tambah `ic_hapus` warna `pengeluaran` di kanan.
- Segmented Pengeluaran / Pemasukan: track `outlineVariant` radius penuh padding 4,
  segmen aktif berlatar `surface` dengan `shadowElevation = 1.dp`, tinggi 40 dp.
  **Default: Pengeluaran.**
- Blok nominal, rata tengah: label "NOMINAL" (12 sp/600, `teksRedup`, tracking 2%)
  lalu nominal `displayMedium` (40/700) berwarna sesuai tipe, diikuti kursor
  (batang 2,5 × 34 dp) yang berkedip.
- Label "Kategori" lalu deretan chip horizontal yang bisa di-scroll: tinggi 38 dp,
  radius penuh, titik warna kategori 9 dp + nama. Terpilih = border + latar
  `primaryContainer`, teks `primary`, bobot 600.
- Baris dua field: tanggal (`ic_kalender`, isi "Hari ini" atau "28 Agu") dan
  catatan (`ic_catatan`). Tinggi 46 dp, radius 16, border 1 dp `outline`.
- **Keypad angka milik aplikasi**, bukan keyboard sistem: grid 3 kolom, tombol
  56 dp, angka 23 sp/500; baris terakhir `000` · `0` · `ic_hapus_mundur`.
- Tombol "Simpan" penuh lebar, tinggi 54 dp, radius 18, warna `primary`.
  **Nonaktif selama nominal 0 atau kategori belum dipilih.**

Perilaku wajib: nominal diformat otomatis saat mengetik; tombol back saat form
terisi memunculkan konfirmasi "Buang perubahan?"; isian bertahan saat rotasi
(simpan di `SavedStateHandle`).

---

## 3. Hapus + urungkan

- Geser baris ke kiri memunculkan panel merah selebar 96 dp berisi `ic_hapus`
  dan teks "Hapus".
- Setelah dilepas, baris hilang dan muncul kartu di bawah layar (bukan Snackbar
  Material bawaan): latar `#22262A`, radius 18, judul "Transaksi dihapus",
  baris kedua "kategori · nominal", tombol "URUNGKAN" berlatar aksen transparan.
- Di bawah kartu, bar tipis 3 dp menunjukkan sisa waktu **5 detik**.
- Menutup aplikasi sebelum 5 detik habis tetap menyelesaikan penghapusan.
- Urungkan mengembalikan transaksi persis seperti semula, termasuk id-nya.

---

## 4. Kelola kategori

- App bar: `ic_arrow_back` · "Kategori" · `ic_plus` warna `primary`.
- Segmented Pengeluaran / Pemasukan.
- Kartu berisi baris: lingkaran 34 dp radius 12 · nama (14,5 sp/600) ·
  "N transaksi" (12 sp, `teksRedup`) · `ic_geser` untuk mengurutkan.
- Kategori bawaan "Lainnya" memakai chip "Bawaan" dan tidak punya `ic_geser` —
  tidak bisa dihapus maupun diarsipkan.
- Bagian "Diarsipkan" di bawah: baris meredup, aksi teks "Pulihkan" warna `primary`.

## 5. Form kategori

- App bar: `ic_close` · "Kategori baru" · tombol "Simpan" pil warna `primary`.
- Pratinjau: kotak 76 dp radius 28 berwarna pilihan + ikon 34 dp, nama di
  bawahnya, lalu kata "Pratinjau" (12,5 sp, `teksRedup`).
- Field nama: tinggi 52 dp radius 16, border 1,5 dp `primary` saat fokus.
- Segmented tipe.
- Grid ikon 6 kolom, tile 52 dp radius 16; terpilih = border `primary` +
  latar `primaryContainer`.
- Grid warna 6 kolom, lingkaran; terpilih = cincin luar 3 dp warna latar + 2 dp
  warna itu sendiri, dan ikon centang di tengah.

---

## 6. Ekspor Excel (bottom sheet)

- Sheet `Sudut.sheet` (30 dp atas), scrim `#101215` 42%.
- Handle 38 × 4 dp.
- Judul "Ekspor laporan" 19 sp/700, subjudul "Agustus 2026 · N transaksi".
- Kartu nama file: kotak "XLS" 36 dp warna `#1E7B4E`, nama file
  `Laporan-Keuangan-YYYY-MM.xlsx`, baris kedua "3 sheet · kamu pilih lokasi simpannya".
- Tiga baris isi laporan dengan ikon centang bulat.
- **Bilah peringatan kuning** (`peringatanLatar`, radius 14): "Ini laporan untuk
  dibaca, bukan cadangan data — file ini tidak bisa dipulihkan kembali ke aplikasi."
  Jangan dihapus, ini mitigasi risiko di PRD §10.
- Progres: baris "Menyiapkan file…" + persentase warna `primary`, bar 6 dp.
- Tombol "Batal" outline tinggi 50 dp — membatalkan menghapus file setengah jadi.

## 7. Ekspor selesai

Kartu aksi di bawah layar (latar `#22262A`, radius 22): ikon centang bulat,
"Laporan Agustus tersimpan", baris lokasi, lalu dua tombol "BUKA" dan "BAGIKAN".

## 8. Pengaturan

Grup berlabel (`labelSmall`, huruf besar, `teksRedup`): **TAMPILAN** (tema,
format tanggal, hari awal bulan) · **DATA** (ekspor laporan bulan ini, kelola
kategori) · **TENTANG** (tanpa izin internet, versi).

Di bawah grup DATA ada bilah peringatan kuning "Belum ada cadangan data" —
teksnya menjelaskan risiko kehilangan data. Ini juga tidak boleh dihapus.

Baris pengaturan: ikon 32 dp radius 11 di latar `surfaceVariant` · judul 14,5 sp/500 ·
nilai di kanan (13,5 sp, `teksRedup`) · `ic_chevron_right`. Pemisah 1 dp mulai
dari 59 dp dari kiri.

## 9. Layar pertama dibuka

- Panah bulan kiri/kanan meredup (belum ada data bulan lain).
- Kartu putih dengan ikon `ic_gembok`: "Data hanya tersimpan di HP ini" +
  penjelasan + tombol "Mengerti". Muncul sekali saja.
- Kondisi kosong di tengah: ikon besar meredup, "Belum ada catatan bulan ini",
  "Catat pengeluaran pertamamu — cukup nominal dan kategori, di bawah 10 detik."
- Panah putus-putus menunjuk ke FAB.
