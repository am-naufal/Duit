# Keputusan tertunda & yang belum dikerjakan

Rekap semua hal yang perlu keputusanmu atau sengaja ditunda, dikumpulkan dari
pengerjaan ketujuh layar (Layar utama, Tambah transaksi, Ubah + hapus, Kelola
kategori, Form kategori, Pengaturan, Ekspor Excel) plus data layer.

Status per 11 September 2026 (diperbarui setelah B-1/B-2/B-3/B-4/B-8 selesai,
B-5 sebagian besar terverifikasi lewat emulator sungguhan — dan menemukan +
memperbaiki dua bug nyata di jalan: TOTAL laporan ekspor mencampur nominal
masuk & keluar, dan `NavBackStackEntry.savedStateHandle` yang ternyata tak
pernah benar-benar terhubung ke ViewModel tujuan — plus D-1 & B-6 ternyata
sudah lama selesai, catatan basi diperbaiki). Verifikasi terakhir:
`assembleDebug`, `testDebugUnitTest` (26 tes hijau), `lintDebug` (0 temuan di
kode `ui/` & `data/`) semua lulus. Release APK dengan R8: **3,24 MB**. Gestur
seret (B-1) sudah diverifikasi manual di emulator sungguhan; mekanisme inti
WorkManager (B-4) juga — Worker benar-benar dijadwalkan lewat
`SystemJobScheduler` Android, bukan diam-diam tetap lewat coroutine biasa.
Sisa pekerjaan nyata: skenario >5.000 baris sungguhan untuk B-4 (di luar
jangkauan percobaan manual di sesi ini), grafik pengeluaran & saldo (belum
dikerjakan), dan verifikasi visual B-5 di aplikasi spreadsheet asli — dicatat
di bagian masing-masing.

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

### B-1. Selesai 11 September 2026 — Seret untuk mengurutkan kategori (Kelola kategori §4)

Gestur seret di handle `ic_geser` sekarang diimplementasikan manual (tanpa
library reorderable — tetap nol dependensi jaringan) di
`KelolaKategoriScreen.kt`, komposabel privat `DaftarAktifBisaDiseret`:
`detectDragGestures` langsung di handle (bukan `AfterLongPress` — handle sudah
jadi target sentuh terpisah dari baris yang bisa diklik untuk mengubah, jadi
tak perlu menunda dengan tekan-lama), `androidx.compose.runtime.key(baris.id)`
supaya state/`pointerInput` tiap baris tidak reset saat urutan berubah, lalu
memanggil `DuitRepository.urutkanKategori` (lewat
`KelolaKategoriViewModel.urutkanUlang`, yang plumbing-nya memang sudah ada)
begitu jari dilepas.

**Deviasi sadar:** hanya baris yang sedang diseret yang beranimasi mengikuti
jari (`graphicsLayer { translationY = … }`); baris lain yang tergeser
posisinya langsung berpindah tanpa animasi geser. Animasi penuh ala
`LazyColumn`'s `Modifier.animateItem()` mengharuskan tiap baris kategori jadi
item ter-lazy sendiri-sendiri, yang akan memecah satu bayangan kartu §4 jadi
berbayang per baris — dianggap tidak sepadan untuk daftar kategori yang pendek.
Kategori sistem ("Lainnya") dikecualikan dari penyeretan dan selalu tetap di
posisi terakhir, sesuai catatan `sistem` di `BarisKategori`.

**Diverifikasi manual 11 September 2026** di emulator sungguhan
(`emulator-5554`, sama seperti verifikasi B-5): `adb shell input swipe` dari
handle `ic_geser` kategori "Belanja" (posisi 1) turun melewati dua baris —
hasilnya langsung berpindah ke posisi 3, di bawah "Tagihan", persis seperti
seharusnya. Dipaksa-tutup aplikasi (`am force-stop`, bukan cuma pindah layar)
lalu dibuka lagi: urutan baru tetap ada, membuktikan `urutkanUlang` benar-benar
menulis ke Room, bukan cuma state lokal. Tap ke baris "Belanja" di posisi
barunya tetap membuka "Ubah kategori" yang benar (nama, ikon, warna, status
arsip cocok) — gestur seret tidak mengganggu klik-untuk-ubah. Tak ada
`FATAL EXCEPTION` di logcat sepanjang percobaan.

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

### B-4. Selesai 11 September 2026 — WorkManager untuk ekspor volume besar (F-6)

