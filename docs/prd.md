# PRD — Aplikasi Manajemen Keuangan Pribadi (Android, Penyimpanan Lokal)

| | |
|---|---|
| **Nama produk** | Duitku |
| **Platform** | Android native — Kotlin + Jetpack Compose |
| **Penyimpanan** | 100% lokal di perangkat (Room / SQLite). Tanpa server, tanpa akun, tanpa internet |
| **Pengguna** | Pemakaian pribadi (single user, satu perangkat) |
| **Versi dokumen** | 1.1 — 29 Agustus 2026 (v1.1 menambahkan F-6 ekspor Excel) |
| **Status** | Draft untuk dieksekusi |

---

## 1. Ringkasan

Aplikasi pencatat keuangan pribadi untuk dipakai sendiri sehari-hari. Fungsinya sederhana dan disengaja sempit: mencatat uang masuk dan uang keluar, mengelompokkannya ke dalam kategori, lalu melihat sisa saldo dan ringkasan bulan berjalan.

Seluruh data disimpan di database lokal perangkat. Tidak ada login, tidak ada sinkronisasi cloud, tidak ada permission internet sama sekali. Konsekuensinya harus diterima sejak awal: **data hidup dan mati bersama perangkat** — kalau HP hilang atau app di-uninstall, datanya hilang. Ekspor laporan bulanan ke Excel tersedia sejak rilis pertama, tapi perlu ditegaskan: itu laporan untuk dibaca, bukan cadangan yang bisa dipulihkan. Backup-restore sesungguhnya sengaja ditunda ke rilis berikutnya agar MVP tetap cepat jadi, dan ini tetap risiko nomor satu yang harus disadari (lihat §10).

## 2. Masalah yang diselesaikan

- Aplikasi keuangan yang ada di Play Store umumnya meminta pendaftaran akun, koneksi ke rekening bank, atau menampilkan iklan — berlebihan untuk kebutuhan mencatat pengeluaran harian.
- Mencatat di spreadsheet terasa lambat di HP; niat mencatat sering hilang sebelum aplikasi terbuka.
- Kebutuhan sebenarnya sederhana: **tahu uang habis ke mana bulan ini**, dan bisa mencatat sebuah transaksi dalam waktu di bawah 10 detik.

## 3. Tujuan & metrik keberhasilan

**Tujuan produk**

1. Mencatat satu transaksi selesai dalam ≤ 10 detik dari membuka app.
2. Aplikasi bisa menjawab satu pertanyaan dengan sekali lihat: "berapa yang sudah saya keluarkan bulan ini, dan untuk apa?"
3. Nol ketergantungan eksternal — jalan penuh dalam mode pesawat.

**Metrik (dievaluasi sendiri setelah 1 bulan pemakaian)**

| Metrik | Target |
|---|---|
| Hari tercatat dalam sebulan | ≥ 25 dari 30 hari |
| Waktu input satu transaksi | ≤ 10 detik |
| Cold start sampai layar utama tampil | ≤ 800 ms di perangkat mid-range |
| Ukuran APK | ≤ 15 MB (naik dari 12 MB karena penulis file Excel; lihat F-6) |
| Crash | 0 crash selama pemakaian normal |

## 4. Pengguna & konteks pemakaian

Satu orang, satu perangkat Android, mata uang Rupiah. Skenario dominan: berdiri di kasir atau baru selesai bayar sesuatu, membuka app dengan satu tangan, mengetik nominal, memilih kategori, simpan. Skenario kedua: duduk di akhir minggu, membuka ringkasan, melihat kategori mana yang membengkak.

Implikasi desain: **tombol tambah transaksi harus jadi elemen paling menonjol di layar utama**, dan alur input tidak boleh lebih dari satu layar.

## 5. Scope

### 5.1 Masuk MVP (Rilis 1)

1. **Pencatatan transaksi** — pemasukan dan pengeluaran, dengan nominal, kategori, tanggal, dan catatan opsional.
2. **Manajemen kategori** — kategori bawaan yang bisa langsung dipakai, plus tambah/ubah/hapus kategori sendiri.
3. **Daftar transaksi** — riwayat urut tanggal terbaru, dikelompokkan per hari, bisa difilter per bulan.
4. **Ringkasan bulan berjalan** — total pemasukan, total pengeluaran, selisih, dan rincian pengeluaran per kategori.
5. **Edit & hapus transaksi.**
6. **Ekspor laporan bulanan ke Excel (.xlsx)** — satu file per bulan, berisi rekap dan rincian transaksi.

