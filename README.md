# StepBooster — Instalare & Rulare

## Ce face aplicația
Înmulțește pașii detectați de senzorul fizic și îi scrie în Health Connect.
Multiplicator configurabil între **1.5x și 5.0x** (pași din 0.5 în 0.5).
Rulează continuu în fundal, supraviețuiește lock screen, swipe din recents și restart telefon.

---

## PASUL 1 — Cerințe
- **Android Studio** Hedgehog (2023.1.1) sau mai nou → https://developer.android.com/studio
- **Java 11+** (vine inclus cu Android Studio)
- **Android SDK 34** (se descarcă automat la primul sync)
- **Health Connect** instalat pe telefon
  - Android 14+: vine preinstalat
  - Android 13: Play Store → caută "Health Connect"

---

## PASUL 2 — Deschide proiectul
1. Dezarhivează `StepBooster.zip`
2. Android Studio → **File → Open** → selectează folderul `StepBooster`
3. Așteaptă **Gradle Sync** (prima dată ~3-5 minute, descarcă dependențele)

---

## PASUL 3 — Conectează telefonul
1. Telefon → **Setări → Opțiuni dezvoltator → Depanare USB** → ON
   *(Dacă nu apare: Setări → Despre telefon → apasă de 7x pe "Număr compilare")*
2. Conectează prin USB
3. Acceptă "Permite depanarea USB" pe telefon
4. În Android Studio, sus, selectează telefonul din lista de dispozitive

---

## PASUL 4 — Rulează
- Click pe butonul **▶ Run** (sau Shift+F10)
- Aplicația se instalează și pornește automat pe telefon

---

## PASUL 5 — Configurare pe telefon
1. Apasă **"Solicită Permisiuni Health Connect"** → acordă toate permisiunile
2. Apasă **"Dezactivează Optimizare Baterie"** → selectează "Nu optimiza"
3. **Xiaomi/MIUI**: Setări → Gestionare aplicații → StepBooster → Autostart → ON
4. **Samsung**: Baterie → Limite utilizare fundal → Aplicații care nu dorm → Adaugă StepBooster
5. Setează multiplicatorul dorit cu slider-ul
6. Apasă **PORNEȘTE StepBooster**

---

## Structura proiectului
```
StepBooster/
├── app/src/main/java/com/example/stepbooster/
│   ├── MainActivity.kt          ← Ecranul principal
│   ├── MainViewModel.kt         ← Logica UI
│   ├── StepBoosterService.kt    ← Serviciu fundal + senzor + WakeLock
│   ├── HealthConnectManager.kt  ← Scriere pași în Health Connect
│   ├── MultiplierDataStore.kt   ← Salvare multiplicator
│   ├── BootReceiver.kt          ← Repornire după restart telefon
│   ├── WatchdogAlarmReceiver.kt ← Watchdog la 5 minute
│   └── ui/
│       ├── MainScreen.kt        ← UI Compose complet
│       └── theme/Theme.kt
└── app/src/main/AndroidManifest.xml
```