Ekspor di atas 5.000 transaksi (`AMBANG_WORKMANAGER` di `EksporViewModel`)
sekarang lewat `data/ekspor/EksporWorker.kt` (`CoroutineWorker`), bukan
coroutine `viewModelScope` biasa — supaya penulisan file tetap selesai kalau
aplikasi ditutup paksa di tengah proses. Volume normal (di bawah 5.000, jauh
di atas pemakaian pribadi wajar) tetap lewat jalur langsung lama, karena instan
dan tak perlu bertahan dari kematian proses.

`EksporWorker` merakit ulang `LaporanBulanan` dari Room sendiri lewat
`inputData` (uri tujuan + rentang `Periode`) — sengaja tidak memakai objek
laporan yang sudah dihitung `EksporViewModel`, karena proses yang menjadwalkan
Worker itu bisa saja sudah mati saat Worker benar-benar jalan.
`EksporViewModel` mengamati progres/hasil lewat `WorkManager.getWorkInfoByIdFlow`.

**Keputusan permission yang kamu setujui:** menambah `androidx.work:work-runtime-ktx`
menarik empat permission lewat manifest library itu sendiri —
`RECEIVE_BOOT_COMPLETED`, `ACCESS_NETWORK_STATE`, `FOREGROUND_SERVICE`, `WAKE_LOCK`
(plus satu signature permission internal `<app>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`,
tak terlihat pengguna). Tiga yang pertama dicoret lewat `tools:node="remove"` di
`AndroidManifest.xml` karena tak dipakai fitur ini (tak ada constraint jaringan,
tak ada reschedule-setelah-reboot, tak ada `setForeground()`) — terutama
`ACCESS_NETWORK_STATE` yang paling bertentangan dengan janji "tanpa permission
jaringan" (CLAUDE.md aturan 1). `WAKE_LOCK` **dipertahankan** karena itu yang
membuat proses tulis-file benar-benar bertahan saat layar mati/app ditutup —
inti alasan WorkManager dipakai. Manifest akhir sudah diverifikasi lewat
`app/build/intermediates/merged_manifest/debug/.../AndroidManifest.xml`: hanya
`WAKE_LOCK` + permission signature internal itu yang tersisa.

Versi `work-runtime-ktx` dipatok ke **2.10.0** (bukan rilis terbaru) karena
sesi ini tak punya akses jaringan untuk mengambil versi lain — itu yang sudah
ada di cache Gradle lokal. `lintDebug` menandainya sebagai "versi lebih baru
tersedia (2.11.2)", sama seperti dependensi lain di proyek ini; boleh dinaikkan
kapan saja lewat Android Studio yang punya akses internet untuk build.

**Sebagian diverifikasi 11 September 2026** di emulator sungguhan:
`AMBANG_WORKMANAGER` diturunkan sementara ke `0` (dikembalikan ke `5_000`
setelah selesai — cek `git status` bersih, tak ada diff tersisa), lalu ekspor
dipicu seperti biasa. Logcat mengonfirmasi jalurnya benar-benar lewat
WorkManager, bukan diam-diam tetap lewat coroutine langsung:
`WM-GreedyScheduler: Starting work for ... WM-WorkerWrapper: Starting work for
com.alenza.duit.data.ekspor.EksporWorker`, progres ter-update
(`Data {progres : 0.2}` → `1.0`), lalu `Worker result SUCCESS`. Ini
membuktikan mekanisme intinya benar — Worker terdaftar ke `SystemJobScheduler`
milik Android sendiri (bukan `viewModelScope`), yang secara desain memang
bertahan dari kematian Activity/ViewModel.

**Belum diverifikasi:** skenario "aplikasi ditutup paksa di tengah tulis file
5.000+ baris" yang sebenarnya — dengan hanya beberapa transaksi di Room,
penulisan selesai dalam hitungan milidetik (terlalu cepat untuk disela
`am force-stop` secara manual), dan membuat >5.000 transaksi sungguhan di
emulator tidak praktis lewat UI automation. `testDebugUnitTest` juga tak
menyentuh jalur ini sama sekali (butuh Robolectric/instrumentasi, bukan tes
JVM biasa). Kalau mau menutup celah terakhir ini: buat >5.000 transaksi
sungguhan (atau turunkan `AMBANG_WORKMANAGER` sementara lagi dengan dataset
kecil tapi tambahkan delay artifisial di `PenulisXlsx` untuk memperlambat
tulisnya), lalu tutup paksa aplikasi persis di tengah progres.

### B-5. Sebagian besar selesai 11 September 2026 — dan menemukan bug nyata

Sesi ini ternyata **punya** akses ke emulator Android yang sedang berjalan
(bukan cuma JVM), jadi bagian dari B-5 bisa dikerjakan langsung, bukan cuma
didokumentasikan sebagai "perlu kamu". Yang dilakukan:

1. **Menemukan & memperbaiki bug pemutus file nyata.** `FORMAT_RUPIAH` di
   `PenulisXlsx.kt` memakai teks literal berkutip (`"\"Rp\"#,##0;[Red]-\"Rp\"#,##0"`).
   fastexcel 0.18.4 menulis string itu apa adanya ke atribut XML
   `<numFmt formatCode="...">` di `xl/styles.xml` **tanpa meng-escape tanda
   kutip di dalamnya** — hasilnya `formatCode=""Rp"#,##0;[Red]-"Rp"#,##0"`,
   yang memutus atribut itu sendiri. Setiap file .xlsx yang pernah diekspor
   aplikasi ini kemungkinan besar akan memicu peringatan "file rusak, coba
   perbaiki?" di Excel/Sheets/LibreOffice manapun — persis skenario terburuk
   yang dikhawatirkan F-6/DoD. **Diperbaiki** dengan escape backslash-per-huruf
   (`\R\p#,##0;[Red]-\R\p#,##0`, valid dan setara di sintaks format Excel,
   tak melibatkan tanda kutip sama sekali).
2. **Tes permanen baru** `PenulisXlsxTest.kt` mem-parse ULANG setiap entry XML
   di dalam file .xlsx yang sungguhan ditulis `PenulisXlsx` (lewat `ZipFile` +
   `DocumentBuilder`) dan menuntut semuanya well-formed — kelas bug ini kini
   akan selalu tertangkap `testDebugUnitTest`, bukan cuma kelihatan kalau ada
   yang iseng membuka filenya. `LaporanTest` yang lama hanya mengunci
   angka-angka, tak pernah membuka file yang sungguhan ditulis.
3. **Dikonfirmasi lewat 3 jalur independen**, sebelum dan sesudah perbaikan:
   - JVM: harness manual (dihapus setelah dipakai) menulis file edge-case
     (koma, kutip ganda, emoji dengan surrogate pair, `<`, `&`, apostrof di
     catatan) — sebelum perbaikan `openpyxl` gagal total dengan
     `ParseError: not well-formed`; sesudah perbaikan semua utuh persis,
     termasuk catatan edge-case-nya.
   - Emulator sungguhan (`emulator-5554`): transaksi ditambah lewat UI,
     ekspor dipicu lewat "Ekspor laporan bulan ini" → SAF picker → simpan ke
     Downloads, ditarik lewat `adb pull`, divalidasi dengan `openpyxl` —
     9 entry XML semua well-formed, total Ringkasan (Pemasukan 4.000.000 /
     Pengeluaran 850.000 / Selisih 3.150.000) cocok persis dengan yang
     tertampil di layar, saldo berjalan baris terakhir == selisih.
   - Tak ada crash/`FATAL EXCEPTION` di logcat selama seluruh alur.

**Masih perlu kamu** (di luar jangkauan sesi ini): membuka file hasil ekspor
di aplikasi spreadsheet SUNGGUHAN — Microsoft Excel, Google Sheets (web &
Android), LibreOffice Calc — karena "well-formed XML" belum tentu sama dengan
"semua aplikasi menampilkannya persis seperti yang dimaksud" (mis. locale
angka, lebar kolom di layar kecil, rendering emoji). Risiko file benar-benar
gagal dibuka sudah jauh berkurang setelah bug di atas diperbaiki, tapi
verifikasi visual di aplikasi asli tetap DoD yang belum tercentang.

Catatan sampingan: mengetik teks lewat `adb shell input text` di emulator ini
sangat tidak reliable untuk karakter khusus (kutip, emoji) — huruf sering
hilang/tertukar urutan bahkan untuk teks ASCII biasa. Untuk verifikasi konten
presisi, tulis lewat harness JVM langsung ke `PenulisXlsx`, bukan lewat
simulasi ketikan di UI.

### B-6. Sudah selesai — Auto-filter pada header sheet Excel (F-6)

Catatan ini juga sudah basi: `ws.setAutoFilter(...)` sudah dipanggil di
`PenulisXlsx.kt` untuk ketiga sheet (Ringkasan, Transaksi, Harian), API
`setAutoFilter` fastexcel 0.18.4 memang ada dan kompilasinya lulus. Header
tebal + freeze pane + lebar kolom + wrap text catatan + number format Rupiah +
tanggal sebagai tipe tanggal Excel + baris TOTAL `=SUM()` — semua sudah ada.
Yang **masih** belum diverifikasi: apakah filter itu benar-benar berfungsi
saat file dibuka di aplikasi spreadsheet sungguhan — itu bagian dari B-5 di
bawah, bukan pekerjaan kode lagi.

