# 🛴 scootah!

> Scooter kullanıcıları için modern, hafif bir MVP Android prototipi.

## 📱 Ekranlar

| Ekran | Açıklama |
|-------|----------|
| **Karşılama** | Animasyonlu giriş + Mock Google Sign-In |
| **Kurulum** | Kullanıcı adı & scooter isim girişi |
| **Ana Sayfa** | Selamlama, Streak barı, Eko etki kartları |
| **Garaj** | Scooter profil kartı, istatistikler, ayarlar |
| **Harita** | Canvas harita mock'u, yol güvenlik katmanı, canlı sürüş paneli |

## 🏗️ Mimari

```
MVVM / MVI — StateFlow tek yönlü veri akışı
│
├── data/
│   ├── model/UserProfile.kt      → Veri modeli
│   └── repository/UserRepository → In-memory single source of truth
│
├── ui/
│   ├── onboarding/               → Welcome + Setup ekranları
│   ├── home/                     → Ana sayfa
│   ├── garage/                   → Garaj/profil
│   └── map/                      → Harita mock
│
└── navigation/                   → NavHost + BottomNav
```

## 🛠️ Teknoloji

- **Kotlin** 2.0.21 + JVM 17
- **Jetpack Compose** (BOM 2024.11.00)
- **Material 3**
- **Navigation Compose** 2.8.4
- **StateFlow** (MVVM/MVI)
- **Gradle Version Catalog** (`libs.versions.toml`)
- ❌ Harici API / Firebase yok — tamamen offline

## 🚀 Projeyi Açma

1. Android Studio Ladybug (2024.2+) ile aç
2. `File → Open → scootah/` klasörünü seç
3. Gradle sync bekle
4. Emülatörde çalıştır (API 26+)

## ✨ Özellikler

### Ana Sayfa
- 🕐 Saate göre Türkçe selamlama (Günaydın / İyi günler / İyi akşamlar / İyi geceler)
- 🔥 Duolingo tarzı haftalık streak barı + gün kutuları
- 🌿 CO₂ ve yakıt tasarrufu eko etki kartları

### Harita Mock
- Canvas ile çizilen güvenli (yeşil) / riskli (kırmızı) / az kullanılan (gri) yol katmanları
- Animasyonlu scooter ikonu (sürüş sırasında nabız etkisi)
- Canlı hız / mesafe / süre paneli
- Simüle edilmiş sinüsoidal hız (0–25 km/h)

### Garaj
- Scooter profil kartı + toplam km & seri istatistikleri
- Ayarlar menüsü placeholder'ları

## 📁 Proje Yapısı

```
scootah/
├── gradle/libs.versions.toml        ← Version catalog
├── build.gradle.kts                 ← Root build
├── settings.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts             ← App dependencies
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/scootah/app/
        │   ├── MainActivity.kt
        │   ├── ScootahApp.kt
        │   ├── navigation/
        │   │   ├── Screen.kt
        │   │   ├── AppNavigation.kt
        │   │   └── MainScreen.kt
        │   ├── data/
        │   │   ├── model/UserProfile.kt
        │   │   └── repository/UserRepository.kt
        │   ├── ui/
        │   │   ├── theme/{Color,Theme,Type}.kt
        │   │   ├── onboarding/{WelcomeScreen,SetupScreen,OnboardingViewModel}.kt
        │   │   ├── home/{HomeScreen,HomeViewModel}.kt
        │   │   ├── garage/{GarageScreen,GarageViewModel}.kt
        │   │   └── map/{MapScreen,MapViewModel}.kt
        │   └── util/TimeUtils.kt
        └── res/
            ├── values/{strings,colors,themes}.xml
            ├── drawable/ic_launcher_foreground.xml
            └── xml/data_extraction_rules.xml
```