### 5.2 Non-goals (eksplisit TIDAK dikerjakan di Rilis 1)

- Multi-dompet / multi-akun (cash, bank, e-wallet terpisah) — semua transaksi masuk satu kantong tunggal.
- Anggaran (budget) per kategori dan notifikasi limit.
- Grafik (donat, garis, batang berwarna) dan halaman laporan terpisah. Ringkasan MVP berupa angka dan daftar; satu bar proporsi polos di baris kategori diperbolehkan, selebihnya tidak.
- Backup dan restore database secara utuh (ekspor Excel di F-6 adalah laporan untuk dibaca manusia, **bukan** backup yang bisa diimpor kembali — bedanya penting, lihat catatan di §10).
- Ekspor PDF dan ekspor rentang tanggal bebas (MVP hanya per bulan penuh).
- Kunci PIN / biometrik.
- Transaksi berulang, pengingat, scan struk, multi-mata uang, utang-piutang, lampiran foto.
- Widget home screen, Wear OS, tablet layout.
- iOS.

Setiap item di atas bukan "tidak akan pernah" — beberapa dijadwalkan di §11. Yang penting: tidak satu pun boleh masuk diam-diam ke Rilis 1.

## 6. Spesifikasi fungsional

### F-1 Tambah transaksi

**User story:** Sebagai pengguna, saya ingin mencatat pengeluaran segera setelah membayar, agar tidak lupa.

Perilaku:

- Diakses lewat FAB (floating action button) di layar utama.
- Form satu layar berisi: pemilih tipe (Pengeluaran / Pemasukan), nominal, kategori, tanggal, catatan.
- Layar terbuka dengan **keypad angka aktif dan fokus di field nominal** — pengguna bisa langsung mengetik tanpa tap tambahan.
- Tipe default: **Pengeluaran** (kasus terbanyak).
- Tanggal default: **hari ini**. Bisa diubah lewat date picker; tanggal masa depan diizinkan, tanggal sebelum tahun 2000 ditolak.
- Nominal diformat otomatis saat mengetik menjadi format Rupiah (`15000` → `Rp 15.000`). Hanya bilangan bulat, tanpa desimal (sen tidak relevan untuk Rupiah).
- Kategori dipilih dari daftar horizontal chip yang bisa di-scroll, difilter sesuai tipe transaksi yang sedang aktif.
- Catatan bebas, maksimal 140 karakter, opsional.
- Tombol Simpan aktif hanya jika nominal > 0 dan kategori terpilih.

**Kriteria penerimaan**

- [ ] Menyimpan transaksi valid akan menutup form, kembali ke layar utama, dan transaksi baru muncul di paling atas daftar tanpa perlu refresh.
- [ ] Saldo dan ringkasan di layar utama ikut berubah seketika.
- [ ] Menekan tombol back saat form terisi memunculkan konfirmasi "Buang perubahan?".
- [ ] Nominal 0 atau kosong tidak bisa disimpan; tombol Simpan tetap nonaktif.
- [ ] Rotasi layar atau app masuk background tidak menghilangkan isian form.

### F-2 Kategori

**User story:** Sebagai pengguna, saya ingin mengelompokkan transaksi agar tahu uang terbesar habis ke mana.

Perilaku:

- Setiap kategori punya nama, ikon, warna, dan tipe (pengeluaran atau pemasukan).
- Kategori bawaan terisi otomatis saat app pertama kali dijalankan:
  - *Pengeluaran:* Makan & Minum, Transportasi, Belanja, Tagihan, Kesehatan, Hiburan, Pendidikan, Lainnya.
  - *Pemasukan:* Gaji, Bonus, Hadiah, Lainnya.
- Pengguna bisa menambah kategori baru, mengubah nama/ikon/warna, dan mengarsipkan kategori.
- **Kategori tidak dihapus permanen jika sudah dipakai** — statusnya diubah jadi arsip: hilang dari pilihan saat input, tapi transaksi lama tetap menampilkan namanya. Kategori yang belum pernah dipakai boleh dihapus sungguhan.
- Kategori "Lainnya" tidak bisa dihapus maupun diarsipkan; dia jadi tujuan fallback.