### B-7. Panah putus-putus ke FAB (Layar utama §9, kondisi kosong)

Dilewati — nilai visual rendah. Kondisi kosong lainnya (ikon + dua baris teks)
sudah ada.

### B-8. Selesai 11 September 2026 — Alert pemberitahuan untuk semua aksi CRUD

Diminta pengguna: pemberitahuan saat menambah/mengubah/menghapus/dan
lain-lain, cakupan penuh termasuk Pengaturan. Ditambahkan:

- `ui/Pemberitahuan.kt` — kelas kecil (pola dipakai ulang, bukan ditulis 4x)
  yang menyimpan satu pesan `StateFlow<String?>` dan menghapusnya sendiri
  setelah 2,5 detik.
- `ui/komponen/KartuNotifikasi.kt` — kartu mengambang gaya sama dengan
  `KartuUrungkan` (§3) tapi tanpa tombol aksi, dipakai untuk konfirmasi yang
  tak bisa/perlu diurungkan.
- Dipasang di: Layar utama (tambah/ubah transaksi — "Transaksi
  ditambahkan"/"diperbarui"), Kelola kategori (tambah/ubah/arsipkan/hapus
  kategori dari Form kategori, plus pulihkan & urutkan langsung di layar itu
  sendiri), Pengaturan (ganti tema/format tanggal/hari awal bulan).
  Penghapusan transaksi tetap pakai `KartuUrungkan` yang sudah ada (dengan
  tombol URUNGKAN) — tidak diduplikasi jadi dua kartu.

**Bug arsitektur nyata ditemukan & diperbaiki di jalan.** Pesan dari
Tambah/Form kategori awalnya dikirim ke layar sebelumnya lewat
`NavBackStackEntry.savedStateHandle` — pola resmi Navigation Compose untuk
"return a result", dan persis pola yang SUDAH DIPAKAI kode lama untuk
forwarding hapus transaksi (`KUNCI_HAPUS_DARI_FORM`). Saat diverifikasi
manual di emulator, pesan tak pernah muncul. Debugging dengan
`System.identityHashCode` membuktikan: `SavedStateHandle` yang didapat lewat
`previousBackStackEntry.savedStateHandle` **bukan instance yang sama** dengan
`SavedStateHandle` yang di-inject ke constructor `UtamaViewModel` lewat
`viewModel()` — bahkan sejak komposisi pertama, bukan cuma setelah navigasi.
Akibatnya nilai yang di-`set()` di satu sisi tak pernah "terlihat" oleh
`getStateFlow` di sisi lain.

Ini artinya **forwarding hapus transaksi dari Form transaksi juga sudah lama
tak berfungsi** sebelum sesi ini — bug lama yang tak pernah ketahuan karena
belum pernah dicoba manual di perangkat sungguhan sampai sekarang.

Diperbaiki dengan `ui/HasilAntarLayar.kt`: singleton sederhana (pola sama
seperti `Preferensi`/`DuitRepository`) berisi beberapa `StateFlow` untuk
membawa hasil antar layar, menggantikan `NavBackStackEntry.savedStateHandle`
sepenuhnya untuk kasus ini. `UtamaViewModel` tak lagi butuh `SavedStateHandle`
sama sekali (dulu cuma dipakai untuk forwarding ini). Diverifikasi ulang di
emulator: tambah transaksi, hapus transaksi dari Form (kartu Urungkan), dan
tambah kategori — ketiganya sekarang menampilkan kartu yang benar.

`assembleDebug`, `testDebugUnitTest` (26 tes hijau, tak berubah — perbaikan
ini murni jalur UI/navigasi, tak tersentuh tes JVM), `lintDebug` (0 temuan
ui/ & data/) semua lulus.

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

- ~~Kategori terpilih yang sudah diarsipkan di Form transaksi mode ubah: tak
  muncul sebagai chip.~~ **Sudah ditangani** — `TambahViewModel.sisipkanArsipTerpilih`
  menyisipkan kategori terarsip yang masih jadi kategori terpilih transaksi
  yang sedang diubah kembali ke daftar chip (`KategoriChip.arsip`), supaya
  tetap kelihatan terpilih. Catatan ini keliru, dibiarkan tercoret sebagai jejak.
- **Layar sangat pendek** (< ~620 dp tinggi konten) di Form transaksi: elemen bisa
  terlalu rapat karena spec melarang scroll.
- **Cold start tema**: `MainActivity` render dengan `Tema.SISTEM` sampai DataStore
  terbaca — kilatan singkat mungkin terlihat kalau user memilih tema non-default.
