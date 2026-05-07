# Sarvix APK Build Guide

This guide will help you build a test APK for the Sarvix Android application.

## Prerequisites

1. **Android Studio** - Hedgehog (2023.1.1) or later
   - Download from: https://developer.android.com/studio

2. **JDK 17** or later
   - Check with: `java -version`

3. **Android SDK** - API 26-34
   - Install via Android Studio SDK Manager

4. **Firebase Project** with configuration file

## Quick Start (5 minutes)

### Step 1: Open Project in Android Studio

1. Launch Android Studio
2. Select "Open" and navigate to the `SarvixApp` folder
3. Wait for Gradle sync to complete (may take 2-5 minutes)

### Step 2: Configure Firebase

1. Go to https://console.firebase.google.com/
2. Create a new project named "Sarvix-Test"
3. Add an Android app:
   - Package name: `com.sarvix.app`
   - Download `google-services.json`
4. Replace the placeholder file at:
   ```
   SarvixApp/app/google-services.json
   ```
5. Enable **Email/Password** authentication in Firebase Console

### Step 3: Create Keystore (One-time setup)

Open terminal in Android Studio (bottom panel) and run:

```bash
cd app
keytool -genkey -v -keystore sarvix-test.keystore -alias sarvix-test -keyalg RSA -keysize 2048 -validity 10000 -storepass sarvixtest -keypass sarvixtest -dname "CN=Sarvix Test, OU=Development, O=Sarvix, L=Test, ST=Test, C=US"
```

### Step 4: Build the APK

#### Option A: Build via Android Studio (Recommended)

1. In Android Studio, go to **Build** → **Generate Signed Bundle / APK...**
2. Select **APK**
3. Click **Create new...** for the keystore:
   - Key store path: `[project]/app/sarvix-test.keystore`
   - Password: `sarvixtest`
   - Key alias: `sarvix-test`
   - Key password: `sarvixtest`
4. Select **release** or **preview** build variant
5. Click **Finish**
6. The APK will be at:
   ```
   app/release/app-release.apk
   ```

#### Option B: Build via Command Line

```bash
# Navigate to project root
cd SarvixApp

# Create keystore (if not done)
cd app
keytool -genkey -v -keystore sarvix-test.keystore -alias sarvix-test -keyalg RSA -keysize 2048 -validity 10000 -storepass sarvixtest -keypass sarvixtest -dname "CN=Sarvix Test, OU=Development, O=Sarvix, L=Test, ST=Test, C=US"
cd ..

# Build release APK
./gradlew assembleRelease

# Or build preview APK (recommended for testing)
./gradlew assemblePreview
```

The APK will be located at:
- Release: `app/build/outputs/apk/release/app-release.apk`
- Preview: `app/build/outputs/apk/preview/app-preview.apk`

## Build Variants

| Variant | Use Case | Minified | Signed |
|---------|----------|----------|--------|
| `debug` | Development | No | No |
| `preview` | **Testing (Recommended)** | No | Yes |
| `release` | Production | Yes | Yes |

For preview testing, use the **preview** variant as it:
- Includes debug symbols for better error reporting
- Is signed for device installation
- Has faster build times

## Installing on Device

### Method 1: ADB (Command Line)

```bash
# Connect device via USB with USB debugging enabled
adb devices

# Install APK
adb install -r app/build/outputs/apk/preview/app-preview.apk

# Or for release
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Method 2: Direct Transfer

1. Enable **Install from Unknown Sources** on your Android device:
   - Settings → Security → Unknown Sources (or Install unknown apps)

2. Transfer APK to device:
   - Email, Google Drive, USB cable, or file sharing app

3. Open the APK file on device and tap **Install**

### Method 3: Android Studio

1. Connect device via USB
2. Enable USB debugging on device
3. In Android Studio, select your device from the dropdown
4. Click the **Run** button (green play icon)

## Troubleshooting

### Build Errors

**Error: "Could not find google-services.json"**
- Solution: Download the real `google-services.json` from Firebase Console and replace the placeholder

**Error: "Keystore file does not exist"**
- Solution: Run the keystore creation command in Step 3

**Error: "Gradle sync failed"**
- Solution: 
  1. File → Invalidate Caches / Restart
  2. Try sync again

**Error: "Minimum SDK version"**
- Solution: Your device must run Android 8.0 (API 26) or higher

### Installation Errors

**Error: "App not installed"**
- Uninstall any existing Sarvix app first
- Check APK is signed (preview or release, not debug)

**Error: "Parse error"**
- APK may be corrupted, rebuild
- Device Android version too old (need API 26+)

**Error: "Blocked by Play Protect"**
- Tap "Install anyway" 
- This is normal for test APKs not from Play Store

## Firebase Configuration Checklist

Before building, ensure Firebase is configured:

- [ ] Project created in Firebase Console
- [ ] Android app added with package `com.sarvix.app`
- [ ] `google-services.json` downloaded and placed in `app/`
- [ ] **Authentication** → **Email/Password** provider enabled
- [ ] **Firestore Database** created (start in test mode)
- [ ] **Storage** bucket created

## Test Accounts

Create test accounts in the app with these formats:
- Email: Any valid email
- Username: Must start with @ (e.g., @testuser, @johndoe)
- Password: Minimum 8 characters

## Need Help?

If you encounter issues:

1. Check the **SETUP_INSTRUCTIONS.md** for detailed Firebase setup
2. Review **DATABASE_SCHEMA.md** for Firestore configuration
3. Check Android Studio's **Build** → **Build Output** for error details

## APK Distribution

To share the test APK with others:

1. Build the preview APK
2. Upload to Google Drive, Dropbox, or similar
3. Share the download link
4. Recipients must enable "Install from Unknown Sources"

**Note**: This is a test build. For production distribution, you need:
- Play Store developer account ($25 one-time fee)
- Signed release APK
- App signing via Google Play