**Kriteria penerimaan**

- [ ] Instalasi baru langsung punya 12 kategori bawaan tanpa setup manual.
- [ ] Mengarsipkan kategori tidak mengubah nilai atau kategori transaksi lama.
- [ ] Nama kategori duplikat dalam tipe yang sama ditolak dengan pesan jelas.

### F-3 Daftar transaksi

Perilaku:

- Daftar utama menampilkan transaksi bulan yang sedang dipilih, urut dari tanggal terbaru.
- Dikelompokkan dengan header per hari (`Hari ini`, `Kemarin`, lalu `Sen, 25 Agu 2026`), disertai subtotal pengeluaran hari itu.
- Tiap baris menampilkan ikon + nama kategori, catatan (jika ada), dan nominal — pengeluaran berwarna merah dengan awalan `−`, pemasukan hijau dengan awalan `+`.
- Navigasi bulan lewat panah kiri/kanan di header.
- Empty state ramah untuk bulan tanpa transaksi, dengan ajakan mencatat.
- Daftar menggunakan paging; performa harus tetap mulus di 5.000+ transaksi.

**Kriteria penerimaan**

- [ ] Scroll tetap lancar (tidak ada frame drop terlihat) dengan 5.000 transaksi dummy.
- [ ] Berpindah bulan memuat data dalam ≤ 200 ms.

### F-4 Edit & hapus transaksi

Perilaku:

- Tap sebuah baris membuka form yang sama dengan F-1, sudah terisi.
- Hapus tersedia lewat swipe atau tombol di dalam form, diikuti **Snackbar dengan aksi Urungkan selama 5 detik** (bukan dialog konfirmasi — lebih cepat dan tetap aman).
- Penghapusan bersifat permanen setelah jendela urungkan lewat.

**Kriteria penerimaan**

- [ ] Menekan Urungkan mengembalikan transaksi persis seperti semula, termasuk id-nya.
- [ ] Menutup app sebelum 5 detik habis tetap menyelesaikan penghapusan.

### F-5 Ringkasan bulan berjalan

Perilaku:

- Kartu di bagian atas layar utama: total pemasukan, total pengeluaran, dan selisih untuk bulan yang dipilih.
- Di bawahnya, rincian pengeluaran per kategori, urut dari nominal terbesar, masing-masing menampilkan nominal, persentase terhadap total pengeluaran, dan bar proporsi sederhana.
- Tap baris kategori memfilter daftar transaksi ke kategori tersebut.

**Kriteria penerimaan**

- [ ] Angka ringkasan selalu sama dengan penjumlahan manual transaksi di bulan tersebut.
- [ ] Persentase dibulatkan ke bilangan bulat dan totalnya tetap ditampilkan konsisten (100% ± pembulatan tidak boleh terlihat aneh, misalnya jangan sampai muncul 101%).

### F-6 Ekspor laporan bulanan ke Excel

**User story:** Sebagai pengguna, saya ingin mengekspor laporan satu bulan ke file Excel, agar bisa saya buka di laptop, arsipkan, atau olah lebih lanjut di spreadsheet.

Perilaku:

- Diakses dari menu di header bulan pada layar utama (ikon ekspor) — bulan yang diekspor adalah bulan yang sedang ditampilkan.
- Setelah ditekan, muncul pemilih lokasi penyimpanan sistem (Storage Access Framework, `ACTION_CREATE_DOCUMENT`). Tidak ada permission storage yang diminta.
- Nama file default: `Laporan-Keuangan-2026-08.xlsx` (`Laporan-Keuangan-YYYY-MM.xlsx`).
- Setelah file tersimpan, muncul Snackbar dengan aksi **Buka** dan **Bagikan** (share sheet lewat `FileProvider`).
- Proses berjalan di background (coroutine + `WorkManager` bila > 5.000 baris), dengan indikator progres; membatalkan proses menghapus file setengah jadi.
- Bulan tanpa transaksi tetap bisa diekspor dan menghasilkan file berisi header saja, dengan pesan konfirmasi bahwa datanya kosong.

**Struktur file Excel**

