---
description: Implementasikan satu layar dari docs/design-spec.md
---

Implementasikan layar: **$ARGUMENTS**

Langkah:

1. Baca `CLAUDE.md`, lalu bagian yang relevan di `docs/design-spec.md` dan
   `docs/prd.md`. Jangan menebak nilai yang sudah tertulis di sana.
2. Lihat `app/src/main/java/com/duit/ui/theme/` dan pakai token dari sana.
   Kalau butuh nilai baru, tambahkan ke `Dimens.kt` dulu.
3. Cek `ui/komponen/` — pakai ulang yang sudah ada, jangan bikin kembar.
   Kalau ada bagian layar ini yang jelas akan dipakai layar lain, taruh di sana.
4. Buat berkas di `ui/layar/<nama>/`: `<Nama>Screen.kt`, `<Nama>ViewModel.kt`,
   dan data class state-nya. State di-hoist; composable tidak menyentuh Room
   langsung.
5. Buat `@Preview` untuk mode terang DAN gelap, dengan data contoh yang masuk
   akal (rupiah realistis, bukan 123456).
6. Jalankan `./gradlew assembleDebug`. Perbaiki sampai hijau.
7. Laporkan singkat: berkas apa yang dibuat, bagian mana dari spesifikasi yang
   belum bisa dikerjakan dan kenapa.

Aturan:

- Target sentuh minimum 48×48 dp, termasuk tombol ikon.
- Setiap nominal lewat `Rupiah`, dan gaya teksnya tabular.
- Jangan menggambar status bar atau keyboard sistem palsu.
- Kalau spesifikasi bertabrakan dengan kode yang sudah ada, **berhenti dan
  tanya** — jangan diam-diam memilih salah satu.
