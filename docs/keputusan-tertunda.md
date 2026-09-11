# Keputusan tertunda & yang belum dikerjakan

Rekap semua hal yang perlu keputusanmu atau sengaja ditunda, dikumpulkan dari
pengerjaan ketujuh layar (Layar utama, Tambah transaksi, Ubah + hapus, Kelola
kategori, Form kategori, Pengaturan, Ekspor Excel) plus data layer.

Status per 11 September 2026 (diperbarui setelah B-2/B-3 selesai). Verifikasi
terakhir: `assembleDebug`, `testDebugUnitTest` (23 tes hijau), `lintDebug`
(0 temuan di kode `ui/` & `data/`) semua lulus. Release APK dengan R8: **3,24 MB**.

---

## A. Disetujui 11 September 2026

Semua enam poin di bawah sudah dikonfirmasi oleh pemilik proyek, persis seperti
yang diimplementasikan — tidak ada perubahan kode yang diperlukan. Dipertahankan
di sini sebagai catatan keputusan, bukan lagi item terbuka.

### A-1. Konfigurasi build release diubah — R8/minifikasi diaktifkan

**Konteks.** PRD F-6 mensyaratkan APK ≤ 15 MB. Skeleton awal `app/build.gradle.kts`
memakai `optimization { enable = false }` di blok `release`, sehingga:

| Konfigurasi | APK release |
|---|---|
| Tanpa R8 (skeleton awal) | **24,1 MB** — lewat budget |
| Dengan R8 + `shrinkResources` | **3,24 MB** |

Bagian terbesar dari 24 MB itu adalah bytecode Compose/Kotlin/AndroidX yang tak
di-tree-shake — **bukan** fastexcel (fastexcel + core library desugaring hanya
menambah < 2 MB setelah R8).

**Yang sudah saya lakukan:** mengganti blok `release` jadi
`isMinifyEnabled = true` + `isShrinkResources = true` + `proguard-rules.pro`
(berisi `-keep` untuk `org.dhatim.fastexcel.**`).

**Keputusan yang kamu perlu ambil:**
- Setuju mengaktifkan R8 untuk release? (rekomendasi: **ya** — rilis nyata
  memang selalu butuh ini, dan `fastexcel` jadi tidak masalah).
- Kalau **tidak**, rencana cadangan PRD F-6 adalah menulis SpreadsheetML manual
  dengan `ZipOutputStream` (nol dependensi). Itu menghemat ~200 KB–2 MB saja,
  jadi tidak menyelesaikan masalah 24 MB kalau R8 tetap mati.

**Wajib sebelum rilis:** smoke-test APK release di perangkat nyata — R8 baru
diaktifkan, jalur `fastexcel` belum pernah dijalankan setelah minifikasi.

### A-2. Di mana kategori diarsipkan / dihapus

`docs/design-spec.md` **tidak menggambar** aksi arsip/hapus kategori — baik di
§4 (Kelola kategori, hanya ada "Pulihkan" untuk yang sudah diarsip) maupun di §5
(Form kategori).

**Keputusan saya (perlu kamu setujui):** tombol teks destruktif di **Form
kategori**, mode ubah, non-sistem:
- kategori yang **sudah dipakai** transaksi → "Arsipkan kategori" (langsung, bisa
  dipulihkan lewat §4)
- kategori yang **belum pernah dipakai** → "Hapus kategori" + dialog konfirmasi
- kategori "Lainnya" (sistem) → tidak ada tombol, hanya keterangan

Alternatif yang tidak saya pilih: geser baris di §4 (seperti hapus transaksi §3).

### A-3. Ikon kondisi kosong (Layar utama §9)

Spec cuma bilang "ikon besar meredup", tak sebut ikon mana. Dipakai
`ic_catatan`. Ganti kalau ada preferensi.

### A-4. Ikon baris di Pengaturan

Tak ada ikon khusus untuk "tema" dan "kelola kategori"; sekarang pakai
`ic_info` dan `ic_catatan`, dan `ic_kalender` dipakai dua kali (format tanggal &
hari awal bulan). Perlu ikon baru di `res/drawable/ic_*.xml` kalau mau rapi.

### A-5. Penambahan pada spec Ekspor (§6)

Spec bottom sheet §6 hanya menggambar tombol "Batal". Saya menambahkan tombol
primer **"Pilih lokasi & simpan"** yang meluncurkan Storage Access Framework
(`ACTION_CREATE_DOCUMENT`) — tanpa itu tak ada cara memulai ekspor. Konfirmasi
kalau teks/penempatannya perlu diubah.