File berisi tiga sheet:

*Sheet 1 — `Ringkasan`*

| Baris | Isi |
|---|---|
| Judul | `Laporan Keuangan — Agustus 2026` |
| Metadata | Tanggal ekspor, jumlah transaksi, rentang tanggal |
| Blok total | Total Pemasukan, Total Pengeluaran, Selisih |
| Tabel per kategori | Kategori · Tipe · Jumlah Transaksi · Total · % terhadap total pengeluaran — urut dari nominal terbesar, diakhiri baris **TOTAL** |

*Sheet 2 — `Transaksi`*

Satu baris per transaksi, kolom: `Tanggal` · `Hari` · `Tipe` · `Kategori` · `Catatan` · `Pemasukan` · `Pengeluaran` · `Saldo Berjalan`. Urut dari tanggal terlama ke terbaru (kebalikan dari tampilan di app — untuk spreadsheet, urutan kronologis lebih berguna karena saldo berjalan jadi masuk akal dibaca dari atas).

*Sheet 3 — `Harian`*

Satu baris per tanggal dalam bulan tersebut (termasuk tanggal tanpa transaksi, diisi 0), kolom: `Tanggal` · `Pemasukan` · `Pengeluaran` · `Selisih` · `Saldo Kumulatif`. Sheet ini disiapkan supaya pengguna bisa langsung membuat grafik sendiri di Excel tanpa mengolah ulang data.

**Aturan format**

- Kolom nominal memakai *number format* mata uang Rupiah (`"Rp"#,##0;[Red]-"Rp"#,##0`) — disimpan sebagai **angka**, bukan teks, supaya bisa langsung dijumlahkan di Excel.
- Kolom tanggal memakai tipe tanggal Excel dengan format `dd/mm/yyyy`, bukan string.
- Baris header dicetak tebal, dibekukan (*freeze pane*), dan diberi *auto filter*.
- Lebar kolom disesuaikan isi; kolom catatan dibatasi dan memakai *wrap text*.
- Baris TOTAL memakai formula Excel asli (`=SUM(...)`), bukan nilai yang sudah dihitung app — agar tetap benar jika pengguna menyunting atau memfilter baris.
- Angka negatif tidak dipakai untuk pengeluaran di sheet `Transaksi`; pemasukan dan pengeluaran dipisah ke dua kolom sendiri.

**Keputusan teknis: cara menulis file .xlsx**

Apache POI **tidak dipakai** — ukurannya menambah belasan MB ke APK dan method count-nya berat untuk app sekecil ini. Dua pilihan yang layak, urut preferensi:

1. **`fastexcel` (dhatim)** — writer-only, jauh lebih ringan dari POI, mendukung number format, freeze pane, dan formula. Perlu core library desugaring. **Ini pilihan default.**
2. **Penulis SpreadsheetML minimal buatan sendiri** — file `.xlsx` pada dasarnya adalah arsip ZIP berisi beberapa file XML. Karena struktur laporan kita tetap dan sudah diketahui, menulisnya langsung memakai `ZipOutputStream` sepenuhnya mungkin dan menambah nol dependensi. Ambil jalur ini jika `fastexcel` ternyata mendorong APK melewati 15 MB atau bermasalah dengan desugaring.

Keputusan final diambil setelah mengukur ukuran APK dengan `fastexcel` terpasang, bukan berdasarkan asumsi.

**Kriteria penerimaan**

- [ ] File hasil ekspor terbuka tanpa peringatan *"file rusak / perlu diperbaiki"* di Microsoft Excel, Google Sheets, LibreOffice Calc, dan Google Sheets versi Android.
- [ ] Nominal terbaca sebagai angka: menyorot satu kolom di Excel langsung menampilkan SUM di status bar.
- [ ] Total di sheet `Ringkasan` sama persis dengan total di layar ringkasan app untuk bulan yang sama.
- [ ] Saldo berjalan di baris terakhir sheet `Transaksi` sama dengan selisih di sheet `Ringkasan`.
- [ ] Ekspor 5.000 transaksi selesai dalam ≤ 5 detik dan tidak menyebabkan ANR maupun `OutOfMemoryError`.
- [ ] Catatan yang mengandung koma, tanda kutip, emoji, dan baris baru tetap utuh di dalam sel.
- [ ] Membatalkan pemilih lokasi penyimpanan tidak meninggalkan file kosong.
- [ ] Ekspor tetap berfungsi dalam mode pesawat (tidak ada jalur kode yang menyentuh jaringan).


