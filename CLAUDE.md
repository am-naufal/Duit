# Duitku — aplikasi keuangan pribadi

Android native. Pemakaian pribadi, satu perangkat, tanpa server.
Spesifikasi lengkap: `docs/prd.md`. Detail tampilan: `docs/design-spec.md`.

## Stack

Kotlin · Jetpack Compose + Material 3 · Room (SQLite) · Coroutines + Flow ·
DataStore · WorkManager (hanya untuk ekspor besar) · minSdk 29 / targetSdk 37.

## Aturan yang tidak boleh dilanggar

1. **Tidak ada permission `INTERNET` di AndroidManifest.** Ini janji produk yang
   bisa diverifikasi siapa pun. Jangan tambahkan library yang membutuhkannya.
2. **Tidak ada permission penyimpanan.** Ekspor file lewat Storage Access
   Framework (`ACTION_CREATE_DOCUMENT`).
3. **Nominal selalu `Long` dalam rupiah penuh.** Tidak pernah `Double`, tidak
   pernah `Float`. Tanpa desimal.
4. **Tanggal disimpan sebagai epoch day (`Long`)**, konversi tampilan pakai
   `java.time`.
5. **Migrasi Room ditulis eksplisit sejak versi 1.** `fallbackToDestructiveMigration`
   dilarang — pengguna belum punya backup, kehilangan data tidak bisa dipulihkan.
6. **Sumber kebenaran tunggal adalah Room.** UI mengamati `Flow` dari DAO.
   Jangan simpan salinan state yang bisa basi di ViewModel.
7. **Format rupiah hanya lewat `Rupiah` di `ui/theme/Rupiah.kt`.** Jangan bikin
   formatter kedua di tempat lain.
8. **Angka selalu tabular.** Setiap `Text` yang menampilkan nominal memakai
   `fontFeatureSettings = "tnum"` (sudah ada di gaya `displayMedium`,
   `displaySmall`, dan helper `Angka`).
9. **Target sentuh minimum 48×48 dp.** Termasuk tombol ikon di app bar.
10. **Tanpa analytics, crash reporter pihak ketiga, iklan, atau SDK jaringan.**

## Nilai desain

Semua warna, tipografi, spasi, sudut, dan ukuran sudah ada di
`app/src/main/java/com/alenza/duit/ui/theme/`. **Ambil dari sana, jangan tulis
angka mentah di composable.** Kalau butuh nilai yang belum ada, tambahkan ke
`Dimens.kt` dulu baru dipakai.

Kontras teks/latar di file itu sudah diukur lolos WCAG AA. Kalau kamu mengubah
sebuah warna, ukur ulang rasio kontrasnya sebelum commit — termasuk kalau cuma
menambah token baru (mis. warna brand sekunder), bukan cuma saat mengganti
nilai lama.

Ikon: vector drawable di `res/drawable/ic_*.xml`, digambar stroke 1,85 pada grid 24. Tinting lewat parameter `tint` di `Icon()`. Jangan pakai emoji, jangan pakai
`Icons.Default.*` bawaan Material — gaya garisnya berbeda dan akan terlihat
campur aduk. **Pengecualian:** brand mark (`ic_logo_duitku.xml`, ikon launcher
`ic_launcher_background/foreground.xml`) boleh multi-warna/gradien — itu logo,
bukan ikon fungsional, sama seperti ikon aplikasi Android pada umumnya.

## Struktur

```
ui/theme/      Color, Type, Dimens, Theme, Rupiah, CategoryPalette
ui/komponen/   composable yang dipakai lebih dari satu layar
ui/layar/      satu paket per layar (Screen + ViewModel + state)
data/          entity Room, DAO, database, repository
data/ekspor/   penulis file .xlsx
```

## Cara memverifikasi pekerjaan

Jangan bilang selesai sebelum ini lewat:

```bash
./gradlew assembleDebug          # harus compile
./gradlew testDebugUnitTest      # perhitungan ringkasan & format rupiah
./gradlew lintDebug              # aksesibilitas & manifest
```

Setiap composable layar wajib punya `@Preview` mode terang DAN gelap. Preview
adalah cara tercepat melihat regresi tampilan — buat dulu, baru isi logikanya.

## Gaya kerja yang diharapkan

- Kerjakan satu layar sampai tuntas (composable + preview + state) sebelum pindah.
- Baca `docs/design-spec.md` untuk layar yang sedang dikerjakan, bukan menebak.
- Kalau spesifikasi dan kode yang sudah ada bertabrakan, berhenti dan tanya —
  jangan diam-diam memilih salah satu.
- Bahasa UI: Indonesia. Nama variabel dan komentar boleh Indonesia (sudah
  dipakai di `ui/theme/`), yang penting konsisten.
