# Cara mengimplementasikan tampilan lewat Claude Code

Urutan yang bekerja, dan alasan tiap langkahnya.

---

## 0. Siapkan project dulu, bukan lewat Claude Code

Buat project baru di Android Studio: **Empty Activity (Compose)**, package
`com.duit`, minSdk 26. Jalankan sekali sampai muncul "Hello Android" di emulator.

Kenapa manual: Claude Code bisa saja membuat struktur Gradle dari nol, tapi
versi plugin dan katalog dependensi berubah cepat dan hasilnya sering tidak
compile. Android Studio memberi kerangka yang pasti benar hari ini. Mulai dari
sesuatu yang sudah hijau, jangan dari sesuatu yang harus diperbaiki dulu.

## 1. Salin paket handoff ke root project

```
duit/
├─ CLAUDE.md                  ← memori project untuk Claude Code
├─ docs/prd.md
├─ docs/design-spec.md
├─ .claude/commands/layar.md  ← slash command /layar
└─ app/src/main/…             ← theme + ikon, langsung pakai
```

`CLAUDE.md` adalah bagian paling penting dari paket ini. Claude Code membacanya
di setiap sesi, jadi aturan seperti "nominal selalu `Long`" dan "tanpa permission
INTERNET" tidak perlu kamu ulang-ulang — dan tidak akan pelan-pelan bocor.

## 2. Tambahkan dependensi

Di sesi Claude Code pertama:

```
Baca CLAUDE.md. Tambahkan dependensi yang dibutuhkan ke libs.versions.toml dan
app/build.gradle.kts: Room (+ KSP), DataStore, Navigation Compose,
ui-text-google-fonts, dan lifecycle-viewmodel-compose. Pakai versi stabil
terbaru yang kompatibel dengan Compose BOM yang sudah ada. Jangan tambahkan
apa pun yang butuh akses jaringan. Setelah itu jalankan ./gradlew assembleDebug
dan pastikan hijau.
```

Lalu buka `AndroidManifest.xml` dan pastikan tidak ada `INTERNET` di sana.

**Font.** Unduh **Plus Jakarta Sans** dari fonts.google.com (SIL Open Font
License), ambil 4 bobot — Regular, Medium, SemiBold, Bold — dan taruh di
`app/src/main/res/font/` dengan nama `plus_jakarta_sans_regular.ttf` dst.
(nama persis ada di komentar `Type.kt`). Ini dikerjakan manual, bukan lewat
Claude Code: sesi ini tidak punya akses ke berkas fontnya, dan font yang salah
bobot akan membuat seluruh tampilan terasa meleset tanpa jelas kenapa.
Sertakan `OFL.txt` di `app/src/main/assets/` — lisensinya mewajibkan.

## 3. Data layer dulu, tampilan belakangan

Ini yang sering dibalik dan bikin rugi waktu. Kalau tampilan dibuat lebih dulu,
composable-nya akan dibentuk mengikuti data karangan, lalu harus dibongkar lagi
saat Room masuk.

```
Buat data layer sesuai docs/prd.md §7: entity Transaction dan Category,
DAO, database Room versi 1 dengan migrasi eksplisit, dan seeder 12 kategori
bawaan yang jalan sekali saat database dibuat. Index pada Transaction.date dan
Transaction.categoryId. Tulis unit test untuk perhitungan ringkasan bulanan
(total masuk, total keluar, selisih, rekap per kategori dengan pembulatan
persentase sisa terbesar) dan untuk Rupiah.format. Jalankan testnya.
```

## 4. Satu layar referensi, dikerjakan pelan-pelan

Jangan minta sepuluh layar sekaligus. Buat **satu** layar sampai benar-benar
rapi, lalu jadikan itu contoh untuk sisanya.

```
/layar Layar utama
```

Setelah selesai, buka `@Preview`-nya dan bandingkan dengan artboard. Perbaiki
sampai cocok. Setiap koreksi yang kamu berikan di tahap ini terbayar sepuluh kali
di layar berikutnya — karena langkah 5 menyuruh Claude meniru layar ini.

## 5. Sisanya, satu per satu, dengan layar referensi disebut eksplisit

```
/layar Tambah transaksi
```

lalu tambahkan di prompt yang sama:

```
Ikuti persis pola yang sudah dipakai di ui/layar/utama/ — struktur file,
penamaan, cara ambil warna dari theme, cara bikin preview. Kalau ada komponen
di ui/komponen/ yang bisa dipakai ulang, pakai; jangan bikin kembar.
```

Urutan yang masuk akal: **Layar utama → Tambah transaksi → Ubah + hapus →
Kelola kategori → Form kategori → Pengaturan → Ekspor Excel**.

Ekspor ditaruh terakhir bukan karena tidak penting, tapi karena dia satu-satunya
fitur yang menyentuh library luar; kalau ternyata `fastexcel` bermasalah dengan
desugaring, kamu sudah punya aplikasi yang jalan sambil mencari jalan lain.

## 6. Ekspor Excel

```
Implementasikan F-6 dari docs/prd.md. Pakai fastexcel (dhatim) dulu. Setelah
terpasang, ukur ukuran APK release dengan ./gradlew assembleRelease dan laporkan
angkanya. Kalau lewat 15 MB, berhenti dan beri tahu saya sebelum lanjut —
rencana cadangannya menulis SpreadsheetML manual dengan ZipOutputStream.
```

Perhatikan bentuk prompt itu: ada **titik berhenti yang jelas**. Tanpa itu,
Claude Code akan memilih sendiri saat menemui hambatan, dan kamu baru tahu
setelah keputusannya terlanjur tertanam di banyak file.

## 7. Verifikasi yang tidak bisa ditawar

Sebelum menganggap sebuah layar selesai:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Lalu buka file hasil ekspor di Excel, Google Sheets, dan LibreOffice. PRD
menjadikan ini kriteria rilis, bukan pemeriksaan sekali jalan — file .xlsx yang
XML-nya sedikit meleset tetap "berhasil dibuat" tapi ditolak saat dibuka.

---

## Yang perlu dijaga saat bekerja dengan Claude Code

**Beri nilai, bukan gambar.** Screenshot artboard terlihat membantu, tapi Claude
jauh lebih akurat membaca `docs/design-spec.md` yang menyebut "36 dp radius 13"
daripada menebak dari piksel. Gambar untuk kamu, angka untuk Claude.

**Satu tugas satu sesi.** Kalau satu percakapan sudah melewati beberapa layar,
mulai sesi baru. `CLAUDE.md` membuat sesi baru tidak kehilangan konteks penting,
dan konteks yang bersih menghasilkan kode yang lebih konsisten.

**Commit tiap layar selesai.** Ini yang membuat kamu berani menyuruh Claude Code
mencoba pendekatan lain — kalau hasilnya lebih buruk, `git checkout .` dan
selesai.

**Curigai kesepakatan yang terlalu cepat.** Kalau kamu bilang "warna ini kurang
pas" dan Claude langsung mengganti tanpa bertanya yang mana, kemungkinan besar
dia menebak. Sebut elemennya.

**Jangan biarkan aturan mengendur diam-diam.** Kalau muncul `Double` untuk
nominal, atau `Icons.Default.Add` menggantikan `ic_plus`, tegur saat itu juga.
Satu pengecualian yang dibiarkan akan jadi pola di sepuluh file berikutnya.
