# Splash Screen "Duit Pribadi" — Jetpack Compose

Implementasi splash screen sesuai mockup: background terang, blob gradien biru (kiri atas)
& magenta (kanan bawah), tile logo dompet, judul dua warna, tagline, dan page indicator.

## Isi paket

```
app/src/main/
├── AndroidManifest.xml                         # MainActivity pakai Theme.DuitPribadi.Starting
├── java/com/duitpribadi/app/
│   ├── MainActivity.kt                         # installSplashScreen() + handover ke Compose
│   ├── HomePlaceholder.kt                      # ganti dengan Beranda asli
│   └── ui/
│       ├── theme/Color.kt                      # palet + BrandGradient
│       ├── theme/Theme.kt                      # MaterialTheme light/dark
│       └── splash/SplashScreen.kt              # composable utama + @Preview
└── res/
    ├── drawable/ic_duit_logo.xml               # vector logo dompet (gradien)
    ├── values/colors.xml
    ├── values/themes.xml                       # Theme.SplashScreen (core-splashscreen)
    └── values-night/themes.xml
```

## 1. Dependency (app/build.gradle.kts)

```kotlin
android {
    compileSdk = 35
    defaultConfig { minSdk = 24; targetSdk = 35 }
    buildFeatures { compose = true }
    // vector drawable dengan gradient butuh ini untuk API < 24 (kalau minSdk diturunkan)
    defaultConfig { vectorDrawables.useSupportLibrary = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")   // WAJIB
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
}
```

## 2. Cara kerja

Dua lapis, supaya tidak ada layar putih saat cold start:

1. **Splash sistem** (`Theme.DuitPribadi.Starting`) — muncul instan begitu ikon di-tap,
   menampilkan `ic_duit_logo` di atas background `#F7F9FE`. Ditahan oleh
   `setKeepOnScreenCondition { !appReady }` selama inisialisasi (buka Room, baca preferensi).
2. **Splash Compose** (`SplashScreen.kt`) — mengambil alih dengan animasi penuh:
   logo *spring scale-in*, judul & tagline *fade + slide up*, blob bergerak pelan,
   dot indicator berdenyut. Setelah `holdMillis` (default 1600 ms) memanggil `onFinished()`.

## 3. Menyambungkan ke Beranda

Di `MainActivity.kt`, ganti `HomePlaceholder()` dengan `NavHost` / `HomeScreen()` milikmu:

```kotlin
AnimatedVisibility(visible = !showSplash, enter = fadeIn(tween(400)) + scaleIn(0.98f)) {
    DuitPribadiNavHost()          // <- punyamu
}
```

Dan ganti `appReady = true` dengan kondisi asli, misalnya:

```kotlin
lifecycleScope.launch {
    repository.warmUp()           // buka database, migrasi, dsb
    appReady = true
}
```

## 4. Penyesuaian cepat

| Mau ubah | Di mana |
|---|---|
| Durasi splash | `SplashScreen(holdMillis = 1600L)` |
| Warna brand | `ui/theme/Color.kt` → `DuitColor` |
| Bentuk blob | `BrandBlobs()` di `SplashScreen.kt` (koordinat relatif `w`/`h`, aman untuk semua ukuran layar) |
| Tagline | parameter `text` pada `Text` kedua |
| Ikon | `res/drawable/ic_duit_logo.xml` |

## 5. Catatan

- `installSplashScreen()` **harus** dipanggil sebelum `super.onCreate()` / `setContent()`.
- Tema `Theme.DuitPribadi.Starting` dipasang di level `<activity>`, bukan `<application>`,
  supaya activity lain tidak ikut menampilkan splash.
- Jangan menahan splash sistem lebih dari ~1 detik (Android bisa menganggap app hang);
  animasi panjang biar ditangani lapis Compose.
- Preview tersedia langsung di Android Studio: `SplashPreview` (light) & `SplashPreviewDark`.