## 7. Arsitektur & keputusan teknis

**Stack**

| Lapisan | Pilihan |
|---|---|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arsitektur | MVVM — Compose UI → ViewModel → Repository → DAO |
| Database | Room (SQLite) |
| Async | Coroutines + Flow |
| DI | Hilt (atau manual DI kalau ingin APK sekecil mungkin) |
| Preferensi | DataStore |
| Penulisan Excel | `fastexcel` (dhatim), writer-only — dengan penulis SpreadsheetML manual sebagai cadangan (lihat F-6) |
| Tugas latar | WorkManager (hanya untuk ekspor bervolume besar) |
| minSdk / targetSdk | 26 / 35 |

**Prinsip yang mengikat**

- **Tidak ada permission `INTERNET` di manifest.** Ini bukan sekadar keputusan produk, tapi jaminan teknis yang bisa diverifikasi siapa pun yang membaca manifest.
- Tanpa analytics, tanpa crash reporter pihak ketiga, tanpa iklan, tanpa SDK eksternal yang butuh jaringan.
- Tanpa permission penyimpanan (`READ/WRITE_EXTERNAL_STORAGE`). Ekspor file memakai Storage Access Framework, jadi app hanya bisa menulis ke lokasi yang dipilih pengguna sendiri saat itu juga.
- Sumber kebenaran tunggal adalah database Room; UI selalu mengamati Flow dari DAO, tidak pernah menyimpan salinan state yang bisa basi.
- Nominal disimpan sebagai `Long` dalam satuan rupiah penuh — tidak pernah `Double`, untuk menghindari galat pembulatan floating point.
- Tanggal disimpan sebagai epoch day (`Long`) dengan zona waktu perangkat; konversi tampilan memakai `java.time`.

### Model data

```
Transaction
  id           Long      PK, autogenerate
  amount       Long      selalu positif; arah ditentukan oleh type
  type         Enum      EXPENSE | INCOME
  categoryId   Long      FK → Category.id (ON DELETE RESTRICT)
  date         Long      epoch day
  note         String?   maks. 140 karakter
  createdAt    Long      epoch millis
  updatedAt    Long      epoch millis

Category
  id           Long      PK, autogenerate
  name         String    unik per (name, type)
  type         Enum      EXPENSE | INCOME
  iconKey      String    referensi ke ikon bawaan app
  colorHex     String
  isArchived   Boolean   default false
  isSystem     Boolean   true untuk "Lainnya"
  sortOrder    Int
```

Index pada `Transaction.date` dan `Transaction.categoryId` — dua kolom ini yang dipakai hampir semua query.

Migrasi Room ditulis eksplisit sejak versi 1; `fallbackToDestructiveMigration` dilarang, karena akan menghapus data pengguna yang tidak punya backup.

### Struktur layar

```
MainScreen
├── Header bulan (◀ Agustus 2026 ▶)
├── Kartu ringkasan (masuk / keluar / selisih)
├── Rincian per kategori (collapsible)
├── Daftar transaksi (grup per hari)
└── FAB "+"

TransactionFormScreen   — tambah & edit
CategoryListScreen      — kelola kategori
CategoryFormScreen      — tambah & edit kategori
ExportSheet             — bottom sheet konfirmasi ekspor + progres (bukan layar penuh)
SettingsScreen          — tema, format tanggal, hari awal bulan, info versi
```

## 8. Kebutuhan non-fungsional

- **Performa:** cold start ≤ 800 ms; semua query daftar berjalan di luar main thread.
- **Offline:** seluruh fungsi bekerja dalam mode pesawat, karena memang tidak pernah menyentuh jaringan.
- **Aksesibilitas:** target sentuh minimal 48 dp, content description pada semua ikon, kontras teks memenuhi WCAG AA, dan tata letak tidak rusak sampai ukuran font sistem 200%.
- **Tema:** mendukung mode terang dan gelap, mengikuti setelan sistem; Dynamic Color (Material You) di Android 12+.
- **Bahasa:** Bahasa Indonesia; format angka `Rp 1.500.000`, format tanggal `25 Agu 2026`.
- **Data:** database berada di penyimpanan privat app, `allowBackup=false` agar isi database tidak ikut terkirim ke Google Backup tanpa disadari.

