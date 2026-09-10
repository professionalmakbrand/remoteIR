# 📱 IR Remote AC TV Freeware

[![License](https://img.shields.io/badge/license-Freeware-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/targetSdk-36%20(Android%2016)-brightgreen.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-24%20(Android%207.0)-orange.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![Ads](https://img.shields.io/badge/Ads-100%25%20Free%20%26%20Zero%20Ads-success.svg)](#)

A 100% free, completely ad-free, and open universal infrared remote control app for Android. Designed to replace broken or lost physical remotes for Air Conditioners, Televisions, and Streaming Media Boxes (including older non-smart models from 2005–2020) using your smartphone's built-in IR blaster.

---

## 🌟 Key Highlights & Philosophy

- **100% Free & Zero Ads Forever:** Unlike commercial Play Store remote apps that charge subscriptions, lock buttons, or play full-screen video ads, this app is completely free with zero ads.
- **Works 100% Offline:** Uses the phone's native hardware `ConsumerIrManager` emitter. No internet connection required to transmit remote commands.
- **Multi-Code Verification Engine:** Includes multiple candidate infrared protocol profiles for every brand, covering modern inverters as well as older window and split units.
- **OnePlus-Style QR Remote Sharing:** Share any configured remote by generating a QR code. Scan the QR on another phone to auto-configure the identical remote in 1 tap.
- **Add to Home Screen (Pinned Shortcuts):** Pin 1-tap launcher shortcuts for direct access to your bedroom AC, living room TV, or set-top box.
- **Modern Edge-to-Edge Design:** Fully compliant with Android 15 & 16 edge-to-edge display standards with immersive status bars and gesture protection.
- **Ultra-Lightweight (R8 Optimized):** Slashed from 22.7 MB down to **3.05 MB** with R8 minification and resource shrinking.

---

## ❄️ Supported Air Conditioners (Multi-Code Engine)

| Brand | Candidate Codes | Included Models & Protocols |
| :--- | :---: | :--- |
| **Hitachi** | **10 Codes** | RAR-2P2 Series, 2018 Inverter (captured hardware), 424-bit with 30ms wake-up leader, 28-bit, Classic Window AC |
| **Voltas** | **5 Codes** | Classic Window AC (NEC 38kHz), Vertis Split (0x20DF), All-Weather Inverter (Coolix 48-bit), Gree OEM (64-bit), 148-Pulse Split |
| **Lloyd** | **5 Codes** | Coolix Inverter (48-bit), Gree OEM Split (64-bit), Indian Split (NEC 32-bit), Classic Window (148-Pulse), Kelon/Hisense OEM |
| **LG** | **5 Codes** | Standard Split (28-bit), Dual-Frame Inverter (Seq 2), AKB Series, 6711A Old Window Series, Smart Inverter Dual Cool |
| **O General** | **4 Codes** | Fujitsu General Inverter (128-bit full frame), Short Frame (56-bit), Old Split (NEC 32-bit), AR-RY1 Series |
| **Carrier** | **5 Codes** | Dynamic 148-Pulse, Coolix Inverter (48-bit), Standard Split, Old Window AC (NEC 32-bit), Carlyle/Durakool Series |
| **Samsung** | **3 Codes** | Standard Inverter (Samsung AC protocol), Classic Window/Split (NEC) |
| **Daikin** | **3 Codes** | Daikin ARC433 Series (Inverter), Classic Split AC |
| **Panasonic** | **3 Codes** | Panasonic Inverter (Kaseikyo), Classic Split & Window AC |
| **Whirlpool** | **3 Codes** | Whirlpool Split Inverter (NEC 38kHz), Classic Series |
| **Gree / Midea** | **3 Codes** | Gree 64-bit Modulo-8, Midea/Coolix 48-bit series |

---

## 📺 Supported Televisions & Streaming Media Boxes

| Brand / Device | Candidate Codes | Supported Protocols & Key Features |
| :--- | :---: | :--- |
| **Mi Box 4K / TV Stick** | **Dedicated** | Custom Xiaomi 38kHz protocol (Power, D-Pad, OK, Back, Home, Menu, Volume) |
| **Xiaomi (Mi TV)** | **4 Codes** | PatchWall Smart TV (NEC `0x00`), Android TV (NEC `0x08`), Mi Box 4K, NEC `0x80` |
| **Samsung TV** | **2 Codes** | Tizen Smart TV & Classic LCD / CRT (Samsung 32-bit with 4.5ms header @ 38kHz) |
| **LG TV** | **2 Codes** | WebOS Smart TV & Classic Goldstar (NEC 32-bit @ 38kHz) |
| **Sony TV** | **2 Codes** | Sony Bravia & OLED TVs (Sony SIRC 12-bit & 15-bit @ 40kHz) |
| **OnePlus TV** | **2 Codes** | OnePlus TV Y, U, and Q Series (Universal NEC @ 38kHz) |
| **TCL** | **2 Codes** | TCL Smart TV / Roku TV & Classic LED (NEC @ 38kHz) |
| **Panasonic TV** | **2 Codes** | Panasonic Viera Series (Kaseikyo 48-bit @ 37kHz) & Classic LCD |
| **Philips TV** | **2 Codes** | Philips Smart TV (NEC @ 38kHz) & Classic Philips (RC5 @ 36kHz) |
| **Toshiba** | **2 Codes** | Regza Android TV & Classic (NEC @ 38kHz) |
| **Vu** | **2 Codes** | Vu Android Smart TV & Classic LED (NEC @ 38kHz) |
| **Micromax** | **2 Codes** | Canvas Smart TV & Classic (NEC @ 38kHz) |
| **Lloyd TV** | **2 Codes** | Lloyd Smart TV & Classic LED (NEC @ 38kHz) |
| **Haier** | **2 Codes** | Haier Smart TV & Classic (NEC @ 38kHz) |
| **Sansui** | **2 Codes** | Sansui Smart TV & Classic (NEC @ 38kHz) |

---

## 🛠️ Key App Features

### 1. Interactive Testing Wizard (OnePlus Style)
- Steps through candidate codes one by one (*"Code 1 of N"*).
- **Multi-Button Checking:**
  - **AC:** Test Power, Test Temp 25°C, Test Mode (Cool/Fan/Dry), and Test Power-Off before saving.
  - **TV:** Test Power, Test Mute, and Test Volume (+).
- Enter custom name and room name (e.g. *"Hitachi AC - Master Bedroom"*).

### 2. On-the-Fly Code Switcher
- Once saved, you can switch candidate codes directly inside the active remote screen with a single tap on the **"Switch Code"** chip, letting you quickly verify alternative codes without having to re-pair.

### 3. OnePlus-Style QR Remote Sharing & Scanning
- **Share:** Tap **Share QR** on any remote card or top bar to generate a high-contrast QR code with full settings.
- **Scan / Import:** Tap **Scan QR** in the Dashboard to scan the code off another phone with the live CameraX viewfinder, or paste the share code from your clipboard.

### 4. Pinned Home Screen Shortcuts
- Pin launcher shortcuts directly to your phone's home screen using `ShortcutManagerCompat`.
- Tapping a shortcut launches directly into that specific remote, bypassing the dashboard and suppressing all reminders.

### 5. Hardware Diagnostics & Simulation Mode
- Detects whether your phone has a physical IR emitter (`ConsumerIrManager`).
- For devices without an IR blaster, enters **Simulation Mode** allowing you to inspect carrier frequencies, pulse lengths, and timing bursts without crashes.

### 6. Voluntary "Cup of Tea" (₹20) Support Flow
- 100% optional voluntary donation via Google Play In-App Billing.
- **3-Day Reminder Policy:** Non-intrusive reminder popup on the Dashboard at most once every 3 days.
- **Never interrupts shortcut launches or active use.**
- **Permanently stops once donated.**
- Progress up to 5 stars (⭐⭐⭐⭐⭐).

---

## 🏗️ Technical Architecture

- **UI Framework:** Jetpack Compose with Material 3
- **Navigation:** AndroidX Navigation 3 with type-safe `@Serializable` NavKeys
- **Infrared Transmitter:** Android `ConsumerIrManager` with raw microsecond carrier frequency synthesis
- **QR Engine:** ZXing Core (`com.google.zxing:core:3.5.3`)
- **Camera Pipeline:** AndroidX CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`)
- **In-App Billing:** Google Play Billing Library (`com.android.billingclient:billing-ktx:7.1.1`)
- **Minification:** R8 full mode with custom keep rules (`app/proguard-rules.pro`)
- **Target SDK:** API 36 (Android 16)
- **Minimum SDK:** API 24 (Android 7.0)

---

## 📂 Project Structure

```
app/src/main/java/com/example/actvremotefreeware/
├── MainActivity.kt                      # Main activity with edge-to-edge & shortcut intent handling
├── Navigation.kt                        # NavDisplay & screen routing
├── NavigationKeys.kt                    # Type-safe @Serializable navigation keys
├── billing/
│   └── DonationManager.kt               # Google Play Billing & 3-day reminder policy
├── core/ir/
│   ├── IrTransmitter.kt                 # ConsumerIrManager wrapper & diagnostics
│   ├── ac/                              # AC Multi-Code protocols
│   │   ├── AcProtocolRouter.kt          # Routes AC state to matching brand protocol
│   │   ├── BrandCodeRegistry.kt         # Master registry for all AC brand profiles
│   │   ├── BroadlinkPacketDecoder.kt    # Base64 Broadlink packet decoder
│   │   ├── CarrierMultiCode.kt          # Carrier multi-code profiles
│   │   ├── HitachiAcProtocol.kt         # Hitachi standard split protocol
│   │   ├── HitachiAc1Protocol.kt        # Hitachi 1-byte protocol
│   │   ├── HitachiAc28Protocol.kt       # Hitachi 28-bit protocol
│   │   ├── HitachiAc424Protocol.kt      # Hitachi 424-bit with 30ms wake-up leader
│   │   ├── HitachiBroadlink1081.kt      # Captured 2018 model 1081
│   │   ├── HitachiBroadlink1084.kt      # Captured 2018 model 1084
│   │   ├── HitachiBroadlink1091.kt      # Captured 2018 model 1091
│   │   ├── HitachiMultiCode.kt          # 10 Hitachi candidate profiles
│   │   ├── LgMultiCode.kt               # LG multi-code profiles
│   │   ├── LloydMultiCode.kt            # Lloyd multi-code profiles
│   │   ├── OGeneralMultiCode.kt         # O General multi-code profiles
│   │   └── VoltasMultiCode.kt           # Voltas multi-code profiles
│   └── tv/                              # TV & Streaming Box protocols
│       ├── MiBoxProtocol.kt             # Xiaomi Mi Box 4K custom 38kHz protocol
│       ├── TvBrandCodeRegistry.kt       # Master registry for all 14 TV brands
│       └── TvProtocols.kt               # NEC, Samsung, Sony SIRC, Panasonic, Philips RC5
├── data/
│   └── SavedRemotesRepository.kt        # SharedPreferences JSON persistence & import
├── model/
│   ├── AcModels.kt                      # AcBrand, AcState, AcMode, FanSpeed
│   ├── ApplianceType.kt                 # AC vs TV enum
│   ├── TvBrand.kt                       # Supported TV brands
│   └── TvCommand.kt                     # TV button command enum
├── ui/
│   ├── about/
│   │   ├── AboutAndDonateDialog.kt      # Advit Singh creator credit & donation UI
│   │   └── SupportReminderDialog.kt     # 3-day dashboard reminder dialog
│   ├── ac/
│   │   ├── AcRemoteScreen.kt            # Tactile AC remote screen with LCD display
│   │   └── AcRemoteViewModel.kt         # AC remote UI state holder
│   ├── dashboard/
│   │   └── DashboardScreen.kt           # Home dashboard with cards & version footer
│   ├── share/
│   │   ├── QrCodeUtils.kt               # QR code generation & serialization
│   │   ├── ScanRemoteDialog.kt          # CameraX QR scanner & paste fallback
│   │   └── ShareRemoteDialog.kt         # OnePlus-style QR share display
│   ├── tv/
│   │   └── TvRemoteScreen.kt            # Tactile TV remote with D-pad, rockers, numpad
│   └── wizard/
│       ├── BrandPickerScreen.kt         # AC brand picker
│       ├── DeviceTypePickerScreen.kt    # Appliance type selector (AC vs TV)
│       ├── TestingWizardScreen.kt       # Interactive AC testing wizard
│       ├── TvBrandPickerScreen.kt       # TV brand picker
│       └── TvTestingWizardScreen.kt     # Interactive TV testing wizard
└── util/
    └── ShortcutUtils.kt                 # Pinned launcher shortcut manager
```

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 or JDK 21 (bundled in Android Studio: `jbr`)
- Android SDK 36

### Build Commands (Windows PowerShell)

```powershell
# Set Java Home to Android Studio JBR
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Run automated unit tests (26/26 tests)
.\gradlew.bat testDebugUnitTest

# Assemble Debug APK (22.7 MB)
.\gradlew.bat assembleDebug

# Assemble R8-Optimized Release APK (3.05 MB)
.\gradlew.bat assembleRelease

# Build Google Play Store Release Bundle (.aab)
.\gradlew.bat bundleRelease
```

### Output Locations
- **Release APK:** `app/build/outputs/apk/release/app-release.apk` (3.05 MB, signed with debug key for direct phone installation)
- **Play Store Bundle:** `app/build/outputs/bundle/release/app-release.aab` (6.1 MB, ready to upload to Google Play Console)

---

## 👨‍💻 Developer & Support

- **Developer:** Advit Singh
- **Contact & Device Requests:** [maahihimanshi@gmail.com](mailto:maahihimanshi@gmail.com)
- If an AC, TV, or Set-Top Box model doesn't work for you, please email your brand name, model number, and a photo of your original remote. We analyze the infrared waveforms and add new codes in regular updates!