### A-6. "Ekspor selesai" (§7) dirender di dalam sheet

§7 menggambar kartu aksi melayang terpisah ("Laporan Agustus tersimpan" + BUKA +
BAGIKAN). Implementasi sekarang menampilkannya sebagai **state akhir di dalam
bottom sheet yang sama** (kartu gelap `#22262A`). Deviasi kecil; bilang kalau
mau kartu benar-benar terpisah dari sheet.

---

## B. Sengaja ditunda — pekerjaan yang belum ada

### B-1. Seret untuk mengurutkan kategori (Kelola kategori §4)

Handle `ic_geser` sudah digambar, tapi **gestur seret + simpan urutan belum
diimplementasi**. Plumbing sudah siap:
- `KategoriDao.perbaruiSemua(List<Kategori>)`
- `DuitRepository.urutkanKategori(List<Kategori>)` — menulis ulang `urutan`
  sesuai posisi

Yang kurang: gesture `detectDragGesturesAfterLongPress` + animasi perpindahan
item di `Column` daftar aktif, lalu panggil repo saat dilepas. Manual di Compose
cukup rumit; alternatif: tambah library reorderable lokal (tak butuh internet).

### B-2 & B-3. Selesai 11 September 2026 — "Format tanggal" & "Hari awal bulan" diterapkan

Kedua preferensi ini sekarang benar-benar berefek, bukan cuma tersimpan.

**Format tanggal** (`FormatTanggal`) sudah mengalir dari `Preferensi.aliran` ke
`UtamaViewModel` (header hari) dan `TambahViewModel` (field tanggal di Form
transaksi).

**Hari awal bulan** (`hariAwalBulan`, 1..28) sekarang benar-benar menggeser apa
yang dianggap "bulan berjalan", lewat tipe baru `data/Periode.kt`:
`Periode.dariLabel(label, hariAwalBulan)` menghasilkan rentang tanggal
sebenarnya (bisa lintas dua bulan kalender, mis. 25 Agu–24 Sep), dan
`Periode.labelUntuk(tanggal, hariAwalBulan)` menentukan bulan-label mana yang
memuat suatu tanggal. Disentuh:
- `DuitRepository.transaksiPeriode()` / `ringkasanPeriode()` / `transaksiPeriodeSekali()`
  (dulu `transaksiBulan()` dkk.) — sekarang menerima `Periode`, bukan `YearMonth` mentah.
- `UtamaViewModel` — label "bulan sekarang" dihitung ulang dari `hariAwalBulan`
  begitu pengguna belum menavigasi panah bulan; batas maju/mundur juga memakai
  `Periode.labelUntuk`, bukan `YearMonth.from`.
- `RakitLaporan` & `PenulisXlsx` — sheet "Harian" beriterasi sepanjang
  `periode.awal..periode.akhir` (bukan `1..bulan.lengthOfMonth()`), dan teks
  "Rentang" di sheet Ringkasan memakai tanggal asli itu, bukan
  `bulan.atDay(1)`/`atEndOfMonth()`.
- `EksporViewModel` — path dari Pengaturan ("Ekspor laporan bulan ini") memakai
  sentinel `EksporViewModel.SEKARANG` supaya periode "sekarang" dihitung dari
  `hariAwalBulan` yang benar, bukan `YearMonth.now()` mentah dari nav graph
  (yang sebelumnya bisa salah kalau belum lewat tanggal awal bulan).

Tes baru `PeriodeTest` (5 tes) mengunci semantik ini; `LaporanTest` diubah untuk
memakai `Periode` (dengan `hariAwalBulan = 1` tetap menghasilkan bulan kalender
biasa, jadi hasil 31-baris-Agustus yang sudah ada tidak berubah).

### B-4. WorkManager untuk ekspor volume besar (F-6)

Ekspor sekarang berjalan di coroutine `Dispatchers.IO` di dalam `EksporViewModel`.
Untuk volume normal (ratusan transaksi) instan dan tak ada ANR. PRD F-6 minta
`WorkManager` di atas 5.000 baris supaya proses selamat kalau app ditutup. Belum
dikerjakan. `WorkManager` sudah ada di katalog dependensi tapi belum dipakai.

### B-5. Verifikasi file .xlsx di aplikasi spreadsheet (F-6 & DoD)