## 9. Alur pengalaman pertama (first run)

1. Buka app → langsung layar utama, tanpa onboarding, tanpa permintaan izin.
2. Kategori bawaan sudah terisi.
3. Empty state menampilkan satu kalimat dan panah ke tombol tambah.
4. Satu bilah informasi sekali tampil: "Data hanya tersimpan di HP ini." — dengan tombol Mengerti.

## 10. Risiko & mitigasi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| **HP hilang, rusak, atau app di-uninstall → seluruh data hilang** | Tinggi | Terima secara sadar di Rilis 1, tapi tampilkan peringatan di first run dan di Pengaturan. **Backup/restore dijadikan item pertama Rilis 2 dan sebaiknya tidak ditunda lebih dari satu bulan pemakaian.** |
| Kebiasaan mencatat tidak terbentuk | Tinggi (produk jadi sia-sia) | Alur input ≤ 10 detik jadi kriteria rilis, bukan sekadar target. Evaluasi setelah 2 minggu. |
| Scope creep — tergoda menambah grafik/budget sebelum MVP jadi | Sedang | §5.2 bersifat mengikat; semua ide baru masuk backlog Rilis 2 tanpa perdebatan. |
| Migrasi database salah saat menambah fitur baru | Sedang | Migrasi eksplisit + tes migrasi otomatis sejak awal. |
| Salah pembulatan pada persentase kategori | Rendah | Uji unit khusus pada perhitungan ringkasan. |
| **Ekspor Excel disangka backup** — file .xlsx tidak bisa diimpor kembali, jadi rasa aman yang palsu | Sedang | Beri label jelas di UI ("Laporan, bukan cadangan data") dan tetap prioritaskan backup/restore asli di Rilis 2. |
| Library penulis Excel membengkakkan APK atau bentrok dengan desugaring | Sedang | Ukur ukuran APK sebelum berkomitmen; penulis SpreadsheetML manual sudah disiapkan sebagai jalur cadangan. |
| File .xlsx hasil buatan sendiri ditolak Excel karena XML tidak valid | Sedang | Uji buka di empat aplikasi spreadsheet sebagai kriteria rilis, bukan pengecekan manual sekali jalan. |

## 11. Roadmap sesudah MVP

**Rilis 2 — keamanan data (prioritas tertinggi)**

- Ekspor dan impor backup (file JSON atau `.db`) lewat Storage Access Framework.
- Ekspor CSV (rentang tanggal bebas) dan ekspor Excel multi-bulan dalam satu file.
- Kunci PIN dan biometrik.

**Rilis 3 — pemahaman**

- Grafik: donat pengeluaran per kategori, garis tren bulanan.
- Anggaran per kategori dengan indikator sisa.
- Pencarian dan filter lanjutan.

**Rilis 4 — kenyamanan**

- Multi-dompet (cash / bank / e-wallet) dan transfer antar dompet.
- Transaksi berulang dan pengingat harian.
- Widget home screen untuk input cepat.

## 12. Definition of Done untuk Rilis 1

- [ ] Keenam fitur di §5.1 selesai dan seluruh kriteria penerimaannya terpenuhi.
- [ ] Manifest terbukti tidak memuat permission `INTERNET`.
- [ ] Tes unit untuk perhitungan ringkasan dan format mata uang lulus.
- [ ] Manifest terbukti tidak memuat permission penyimpanan.
- [ ] File ekspor bulan berisi ≥ 100 transaksi diverifikasi terbuka bersih di Excel, Google Sheets, dan LibreOffice Calc.
- [ ] Tes migrasi Room versi 1 → 2 berjalan (disiapkan lebih dulu meski belum ada versi 2).
- [ ] Diuji pada Android 8 dan Android 14+, di mode terang dan gelap.
- [ ] Tidak ada crash setelah 7 hari pemakaian nyata.
- [ ] APK rilis ditandatangani dan **keystore beserta passwordnya disimpan di tempat yang tidak akan hilang** — tanpa itu, app tidak bisa di-update di kemudian hari.
