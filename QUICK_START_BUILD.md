# Quick Start: Build Sarvix Test APK

## ⚡ Fastest Method (Using Android Studio)

### Step 1: Open Project (2 minutes)
1. Download/transfer the `SarvixApp` folder to your computer
2. Open **Android Studio**
3. Click **File → Open** and select the `SarvixApp` folder
4. Wait for Gradle sync (2-3 minutes, first time only)

### Step 2: Configure Firebase (3 minutes)
1. Go to https://console.firebase.google.com/
2. Click **"Create a project"** → Name it "Sarvix-Test"
3. Click the **Android icon** (</>) to add an app
4. Enter package name: `com.sarvix.app`
5. Download `google-services.json`
6. Copy it to: `SarvixApp/app/` (replace the placeholder)
7. In Firebase Console, go to **Build → Authentication → Get Started**
8. Enable **Email/Password** provider

### Step 3: Build APK (1 minute)

**Option A: Build Debug APK (Fastest)**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

**Option B: Build Signed APK (For device installation)**
```
Build → Generate Signed Bundle / APK...
→ Select APK
→ Create new keystore:
   - Path: [project]/app/sarvix-test.keystore
   - Password: sarvixtest
   - Alias: sarvix-test
   - Key password: sarvixtest
→ Select release
→ Finish
```
APK location: `app/build/outputs/apk/release/app-release.apk`

### Step 4: Install on Device

**Method 1 - ADB (if you have Android SDK)**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Method 2 - Direct Install**
1. Transfer APK to your Android device (email, USB, cloud)
2. On device: Enable **Settings → Security → Unknown Sources**
3. Open APK file and tap **Install**

---

## 📱 Device Requirements

- **Android 8.0+** (API 26 or higher)
- **Internet connection** (for Firebase)
- **~50MB free space**

---

## 🔧 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Gradle sync failed" | File → Invalidate Caches / Restart |
| "Could not find google-services.json" | Download from Firebase Console |
| "App not installed" | Uninstall old version first |
| "Parse error" | Device Android version too old (need 8.0+) |

---

## 📦 Build Variants

| Variant | Best For | Location |
|---------|----------|----------|
| Debug | Development | `app/build/outputs/apk/debug/` |
| Preview | **Testing** | `app/build/outputs/apk/preview/` |
| Release | Production | `app/build/outputs/apk/release/` |

---

## 🚀 Quick Test Checklist

After installing, test these features:

- [ ] Create account with @username
- [ ] Complete profile setup (3+ interests)
- [ ] Set mood manually
- [ ] Send message with intent tag
- [ ] Request clarification on received message
- [ ] Browse matches
- [ ] Create a post in Sarvix Reads

---

## 📚 More Documentation

- **Full Setup**: See `SETUP_INSTRUCTIONS.md`
- **Database Schema**: See `DATABASE_SCHEMA.md`
- **Project Details**: See `PROJECT_SUMMARY.md`

---

## 💡 Tips

1. **First build takes longer** - Gradle downloads dependencies
2. **Use Preview variant** for testing - faster builds than release
3. **Enable USB debugging** for direct install from Android Studio
4. **Test on real device** - some features work better than emulator