**Belum dilakukan.** F-6 dan Definition of Done menjadikan "file terbuka bersih
di Microsoft Excel, Google Sheets, LibreOffice Calc, dan Google Sheets Android"
sebagai kriteria rilis. Sesi ini tak bisa membuka file. Perlu kamu:
1. jalankan ekspor di perangkat/emulator
2. buka hasilnya di keempat aplikasi
3. cek: tak ada peringatan "file rusak", kolom nominal terbaca sebagai angka
   (SUM muncul di status bar), total sheet Ringkasan == total di layar,
   saldo berjalan baris terakhir == selisih, catatan dengan koma/kutip/emoji
   utuh.

Unit test `LaporanTest` sudah mengunci angka-angkanya (total, saldo berjalan,
jumlah baris harian, urutan kronologis) — tapi bukan validitas XML-nya.

### B-6. Auto-filter pada header sheet Excel (F-6)

Header sudah **tebal + freeze pane**, tapi **auto filter belum diset** (API
`setAutoFilter` di fastexcel 0.18.4 tak dipastikan tanpa pengujian). Aturan
format lain sebagian: lebar kolom disetel, wrap text di kolom catatan, number
format Rupiah sebagai angka, tanggal sebagai tipe tanggal Excel, baris TOTAL
pakai `=SUM()`.

### B-7. Panah putus-putus ke FAB (Layar utama §9, kondisi kosong)

Dilewati — nilai visual rendah. Kondisi kosong lainnya (ikon + dua baris teks)
sudah ada.

---

## C. Deviasi sadar dari spec (tidak butuh aksi, sekadar transparansi)

| Hal | Spec | Implementasi | Alasan |
|---|---|---|---|
| Segmented control | tinggi 40 dp | **48 dp** | CLAUDE.md aturan 9 (target sentuh) menang |
| Chip kategori / field tanggal-catatan | 38 dp / 46 dp | visual sesuai spec, **area sentuh 48 dp** | idem |
| Catatan di Form transaksi | field sebaris tanggal | **dialog kecil** | layar tak boleh scroll + keypad aplikasi selalu tampil; keyboard sistem akan menutupi keypad |
| Kartu "Transaksi dihapus" (§3) | — | tanpa animasi masuk/keluar (bar mundur tetap beranimasi) | penyederhanaan |
| FAB saat kartu urungkan tampil | — | disembunyikan (fade+scale) | supaya tak bertumpuk tombol URUNGKAN |
| Panel geser-hapus | "selebar 96 dp" | latar merah selebar baris, ikon+teks di 96 dp paling kanan | interpretasi wajar `SwipeToDismissBox` |
| Ambang geser hapus | — | default `SwipeToDismissBox` | spec tak menyebut |
| Ukuran font 14,5 / 12,5 sp | eksplisit di spec | dibulatkan ke 15 / 13 (helper `gaya()` pakai `Int`) | ditambahkan sebagai gaya bernama di `Type.kt` |
| Subtotal hari & "+N kcategori lain" | — | `Rupiah.format` polos, tanpa `−` | teks spec sendiri menulis "Rp X" |
| Cincin warna terpilih (Form kategori §5) | "cincin 3 dp latar + 2 dp warna + centang" | border + padding + centang (aproksimasi) | penyederhanaan |
| "Tanpa izin internet" (Pengaturan) | — | baris info statis "Terverifikasi" | manifest memang tak punya `INTERNET`, tapi ini bukan cek runtime |
| Hanya penghapusan **terakhir** yang bisa diurungkan | — | hapus beruntun < 5 dtk → sebelumnya permanen | sama seperti Snackbar Material biasa |
| Navigasi maju melewati bulan berjalan | — | diizinkan bila ada transaksi bertanggal masa depan | F-1 mengizinkan tanggal masa depan |

---

## D. Edge case kecil yang belum ditangani

- **Kategori terpilih yang sudah diarsipkan** di Form transaksi mode ubah: tak
  muncul sebagai chip → tak kelihatan terpilih, walau `bisaSimpan` tetap `true`.
- **Layar sangat pendek** (< ~620 dp tinggi konten) di Form transaksi: elemen bisa
  terlalu rapat karena spec melarang scroll.
- **Cold start tema**: `MainActivity` render dengan `Tema.SISTEM` sampai DataStore
  terbaca — kilatan singkat mungkin terlihat kalau user memilih tema non-default.
